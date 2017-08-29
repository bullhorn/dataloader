package com.bullhorn.dataloader.task;

import com.bullhorn.dataloader.data.ActionTotals;
import com.bullhorn.dataloader.data.CsvFileWriter;
import com.bullhorn.dataloader.data.Result;
import com.bullhorn.dataloader.data.Row;
import com.bullhorn.dataloader.enums.EntityInfo;
import com.bullhorn.dataloader.rest.Field;
import com.bullhorn.dataloader.rest.Preloader;
import com.bullhorn.dataloader.rest.Record;
import com.bullhorn.dataloader.rest.RestApi;
import com.bullhorn.dataloader.util.AssociationUtil;
import com.bullhorn.dataloader.util.MethodUtil;
import com.bullhorn.dataloader.util.PrintUtil;
import com.bullhorn.dataloader.util.PropertyFileUtil;
import com.bullhorn.dataloader.util.StringConsts;
import com.bullhornsdk.data.exception.RestApiException;
import com.bullhornsdk.data.model.entity.association.AssociationField;
import com.bullhornsdk.data.model.entity.core.standard.ClientContact;
import com.bullhornsdk.data.model.entity.core.standard.ClientCorporation;
import com.bullhornsdk.data.model.entity.core.type.AssociationEntity;
import com.bullhornsdk.data.model.entity.core.type.BullhornEntity;
import com.bullhornsdk.data.model.entity.core.type.CreateEntity;
import com.bullhornsdk.data.model.entity.core.type.QueryEntity;
import com.bullhornsdk.data.model.entity.core.type.SearchEntity;
import com.bullhornsdk.data.model.entity.core.type.UpdateEntity;
import com.bullhornsdk.data.model.parameter.QueryParams;
import com.bullhornsdk.data.model.parameter.SearchParams;
import com.bullhornsdk.data.model.parameter.standard.ParamFactory;
import com.bullhornsdk.data.model.response.crud.CrudResponse;
import com.google.common.collect.Sets;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Level;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Handles converting a row of CSV data into REST calls to either insert or update a record in Bullhorn.
 */
public class LoadTask<B extends BullhornEntity> extends AbstractTask<B> {
    private static final Integer RECORD_RETURN_COUNT = 500;

    protected B entity;
    protected Preloader preloader;
    protected Map<String, Method> methodMap;
    boolean isNewEntity = true;
    Integer entityId;
    protected Record record;

    public LoadTask(EntityInfo entityInfo,
                    Row row,
                    Preloader preloader,
                    CsvFileWriter csvFileWriter,
                    PropertyFileUtil propertyFileUtil,
                    RestApi restApi,
                    PrintUtil printUtil,
                    ActionTotals actionTotals) {
        super(entityInfo, row, csvFileWriter, propertyFileUtil, restApi, printUtil, actionTotals);
        this.preloader = preloader;
        this.methodMap = entityInfo.getSetterMethodMap();
    }

    @Override
    public void run() {
        Result result;
        try {
            result = handle();
        } catch (Exception e) {
            result = handleFailure(e, entityId);
        }
        writeToResultCsv(result);
    }

    protected Result handle() throws Exception {
        record = new Record(entityInfo, row, propertyFileUtil);
        addParentLocatorExistField(record);
        getOrCreateEntity();
        handleFields();
        insertAttachmentToDescription();
        insertOrUpdateEntity();
        createAssociations();
        return createResult();
    }

    /**
     * Performs lookup for entity if the entity exist field is set. If found, will use the existing entity. If not
     * found will create a new entity.
     */
    void getOrCreateEntity() throws IOException, IllegalAccessException, InstantiationException {
        List<B> existingEntityList = findEntityList(record);
        if (!existingEntityList.isEmpty()) {
            if (existingEntityList.size() > 1) {
                throw new RestApiException("Cannot Perform Update - Multiple Records Exist. Found "
                    + existingEntityList.size() + " " + entityInfo.getEntityName()
                    + " records with the same ExistField criteria of: " + record.getEntityExistFields().stream()
                    .map(field -> field.getCell().getName() + "=" + field.getStringValue())
                    .collect(Collectors.joining(" AND ")));
            } else {
                isNewEntity = false;
                entity = existingEntityList.get(0);
                entityId = entity.getId();
            }
        } else {
            entity = (B) entityInfo.getEntityClass().newInstance();
        }
    }

    /**
     * Calls rest to insert or update the entity by passing in the filled out entity object.
     */
    protected void insertOrUpdateEntity() throws IOException {
        if (isNewEntity) {
            CrudResponse response = restApi.insertEntity((CreateEntity) entity);
            entityId = response.getChangedEntityId();
            entity.setId(entityId);
            if (entity.getClass() == ClientCorporation.class) {
                setDefaultContactExternalId(entityId);
            }
        } else {
            restApi.updateEntity((UpdateEntity) entity);
        }
    }

    /**
     * Handles inserting/updating all cells in the row.
     *
     * Direct Fields: Populate the field on the entity.
     * Compound Fields (address): Get the address object and populate the field on the address.
     * To-One Associations: Get the association object and populate the internal ID field.
     * To-Many Associations: Call the association REST method (unless we are loading notes)
     */
    void handleFields() throws Exception {
        for (Field field : record.getFields()) {
            if (field.isToMany()) {
                if (entityInfo == EntityInfo.NOTE) {
                    prepopulateAssociation(field);
                }
            } else if (field.isToOne()) {
                B toOneEntity = findToOneEntity(field);
                field.populateAssociationOnEntity(entity, toOneEntity);
            } else {
                field.populateFieldOnEntity(entity);
            }
        }
    }

    private B findToOneEntity(Field field) {
        List<B> list;

        if (field.getFieldEntity().isSearchEntity()) {
            list = searchForEntity(field.getName(), field.getStringValue(), field.getFieldType(),
                field.getFieldEntity(), null);
        } else {
            list = queryForEntity(field.getName(), field.getStringValue(), field.getFieldType(),
                field.getFieldEntity(), null);
        }

        validateListFromRestCall(field.getCell().getName(), list, field.getStringValue());

        return list.get(0);
    }

    /**
     * Special case that handles setting the externalID of the defaultContact that is created on a ClientCorporation.
     * This allows for easily finding that contact later using the externalID, which will be of the format:
     * `defaultContact1234`, where 1234 is the externalID that was set on the parent ClientCorporation.
     */
    private void setDefaultContactExternalId(Integer entityId) {
        List<ClientCorporation> clientCorporations = (List<ClientCorporation>) queryForEntity(
            "id", entityId.toString(), Integer.class, EntityInfo.CLIENT_CORPORATION, Sets.newHashSet("id", "externalID"));
        if (!clientCorporations.isEmpty()) {
            ClientCorporation clientCorporation = clientCorporations.get(0);
            if (StringUtils.isNotBlank(clientCorporation.getExternalID())) {
                final String query = "clientCorporation.id=" + clientCorporation.getId() + " AND status='Archive'";
                List<ClientContact> clientContacts = restApi.queryForList(ClientContact.class, query, Sets.newHashSet("id"), ParamFactory.queryParams());
                if (!clientContacts.isEmpty()) {
                    ClientContact clientContact = clientContacts.get(0);
                    String defaultContactExternalId = "defaultContact" + clientCorporation.getExternalID();
                    clientContact.setExternalID(defaultContactExternalId);
                    restApi.updateEntity(clientContact);
                }
            }
        }
    }

    /**
     * Handles setting the description field of an entity (if one exists) to the previously converted HTML resume
     * file or description file stored on disk, if a convertAttachments has been done previously. This only works if
     * there is an externalID being used in the input file.
     */
    private void insertAttachmentToDescription() throws IOException, InvocationTargetException, IllegalAccessException {
        String descriptionMethod = MethodUtil.findBestMatch(methodMap.keySet(), StringConsts.DESCRIPTION);
        if (descriptionMethod != null && row.hasValue(StringConsts.EXTERNAL_ID)) {
            String convertedAttachmentFilepath = propertyFileUtil.getConvertedAttachmentFilepath(entityInfo,
                row.getValue(StringConsts.EXTERNAL_ID));
            if (convertedAttachmentFilepath != null) {
                File convertedAttachmentFile = new File(convertedAttachmentFilepath);
                if (convertedAttachmentFile.exists()) {
                    String description = FileUtils.readFileToString(convertedAttachmentFile);
                    methodMap.get(descriptionMethod).invoke(entity, description);
                }
            }
        }
    }

    void validateListFromRestCall(String field, List<B> list, String value) {
        if (list == null || list.isEmpty()) {
            throw new RestApiException("Cannot find To-One Association: '" + field + "' with value: '" + value + "'");
        } else if (list.size() > 1) {
            throw new RestApiException("Found " + list.size() + " duplicate To-One Associations: '" + field + "' with value: '" + value + "'");
        }
    }

    /**
     * Makes association REST calls for all To-Many relationships for the entity.
     */
    @SuppressWarnings("unchecked")
    private void createAssociations() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        // Note associations are filled out in the create call
        if (entityInfo == EntityInfo.NOTE) {
            return;
        }

        for (Field field : record.getToManyFields()) {
            AssociationField associationField = AssociationUtil.getToManyField(field);
            List<B> associations = findAssociations(field);
            List<Integer> associationIds = associations.stream().map(BullhornEntity::getId).collect(Collectors.toList());

            try {
                restApi.associateWithEntity((Class<AssociationEntity>) entityInfo.getEntityClass(), entityId,
                    associationField, Sets.newHashSet(associationIds));
            } catch (RestApiException exception) {
                // Provides a simpler duplication error message with all of the essential data
                if (exception.getMessage().contains("an association between " + entityInfo.getEntityName())
                    && exception.getMessage().contains(entityId + " and " + field.getFieldEntity().getEntityName() + " ")) {
                    printUtil.log(Level.INFO, "Association from " + entityInfo.getEntityName()
                        + " entity " + entityId + " to " + associationField.getAssociationType().getSimpleName()
                        + " entities " + associationIds.toString() + " already exists.");
                } else {
                    throw exception;
                }
            }
        }
    }

    private void prepopulateAssociation(Field field) throws IllegalAccessException, InstantiationException,
        InvocationTargetException, ParseException {
        List<B> associations = findAssociations(field);
        for (B association : associations) {
            field.populateAssociationOnEntity(entity, association);
        }
    }

    /**
     * Returns the list of entities that match the search criteria in the given To-Many field.
     */
    private List<B> findAssociations(Field field) throws InvocationTargetException, IllegalAccessException {
        Set<String> values = Sets.newHashSet(field.getStringValue().split(propertyFileUtil.getListDelimiter()));
        List<B> existingAssociations = getExistingAssociations(field);
        if (existingAssociations.size() != values.size()) {
            Set<String> existingAssociationValues = getFieldValueSet(field, existingAssociations);
            if (existingAssociations.size() > values.size()) {
                String duplicates = existingAssociationValues.stream().map(n -> "\t" + n)
                    .collect(Collectors.joining("\n"));
                throw new RestApiException("Found " + existingAssociations.size()
                    + " duplicate To-Many Associations: '" + field.getCell().getName()
                    + "' with value:\n" + duplicates);
            } else {
                String missingAssociations = values.stream().filter(n -> !existingAssociationValues.contains(n))
                    .map(n -> "\t" + n).collect(Collectors.joining("\n"));
                throw new RestApiException("Error occurred: " + field.getCell().getAssociationBaseName()
                    + " does not exist with " + field.getName() + " of the following values:\n" + missingAssociations);
            }
        }

        return existingAssociations;
    }

    /**
     * Given a list of entity objects, this returns the non-duplicate set of all field values.
     */
    private Set<String> getFieldValueSet(Field field, List<B> entities) throws InvocationTargetException, IllegalAccessException {
        Set<String> values = new HashSet<>();
        for (B entity : entities) {
            String value = field.getValueFromEntity(entity).toString();
            values.add(value);
        }
        return values;
    }

    /**
     * Makes the lookup call to check that all associated values are present, and there are no duplicates. This will
     * work with up to 500 associated records, such as candidates or businessSectors. It will perform the lookup using
     * the field given after the period, like: 'businessSector.name' or 'candidate.id'
     *
     * @param field the To-Many association field to lookup records for
     */
    private <Q extends QueryEntity, S extends SearchEntity> List<B> getExistingAssociations(Field field) {
        List<B> list;
        if (field.getFieldEntity().isSearchEntity()) {
            List<String> values = Arrays.asList(field.getStringValue().split(propertyFileUtil.getListDelimiter()));
            String filter = values.stream().map(n -> getQueryStatement(field.getName(), n, field.getFieldType(),
                field.getFieldEntity())).collect(Collectors.joining(" OR "));
            SearchParams searchParams = ParamFactory.searchParams();
            searchParams.setCount(RECORD_RETURN_COUNT);
            list = (List<B>) restApi.searchForList((Class<S>) field.getFieldEntity().getEntityClass(), filter, null, searchParams);
        } else {
            List<String> values = Arrays.asList(field.getStringValue().split(propertyFileUtil.getListDelimiter()));
            String filter = values.stream().map(n -> getWhereStatement(field.getName(), n, field.getFieldType()))
                .collect(Collectors.joining(" OR "));
            QueryParams queryParams = ParamFactory.queryParams();
            queryParams.setCount(RECORD_RETURN_COUNT);
            list = (List<B>) restApi.queryForList((Class<Q>) field.getFieldEntity().getEntityClass(), filter, null, queryParams);
        }
        return list;
    }

    /**
     * For custom objects, this checks the parent locator field and automatically adds it if unspecified.
     *
     * @param record the record specified by the user, which this method may modify
     */
    private void addParentLocatorExistField(Record record) throws IOException {
        if (record.getEntityInfo().isCustomObject()) {
            if (record.getEntityExistFields().stream().noneMatch(Field::isToOne)) {
                if (record.getFields().stream().noneMatch(Field::isToOne)) {
                    throw new IOException("Missing parent entity locator column, for example: 'candidate.id', "
                        + "'candidate.externalID', or 'candidate.whatever' so that the custom object can be loaded "
                        + "to the correct parent entity.");
                } else {
                    //noinspection ConstantConditions
                    record.getFields().stream().filter(Field::isToOne).findFirst().get().setExistField(true);
                }
            }
        }
    }

    Result createResult() {
        if (isNewEntity) {
            return Result.insert(entityId);
        } else {
            return Result.update(entityId);
        }
    }
}

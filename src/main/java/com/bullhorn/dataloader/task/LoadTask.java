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
import com.bullhornsdk.data.model.entity.embedded.OneToMany;
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
import java.util.ArrayList;
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
    private Record record;

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
            this.record = new Record(entityInfo, row, propertyFileUtil);
            result = handle();
        } catch (Exception e) {
            result = handleFailure(e, entityId);
        }
        writeToResultCsv(result);
    }

    protected Result handle() throws Exception {
        getOrCreateEntity();
        handleFields();
        insertAttachmentToDescription();
        insertOrUpdateEntity();
        createNewAssociations();
        return createResult();
    }

    /**
     * Performs lookup for entity if the entity exist field is set. If found, will use the existing entity. If not
     * found will create a new entity.
     */
    void getOrCreateEntity() throws IOException, IllegalAccessException, InstantiationException {
        Map<String, String> entityExistFieldsMap = getEntityExistFieldsMap();
        List<B> existingEntityList = findEntityList(entityExistFieldsMap);
        if (!existingEntityList.isEmpty()) {
            if (existingEntityList.size() > 1) {
                throw new RestApiException("Cannot Perform Update - Multiple Records Exist. Found "
                    + existingEntityList.size() + " " + entityInfo.getEntityName()
                    + " records with the same ExistField criteria of: " + getEntityExistFieldsMap());
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

    // region Direct Field Methods

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
                    prepopulateNoteAssociation(field.getCell().getName());
                }
            } else if (field.isToOne()) {
                B toOneEntity = findToOneEntity(field);
                field.populateAssociationOnEntity(entity, toOneEntity);
            } else {
                field.populateFieldOnEntity(entity);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private B findToOneEntity(Field field) {
        List<B> list;

        if (field.getFieldEntity().isSearchEntity()) {
            list = searchForEntity(field.getName(), field.getStringValue(), field.getFieldType(),
                field.getFieldEntity().getEntityClass(), null);
        } else {
            list = queryForEntity(field.getName(), field.getStringValue(), field.getFieldType(),
                field.getFieldEntity().getEntityClass(), null);
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
            "id",
            entityId.toString(),
            Integer.class,
            (Class<B>) ClientCorporation.class,
            Sets.newHashSet("id", "externalID"));
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
    // endregion

    void validateListFromRestCall(String field, List<B> list, String value) {
        if (list == null || list.isEmpty()) {
            throw new RestApiException("Cannot find To-One Association: '" + field + "' with value: '" + value + "'");
        } else if (list.size() > 1) {
            throw new RestApiException("Found " + list.size() + " duplicate To-One Associations: '" + field + "' with value: '" + value + "'");
        }
    }
    // endregion

    // region To-Many Association Methods

    /**
     * Makes association REST calls for all To-Many relationships for the entity.
     */
    private void createNewAssociations() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        if (entityInfo != EntityInfo.NOTE) {
            for (Field field : record.getFields()) {
                if (field.isToMany()) {
                    List<AssociationField<AssociationEntity, BullhornEntity>> associationFieldList =
                        AssociationUtil.getToManyFields(entityInfo);
                    for (AssociationField associationField : associationFieldList) {
                        if (associationField.getAssociationFieldName().equalsIgnoreCase(
                            field.getCell().getName().substring(0, field.getCell().getName().indexOf(".")))) {
                            addAssociationToEntity(field.getCell().getName(), associationField);
                        }
                    }
                }
            }
        }
    }

    /**
     * Makes an associate REST call to associate the given one to many relationship with the entity.
     */
    private void addAssociationToEntity(String field, AssociationField associationField) throws NoSuchMethodException,
        InvocationTargetException, IllegalAccessException {
        List<Integer> newAssociationIdList = getNewAssociationIdList(field, associationField);
        try {
            restApi.associateWithEntity((Class<AssociationEntity>) entityInfo.getEntityClass(), entityId,
                associationField, Sets.newHashSet(newAssociationIdList));
        } catch (RestApiException e) {
            // Provides a simpler duplication error message with all of the essential data
            if (e.getMessage().contains("an association between " + entityInfo.getEntityName())
                && e.getMessage().contains(entityId + " and " + associationField.getAssociationType().getSimpleName() + " ")) {
                printUtil.log(Level.INFO, "Association from " + entityInfo.getEntityName()
                    + " entity " + entityId + " to " + associationField.getAssociationType().getSimpleName()
                    + " entities " + newAssociationIdList.toString() + " already exists.");
            } else {
                throw e;
            }
        }
    }

    private void prepopulateNoteAssociation(String field) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException, InstantiationException {
        String toManyEntityName = field.substring(0, field.indexOf("."));

        List<AssociationField<AssociationEntity, BullhornEntity>> associationFieldList =
            AssociationUtil.getToManyFields(entityInfo);
        for (AssociationField associationField : associationFieldList) {
            if (associationField.getAssociationFieldName().equalsIgnoreCase(field.substring(0, field.indexOf(".")))) {
                List<Integer> associationIdList = getNewAssociationIdList(field, associationField);
                Class associationClass = associationField.getAssociationType();
                List<B> associationList = new ArrayList<>();
                for (Integer associationId : associationIdList) {
                    B associationInstance = (B) associationClass.newInstance();
                    associationInstance.setId(associationId);
                    associationList.add(associationInstance);
                }
                OneToMany oneToMany = new OneToMany();
                oneToMany.setData(associationList);

                Method method = methodMap.get(toManyEntityName.toLowerCase());
                method.invoke(entity, oneToMany);
            }
        }
    }

    /**
     * Returns the list of internal IDs that match the search criteria in the given field.
     */
    private List<Integer> getNewAssociationIdList(String field, AssociationField associationField)
        throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        String associationName = field.substring(0, field.indexOf("."));
        String fieldName = field.substring(field.indexOf(".") + 1);

        Set<String> valueSet = Sets.newHashSet(row.getValue(field).split(propertyFileUtil.getListDelimiter()));
        Method method = AssociationUtil.getAssociationGetMethod(associationField, fieldName);
        List<B> existingAssociations = getExistingAssociations(field, associationField, valueSet);

        if (existingAssociations.size() != valueSet.size()) {
            Set<String> existingAssociationSet = getExistingAssociationValues(method, existingAssociations);

            if (existingAssociations.size() > valueSet.size()) {
                String duplicateAssociations = existingAssociationSet.stream().map(n -> "\t" + n).collect(Collectors.joining("\n"));
                throw new RestApiException("Found " + existingAssociations.size() + " duplicate To-Many Associations: '" + field + "' with value:\n" + duplicateAssociations);
            } else {
                String missingAssociations = valueSet.stream().filter(n -> !existingAssociationSet.contains(n)).map(n -> "\t" + n).collect(Collectors.joining("\n"));
                throw new RestApiException("Error occurred: " + associationName + " does not exist with " + fieldName + " of the following values:\n" + missingAssociations);
            }
        }

        List<Integer> associationIdList = findIdsOfAssociations(valueSet, existingAssociations, method);
        return associationIdList;
    }

    /**
     * Given a list of entity objects, this returns the non-duplicate set of all returned values.
     */
    private Set<String> getExistingAssociationValues(Method method, List<B> existingAssociations) {
        return existingAssociations.stream().map(n -> {
            try {
                return method.invoke(n).toString();
            } catch (Exception shouldNeverHappen) {
                return null;
            }
        }).collect(Collectors.toSet());
    }

    /**
     * Returns the list of internal IDs from the given list of bullhorn entity object.
     */
    private List<Integer> findIdsOfAssociations(Set<String> valueSet, List<B> existingAssociations, Method method) throws IllegalAccessException, InvocationTargetException {
        List<Integer> associationIdList = new ArrayList<>();
        for (B association : existingAssociations) {
            String returnedValue = String.valueOf(method.invoke(association));
            if (valueSet.contains(returnedValue)) {
                associationIdList.add(association.getId());
            }
        }
        return associationIdList;
    }

    /**
     * Makes the lookup call to check that all associated values are present, and there are no duplicates. This will
     * work with up to 500 associated records, such as candidates or businessSectors. It will perform the lookup using
     * the field given after the period, like: 'businessSector.name' or 'candidate.id'
     */
    private <Q extends QueryEntity, S extends SearchEntity> List<B> getExistingAssociations(
        String field, AssociationField associationField, Set<String> valueSet) {
        List<B> list;
        Class<B> associationClass = associationField.getAssociationType();

        if (SearchEntity.class.isAssignableFrom(associationClass)) {
            String where = getQueryStatement(valueSet, field, associationClass);
            SearchParams searchParams = ParamFactory.searchParams();
            searchParams.setCount(RECORD_RETURN_COUNT);
            list = (List<B>) restApi.searchForList((Class<S>) associationClass, where, null, searchParams);
        } else {
            String where = getWhereStatement(valueSet, field, associationClass);
            QueryParams queryParams = ParamFactory.queryParams();
            queryParams.setCount(RECORD_RETURN_COUNT);
            list = (List<B>) restApi.queryForList((Class<Q>) associationClass, where, null, queryParams);
        }

        return list;
    }

    private String getQueryStatement(Set<String> valueSet, String field, Class<B> associationClass) {
        String fieldName = field.substring(field.indexOf(".") + 1, field.length());
        return valueSet.stream().map(n -> getQueryStatement(fieldName, n, getFieldType(associationClass, field, fieldName), associationClass)).collect(Collectors.joining(" OR "));
    }

    private String getWhereStatement(Set<String> valueSet, String field, Class<B> associationClass) {
        String fieldName = field.substring(field.indexOf(".") + 1, field.length());
        return valueSet.stream().map(n -> getWhereStatement(fieldName, n, getFieldType(associationClass, field, fieldName))).collect(Collectors.joining(" OR "));
    }
    // endregion

    Result createResult() {
        if (isNewEntity) {
            return Result.insert(entityId);
        } else {
            return Result.update(entityId);
        }
    }
}

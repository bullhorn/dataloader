package com.bullhorn.dataloader.task;

import com.bullhorn.dataloader.data.ActionTotals;
import com.bullhorn.dataloader.data.CsvFileWriter;
import com.bullhorn.dataloader.data.Result;
import com.bullhorn.dataloader.data.Row;
import com.bullhorn.dataloader.enums.EntityInfo;
import com.bullhorn.dataloader.rest.CompleteUtil;
import com.bullhorn.dataloader.rest.Field;
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
import com.bullhornsdk.data.model.entity.core.standard.Person;
import com.bullhornsdk.data.model.entity.core.type.AssociationEntity;
import com.bullhornsdk.data.model.entity.core.type.BullhornEntity;
import com.bullhornsdk.data.model.entity.core.type.CreateEntity;
import com.bullhornsdk.data.model.entity.core.type.QueryEntity;
import com.bullhornsdk.data.model.entity.core.type.SearchEntity;
import com.bullhornsdk.data.model.entity.core.type.UpdateEntity;
import com.bullhornsdk.data.model.parameter.standard.ParamFactory;
import com.bullhornsdk.data.model.response.crud.CrudResponse;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Handles converting a row of CSV data into REST calls to either insert or update a record in Bullhorn.
 */
public class LoadTask<B extends BullhornEntity> extends AbstractTask<B> {
    private B entity;
    private boolean isNewEntity = true;
    private Record record;

    public LoadTask(EntityInfo entityInfo,
                    Row row,
                    CsvFileWriter csvFileWriter,
                    PropertyFileUtil propertyFileUtil,
                    RestApi restApi,
                    PrintUtil printUtil,
                    ActionTotals actionTotals,
                    CompleteUtil completeUtil) {
        super(entityInfo, row, csvFileWriter, propertyFileUtil, restApi, printUtil, actionTotals, completeUtil);
    }

    protected Result handle() throws Exception {
        record = new Record(entityInfo, row, propertyFileUtil);
        getOrCreateEntity();
        handleFields();
        insertAttachmentToDescription();
        insertOrUpdateEntity();
        createAssociations();
        return isNewEntity ? Result.insert(entityId) : Result.update(entityId);
    }

    /**
     * Performs lookup for entity if the entity exist field is set. If found, will use the existing entity. If not found
     * will create a new entity.
     */
    @SuppressWarnings("unchecked")
    private void getOrCreateEntity() throws IllegalAccessException, InstantiationException {
        List<B> foundEntityList = findEntityList(record);
        if (!foundEntityList.isEmpty()) {
            if (foundEntityList.size() > 1) {
                throw new RestApiException("Cannot Perform Update - Multiple Records Exist. Found "
                    + foundEntityList.size() + " " + entityInfo.getEntityName()
                    + " records with the same ExistField criteria of: " + record.getEntityExistFields().stream()
                    .map(field -> field.getCell().getName() + "=" + field.getStringValue())
                    .collect(Collectors.joining(" AND ")));
            } else {
                isNewEntity = false;
                entity = foundEntityList.get(0);
                entityId = entity.getId();
            }
        } else {
            entity = (B) entityInfo.getEntityClass().newInstance();
        }
    }

    /**
     * Calls rest to insert or update the entity by passing in the filled out entity object.
     */
    private void insertOrUpdateEntity() {
        if (isNewEntity) {
            CrudResponse response = restApi.insertEntity((CreateEntity) entity);
            entityId = response.getChangedEntityId();
            entity.setId(entityId);
            postProcessEntityInsert(entity.getId());
        } else {
            restApi.updateEntity((UpdateEntity) entity);
        }
    }

    /**
     * Handles inserting/updating all cells in the row.
     *
     * Direct Fields: Populate the field on the entity. Compound Fields (address): Get the address object and populate
     * the field on the address. To-One Associations: Get the association object and populate the internal ID field.
     * To-Many Associations: Call the association REST method (unless we are loading notes)
     */
    private void handleFields() throws Exception {
        for (Field field : record.getFields()) {
            if (field.isToMany()) {
                if (entityInfo == EntityInfo.NOTE) {
                    prepopulateAssociation(field);
                }
            } else if (field.isToOne()) {
                if (!field.getStringValue().isEmpty()) {
                    B toOneEntity = findToOneEntity(field);
                    field.populateAssociationOnEntity(entity, toOneEntity);
                }
            } else {
                field.populateFieldOnEntity(entity);
            }
        }
    }

    private B findToOneEntity(Field field) {
        List<B> list;
        if (field.getFieldEntity().isSearchEntity()) {
            list = searchForToOne(field);
        } else {
            list = queryForToOne(field);
        }
        validateListFromRestCall(field.getCell().getName(), list, field.getStringValue());
        return list.get(0);
    }

    @SuppressWarnings("unchecked")
    private <S extends SearchEntity> List<B> searchForToOne(Field field) {
        String filter = getQueryStatement(field.getName(), field.getStringValue(), field.getFieldType(),
            field.getFieldEntity());
        if (field.getFieldEntity().isSoftDeletable()) {
            filter += " AND " + StringConsts.IS_DELETED + ":" + field.getFieldEntity().getSearchIsDeletedValue(false);
        }
        return (List<B>) restApi.searchForList((Class<S>) field.getFieldEntity().getEntityClass(), filter,
            Sets.newHashSet(StringConsts.ID), ParamFactory.searchParams());
    }

    @SuppressWarnings("unchecked")
    private <Q extends QueryEntity> List<B> queryForToOne(Field field) {
        Set<String> fieldsToReturn = Sets.newHashSet(StringConsts.ID);
        String filter = getWhereStatement(field.getName(), field.getStringValue(), field.getFieldType());
        if (field.getFieldEntity() == EntityInfo.PERSON) {
            fieldsToReturn.add(StringConsts.IS_DELETED);
        } else if (field.getFieldEntity().isSoftDeletable()) {
            filter += " AND " + StringConsts.IS_DELETED + "=false";
        }

        List<B> list = (List<B>) restApi.queryForList((Class<Q>) field.getFieldEntity().getEntityClass(), filter,
            fieldsToReturn, ParamFactory.queryParams());

        if (field.getFieldEntity() == EntityInfo.PERSON) {
            list = list.stream().filter(this::isPersonActive).collect(Collectors.toList());
        }

        return list;
    }

    /**
     * Used to determine if a person entity is truly soft-deleted or not. CorporateUser persons when disabled have their
     * deleted flag set to true, but they should always be considered active.
     *
     * @return true if the entity is not soft-deleted
     */
    private Boolean isPersonActive(B entity) {
        Boolean active = true;
        if (entity.getClass() == Person.class) {
            Person person = (Person) entity;
            active = person.getPersonSubtype().equals(StringConsts.CORPORATE_USER) || !person.getIsDeleted();
        }
        return active;
    }

    private void validateListFromRestCall(String field, List<B> list, String value) {
        if (list == null || list.isEmpty()) {
            throw new RestApiException("Cannot find To-One Association: '" + field + "' with value: '" + value + "'");
        } else if (list.size() > 1) {
            throw new RestApiException("Found " + list.size() + " duplicate To-One Associations: '" + field + "' with value: '" + value + "'");
        }
    }

    /**
     * Populates a given To-Many field for an entity before the entity has been created.
     *
     * @param field the To-Many field to populate the entity with
     */
    private void prepopulateAssociation(Field field) throws IllegalAccessException, InvocationTargetException, ParseException {
        List<B> associations = findAssociations(field);
        for (B association : associations) {
            field.populateAssociationOnEntity(entity, association);
        }
    }

    /**
     * Makes association REST calls for all To-Many relationships for the entity after the entity has been created.
     */
    @SuppressWarnings("unchecked")
    private void createAssociations() throws IllegalAccessException, InvocationTargetException {
        // Note associations are filled out in the create call
        if (entityInfo == EntityInfo.NOTE) {
            return;
        }

        for (Field field : record.getToManyFields()) {
            AssociationField associationField = AssociationUtil.getToManyField(field);
            List<B> associations = findAssociations(field);

            // Filter out any existing associations, down to only new association IDs
            List<B> existingAssociations = restApi.getAllAssociationsList((Class<AssociationEntity>) entityInfo.getEntityClass(),
                Sets.newHashSet(entityId), associationField, Sets.newHashSet(StringConsts.ID), ParamFactory.associationParams());
            List<Integer> associationIds = associations.stream().map(BullhornEntity::getId).collect(Collectors.toList());
            List<Integer> existingIDs = existingAssociations.stream().map(BullhornEntity::getId).collect(Collectors.toList());
            List<Integer> addAssociations = associationIds.stream().filter(id -> !existingIDs.contains(id)).collect(Collectors.toList());
            List<Integer> removeAssociations = existingIDs.stream().filter(id -> !associationIds.contains(id)).collect(Collectors.toList());

            // Add the new associations to the entity
            if (!addAssociations.isEmpty()) {
                restApi.associateWithEntity((Class<AssociationEntity>) entityInfo.getEntityClass(), entityId,
                    associationField, Sets.newHashSet(addAssociations));
            }

            // Remove old associations from the entity
            if (!removeAssociations.isEmpty()) {
                restApi.disassociateWithEntity((Class<AssociationEntity>) entityInfo.getEntityClass(), entityId,
                    associationField, Sets.newHashSet(removeAssociations));
            }
        }
    }

    /**
     * Returns the list of new entities with ID that match the search criteria in the given To-Many field.
     *
     * Given a field like: 'primarySkills' with a value like: 'Skill1;Skill2;Skill3;Skill4', this method will return the
     * list of Skill objects (in this case, four skills) with their ID field filled out from REST, one object per value.
     * If any of these associations do not exist in rest, or are duplicated in rest, an error is thrown, stopping the
     * task from proceeding any further.
     */
    private List<B> findAssociations(Field field) throws InvocationTargetException, IllegalAccessException {
        List<B> associations = Lists.newArrayList();
        if (!field.getStringValue().isEmpty()) {
            Set<String> values = Sets.newHashSet(field.getStringValue().split(propertyFileUtil.getListDelimiter()));
            associations = doFindAssociations(field);
            if (associations.size() != values.size()) {
                Set<String> existingAssociationValues = getFieldValueSet(field, associations);
                if (associations.size() > values.size()) {
                    String duplicates = existingAssociationValues.stream().map(n -> "\t" + n)
                        .collect(Collectors.joining("\n"));
                    throw new RestApiException("Found " + associations.size()
                        + " duplicate To-Many Associations: '" + field.getCell().getName()
                        + "' with value:\n" + duplicates);
                } else {
                    String missingAssociations = values.stream().filter(n -> !existingAssociationValues.contains(n))
                        .map(n -> "\t" + n).collect(Collectors.joining("\n"));
                    throw new RestApiException("Error occurred: " + field.getCell().getAssociationBaseName()
                        + " does not exist with " + field.getName() + " of the following values:\n" + missingAssociations);
                }
            }
        }
        return associations;
    }

    /**
     * Makes the lookup call to check that all associated values are present, and there are no duplicates. This will
     * work with up to 500 associated records, such as candidates or businessSectors. It will perform the lookup using
     * the field given after the period, like: 'businessSector.name' or 'candidate.id'
     *
     * @param field the To-Many association field to lookup records for
     */
    @SuppressWarnings("unchecked")
    private <Q extends QueryEntity, S extends SearchEntity> List<B> doFindAssociations(Field field) {
        List<B> list;
        if (field.getFieldEntity().isSearchEntity()) {
            List<String> values = Arrays.asList(field.getStringValue().split(propertyFileUtil.getListDelimiter()));
            String filter = values.stream().map(n -> getQueryStatement(field.getName(), n, field.getFieldType(),
                field.getFieldEntity())).collect(Collectors.joining(" OR "));
            if (field.getFieldEntity().isSoftDeletable()) {
                filter = "(" + filter + ") AND " + StringConsts.IS_DELETED + ":"
                    + field.getFieldEntity().getSearchIsDeletedValue(false);
            }
            list = (List<B>) restApi.searchForList((Class<S>) field.getFieldEntity().getEntityClass(), filter, null, ParamFactory.searchParams());
        } else {
            List<String> values = Arrays.asList(field.getStringValue().split(propertyFileUtil.getListDelimiter()));
            String filter = values.stream().map(n -> getWhereStatement(field.getName(), n, field.getFieldType()))
                .collect(Collectors.joining(" OR "));
            if (field.getFieldEntity().isSoftDeletable()) {
                filter = "(" + filter + ") AND " + StringConsts.IS_DELETED + "=false";
            }
            list = (List<B>) restApi.queryForList((Class<Q>) field.getFieldEntity().getEntityClass(), filter, null, ParamFactory.queryParams());
        }
        return list;
    }

    /**
     * Given a list of entity objects, this returns the non-duplicate set of all field values.
     */
    private Set<String> getFieldValueSet(Field field, List<B> entities) throws InvocationTargetException, IllegalAccessException {
        Set<String> values = new HashSet<>();
        for (B entity : entities) {
            values.add(field.getValueFromEntity(entity).toString());
        }
        return values;
    }

    /**
     * Sets a locator for the client contact created by default for a new ClientCorporation.
     *
     * Special case that handles setting the externalID of the defaultContact that is created on a ClientCorporation.
     * This allows for easily finding that contact later using the externalID, which will be of the format:
     * `defaultContact1234`, where 1234 is the externalID that was set on the parent ClientCorporation.
     */
    @SuppressWarnings("unchecked")
    private void postProcessEntityInsert(Integer entityId) {
        if (entity.getClass() == ClientCorporation.class) {
            List<ClientCorporation> clientCorporations = restApi.queryForList(ClientCorporation.class,
                "id=" + entityId.toString(), Sets.newHashSet(StringConsts.ID, StringConsts.EXTERNAL_ID), ParamFactory.queryParams());
            if (!clientCorporations.isEmpty()) {
                ClientCorporation clientCorporation = clientCorporations.get(0);
                if (StringUtils.isNotBlank(clientCorporation.getExternalID())) {
                    final String query = "clientCorporation.id=" + clientCorporation.getId() + " AND status='Archive'";
                    List<ClientContact> clientContacts = restApi.queryForList(ClientContact.class, query,
                        Sets.newHashSet(StringConsts.ID), ParamFactory.queryParams());
                    if (!clientContacts.isEmpty()) {
                        ClientContact clientContact = clientContacts.get(0);
                        String defaultContactExternalId = "defaultContact" + clientCorporation.getExternalID();
                        clientContact.setExternalID(defaultContactExternalId);
                        restApi.updateEntity(clientContact);
                    }
                }
            }
        }
    }

    /**
     * Inserts the HTML contents of converted resumes into the description field.
     *
     * Handles setting the description field of an entity (if one exists) to the previously converted HTML resume file
     * or description file stored on disk, if a convertAttachments has been done previously. This only works if there is
     * an externalID being used in the input file.
     */
    private void insertAttachmentToDescription() throws IOException, InvocationTargetException, IllegalAccessException {
        String descriptionMethod = MethodUtil.findBestMatch(entityInfo.getSetterMethodMap().keySet(), StringConsts.DESCRIPTION);
        if (descriptionMethod != null && row.hasValue(StringConsts.EXTERNAL_ID)) {
            String convertedAttachmentFilepath = propertyFileUtil.getConvertedAttachmentFilepath(entityInfo,
                row.getValue(StringConsts.EXTERNAL_ID));
            if (convertedAttachmentFilepath != null) {
                File convertedAttachmentFile = new File(convertedAttachmentFilepath);
                if (convertedAttachmentFile.exists()) {
                    String description = FileUtils.readFileToString(convertedAttachmentFile);
                    entityInfo.getSetterMethodMap().get(descriptionMethod).invoke(entity, description);
                }
            }
        }
    }
}

package com.bullhorn.dataloader.task;

import com.bullhorn.dataloader.data.ActionTotals;
import com.bullhorn.dataloader.data.CsvFileWriter;
import com.bullhorn.dataloader.data.Result;
import com.bullhorn.dataloader.data.Row;
import com.bullhorn.dataloader.enums.EntityInfo;
import com.bullhorn.dataloader.rest.Cache;
import com.bullhorn.dataloader.rest.CompleteUtil;
import com.bullhorn.dataloader.rest.Field;
import com.bullhorn.dataloader.rest.Record;
import com.bullhorn.dataloader.rest.RestApi;
import com.bullhorn.dataloader.util.AssociationUtil;
import com.bullhorn.dataloader.util.FindUtil;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Handles converting a row of CSV data into REST calls to either insert or update a record in Bullhorn.
 */
public class LoadTask extends AbstractTask {
    private BullhornEntity entity;
    private boolean isNewEntity = true;
    private Record record;

    public LoadTask(EntityInfo entityInfo,
                    Row row,
                    CsvFileWriter csvFileWriter,
                    PropertyFileUtil propertyFileUtil,
                    RestApi restApi,
                    PrintUtil printUtil,
                    ActionTotals actionTotals,
                    Cache cache, CompleteUtil completeUtil) {
        super(entityInfo, row, csvFileWriter, propertyFileUtil, restApi, printUtil, actionTotals, cache, completeUtil);
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
    private void getOrCreateEntity() throws IllegalAccessException, InstantiationException {
        List<BullhornEntity> foundEntityList = findEntities(record.getEntityExistFields(), Sets.newHashSet(StringConsts.ID), true);
        if (foundEntityList.isEmpty()) {
            entity = (BullhornEntity) entityInfo.getEntityClass().newInstance();
        } else if (foundEntityList.size() > 1) {
            throw new RestApiException(FindUtil.getMultipleRecordsExistMessage(entityInfo, record.getEntityExistFields(), foundEntityList.size()));
        } else {
            entity = foundEntityList.get(0);
            entityId = entity.getId();
            isNewEntity = false;
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
                    BullhornEntity toOneEntity = findToOneEntity(field);
                    field.populateAssociationOnEntity(entity, toOneEntity);
                }
            } else {
                field.populateFieldOnEntity(entity);
            }
        }
    }

    /**
     * Right now, we are only allowing a single field search for to-one entities
     *
     * @param field the field to use to search for an existing entity
     * @return The entity if found, throws a RestApiException if not found
     */
    private BullhornEntity findToOneEntity(Field field) {
        List<BullhornEntity> entities = findActiveEntities(Lists.newArrayList(field), Sets.newHashSet(StringConsts.ID), false);

        // Throw error if to-one entity does not exist or too many exist
        if (entities == null || entities.isEmpty()) {
            throw new RestApiException("Cannot find To-One Association: '" + field.getCell().getName()
                + "' with value: '" + field.getStringValue() + "'");
        } else if (entities.size() > 1) {
            throw new RestApiException("Found " + entities.size() + " duplicate To-One Associations: '" + field.getCell().getName()
                + "' with value: '" + field.getStringValue() + "'");
        }
        return entities.get(0);
    }

    /**
     * Populates a given To-Many field for an entity before the entity has been created.
     *
     * Populates the To-Many in SDK-REST, in a fashion that only works for Notes currently. Eventually, convert everything
     * over to pre-populating To-Many fields in the correct "replace-all" way, so that our association calls go away.
     *
     * @param field the To-Many field to populate the entity with
     */
    private void prepopulateAssociation(Field field) throws IllegalAccessException, InvocationTargetException, ParseException {
        List<BullhornEntity> associations = findAssociations(field);
        for (BullhornEntity association : associations) {
            field.populateAssociationOnEntity(entity, association);
        }
    }

    /**
     * Makes association REST calls for all To-Many relationships for the entity after the entity has been created.
     *
     * TODO: remove this method once prepopulating associations can be used for all entities
     */
    @SuppressWarnings("unchecked")
    private void createAssociations() throws IllegalAccessException, InvocationTargetException {
        // Note associations are filled out in the create call
        if (entityInfo == EntityInfo.NOTE) {
            return;
        }

        for (Field field : record.getToManyFields()) {
            AssociationField associationField = AssociationUtil.getToManyField(field);
            List<BullhornEntity> associations = findAssociations(field);

            // Filter out any existing associations, down to only new association IDs
            List<BullhornEntity> existingAssociations = restApi.getAllAssociationsList((Class<AssociationEntity>) entityInfo.getEntityClass(),
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
    private List<BullhornEntity> findAssociations(Field field) throws InvocationTargetException, IllegalAccessException {
        List<BullhornEntity> associations = Lists.newArrayList();
        if (!field.getStringValue().isEmpty()) {
            associations = findActiveEntities(Lists.newArrayList(field), Sets.newHashSet(StringConsts.ID), false);

            // Only for strict one-to-one lookups, throw error if we did not receive exactly the same number of associations as requested values
            List<String> values = field.split(propertyFileUtil.getListDelimiter());
            if (!propertyFileUtil.getWildcardMatching() && associations.size() != values.size()) {
                Set<String> existingAssociationValues = new HashSet<>();
                for (BullhornEntity entity : associations) {
                    String value = field.getStringValueFromEntity(entity, propertyFileUtil.getListDelimiter());
                    existingAssociationValues.add(value);
                }
                if (associations.size() > values.size()) {
                    String duplicates = existingAssociationValues.stream().map(n -> "\t" + n).collect(Collectors.joining("\n"));
                    throw new RestApiException("Found " + associations.size()
                        + " duplicate To-Many Associations: '" + field.getCell().getName() + "' with value:\n" + duplicates);
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
     * Sets a locator for the client contact created by default for a new ClientCorporation.
     *
     * Special case that handles setting the externalID of the defaultContact that is created on a ClientCorporation.
     * This allows for easily finding that contact later using the externalID, which will be of the format:
     * `defaultContact1234`, where 1234 is the externalID that was set on the parent ClientCorporation.
     */
    private void postProcessEntityInsert(Integer entityId) {
        if (entity.getClass() == ClientCorporation.class) {
            List<ClientCorporation> clientCorporations = restApi.queryForList(ClientCorporation.class,
                "id=" + entityId.toString(), Sets.newHashSet(StringConsts.ID, StringConsts.EXTERNAL_ID), ParamFactory.queryParams());
            if (!clientCorporations.isEmpty()) {
                ClientCorporation clientCorporation = clientCorporations.get(0);
                if (StringUtils.isNotBlank(clientCorporation.getExternalID())) {
                    final String filter = "clientCorporation.id=" + clientCorporation.getId() + " AND status='Archive'";
                    List<ClientContact> clientContacts = restApi.queryForList(ClientContact.class, filter,
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

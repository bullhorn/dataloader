package com.bullhorn.dataloader.task;

import com.bullhorn.dataloader.enums.Command;
import com.bullhorn.dataloader.enums.EntityInfo;
import com.bullhorn.dataloader.service.csv.CsvFileWriter;
import com.bullhorn.dataloader.service.csv.Result;
import com.bullhorn.dataloader.service.executor.BullhornRestApi;
import com.bullhorn.dataloader.util.ActionTotals;
import com.bullhorn.dataloader.util.AssociationUtil;
import com.bullhorn.dataloader.util.PrintUtil;
import com.bullhorn.dataloader.util.PropertyFileUtil;
import com.bullhorn.dataloader.util.StringConsts;
import com.bullhornsdk.data.exception.RestApiException;
import com.bullhornsdk.data.model.entity.association.AssociationField;
import com.bullhornsdk.data.model.entity.association.EntityAssociations;
import com.bullhornsdk.data.model.entity.core.standard.Candidate;
import com.bullhornsdk.data.model.entity.core.standard.ClientContact;
import com.bullhornsdk.data.model.entity.core.standard.ClientCorporation;
import com.bullhornsdk.data.model.entity.core.standard.JobOrder;
import com.bullhornsdk.data.model.entity.core.standard.Lead;
import com.bullhornsdk.data.model.entity.core.standard.Note;
import com.bullhornsdk.data.model.entity.core.standard.NoteEntity;
import com.bullhornsdk.data.model.entity.core.standard.Opportunity;
import com.bullhornsdk.data.model.entity.core.standard.Placement;
import com.bullhornsdk.data.model.entity.core.type.AssociationEntity;
import com.bullhornsdk.data.model.entity.core.type.BullhornEntity;
import com.bullhornsdk.data.model.entity.core.type.CreateEntity;
import com.bullhornsdk.data.model.entity.core.type.QueryEntity;
import com.bullhornsdk.data.model.entity.core.type.SearchEntity;
import com.bullhornsdk.data.model.entity.core.type.UpdateEntity;
import com.bullhornsdk.data.model.entity.embedded.Address;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class LoadTask<A extends AssociationEntity, E extends EntityAssociations, B extends BullhornEntity> extends AbstractTask<A, E, B> {
    protected B entity;
    protected Integer entityID;
    protected Map<String, Method> methodMap;
    private Map<String, Integer> countryNameToIdMap;
    private Map<String, AssociationField> associationMap = new HashMap<>();
    private Map<String, Address> addressMap = new HashMap<>();
    protected boolean isNewEntity = true;

    public LoadTask(Command command,
                    Integer rowNumber,
                    EntityInfo entityInfo,
                    Map<String, String> dataMap,
                    Map<String, Method> methodMap,
                    Map<String, Integer> countryNameToIdMap,
                    CsvFileWriter csvWriter,
                    PropertyFileUtil propertyFileUtil,
                    BullhornRestApi bullhornRestApi,
                    PrintUtil printUtil,
                    ActionTotals actionTotals) {
        super(command, rowNumber, entityInfo, dataMap, csvWriter, propertyFileUtil, bullhornRestApi, printUtil, actionTotals);
        this.methodMap = methodMap;
        this.countryNameToIdMap = countryNameToIdMap;
    }

    @Override
    public void run() {
        Result result;
        try {
            result = handle();
        } catch (Exception e) {
            result = handleFailure(e, entityID);
        }
        writeToResultCSV(result);
    }

    protected Result handle() throws Exception {
        createEntityObject();
        handleData();
        insertAttachmentToDescription();
        insertOrUpdateEntity();
        createNewAssociations();
        return createResult();
    }

    protected void insertAttachmentToDescription() throws IOException, InvocationTargetException, IllegalAccessException {
        String descriptionMethod = getDescriptionMethod();
        if (!"".equals(descriptionMethod)) {
            String attachmentFilePath = getAttachmentFilePath(entityInfo.getEntityName(), dataMap.get("externalID"));
            File convertedAttachment = new File(attachmentFilePath);
            if (convertedAttachment.exists()) {
                String description = FileUtils.readFileToString(convertedAttachment);
                methodMap.get(descriptionMethod).invoke(entity, description);
            }
        }
    }

    protected String getDescriptionMethod() {
        List<String> descriptionMethods = methodMap.keySet().stream().filter(n -> n.contains(StringConsts.DESCRIPTION)).collect(Collectors.toList());
        if (descriptionMethods.size() > 0) {
            if (descriptionMethods.indexOf(StringConsts.DESCRIPTION) > -1) {
                return StringConsts.DESCRIPTION;
            } else {
                return descriptionMethods.get(0);
            }
        } else {
            return "";
        }
    }

    protected String getAttachmentFilePath(String entityName, String externalID) {
        return "convertedAttachments/" + entityName + "/" + externalID + ".html";
    }

    protected Result createResult() {
        if (isNewEntity) {
            return Result.Insert(entityID);
        } else {
            return Result.Update(entityID);
        }
    }

    protected void createEntityObject() throws IOException, IllegalAccessException, InstantiationException {
        Map<String, String> entityExistFieldsMap = getEntityExistFieldsMap();
        List<B> existingEntityList = findEntityList(entityExistFieldsMap);
        if (!existingEntityList.isEmpty()) {
            if (existingEntityList.size() > 1) {
                throw new RestApiException("Row " + rowNumber + ": Cannot Perform Update - Multiple Records Exist. Found " +
                    existingEntityList.size() + " " + entityInfo.getEntityName() +
                    " records with the same ExistField criteria of: " + getEntityExistFieldsMap());
            } else {
                isNewEntity = false;
                entity = existingEntityList.get(0);
                entityID = entity.getId();
            }
        } else {
            entity = (B)entityInfo.getEntityClass().newInstance();
        }
    }

    protected void insertOrUpdateEntity() throws IOException {
        if (isNewEntity) {
            try {
                CrudResponse response = bullhornRestApi.insertEntity((CreateEntity) entity);
                checkForRestSdkErrorMessages(response);
                entityID = response.getChangedEntityId();
                entity.setId(entityID);
                if (entity.getClass() == ClientCorporation.class) {
                    setDefaultContactExternalId(entityID);
                }
            } catch (RestApiException e) {
                checkForRequiredFieldsError(e);
            }
        } else {
            CrudResponse response = bullhornRestApi.updateEntity((UpdateEntity) entity);
            checkForRestSdkErrorMessages(response);
        }
    }

    protected void setDefaultContactExternalId(Integer entityID) {
        List<ClientCorporation> clientCorporations = (List<ClientCorporation>) queryForEntity("id", entityID.toString(), Integer.class, (Class<B>) ClientCorporation.class, Sets.newHashSet("id", "externalID"));
        if (!clientCorporations.isEmpty()) {
            ClientCorporation clientCorporation = clientCorporations.get(0);
            if (StringUtils.isNotBlank(clientCorporation.getExternalID())) {
                final String query = "clientCorporation.id=" + clientCorporation.getId() + " AND status='Archive'";
                List<ClientContact> clientContacts = bullhornRestApi.query(ClientContact.class, query, Sets.newHashSet("id"), ParamFactory.queryParams()).getData();
                if (!clientContacts.isEmpty()) {
                    ClientContact clientContact = clientContacts.get(0);
                    String defaultContactExternalId = "defaultContact" + clientCorporation.getExternalID();
                    clientContact.setExternalID(defaultContactExternalId);
                    CrudResponse response = bullhornRestApi.updateEntity(clientContact);
                    checkForRestSdkErrorMessages(response);
                }
            }
        }
    }

    protected void handleData() throws Exception {
        for (String field : dataMap.keySet()) {
            if (validField(field)) {
                if (field.contains(".")) {
                    handleAssociations(field);
                } else {
                    populateFieldOnEntity(field);
                }
            }
        }
        for (String addressField : addressMap.keySet()) {
            methodMap.get(addressField.toLowerCase()).invoke(entity, addressMap.get(addressField));
        }
    }

    protected boolean validField(String field) {
        if (!isNewEntity) {
            return !"username".equalsIgnoreCase(field);
        }
        return true;
    }

    protected void populateFieldOnEntity(String field) throws ParseException, InvocationTargetException, IllegalAccessException {
        populateFieldOnEntity(field, dataMap.get(field), entity, methodMap);
    }

    protected void handleAssociations(String field) throws InvocationTargetException, IllegalAccessException, Exception {
        boolean isOneToMany = verifyIfOneToMany(field);
        if (!isOneToMany) {
            handleOneToOne(field);
        }
    }

    private <S extends SearchEntity> void handleOneToOne(String field) throws InvocationTargetException, IllegalAccessException, RestApiException {
        String toOneEntityName = field.substring(0, field.indexOf("."));
        String fieldName = field.substring(field.indexOf(".") + 1, field.length());

        if (toOneEntityName.toLowerCase().contains("address")) {
            handleAddress(toOneEntityName, field, fieldName);
        } else {
            Method method = methodMap.get(toOneEntityName.toLowerCase());
            if (method == null) {
                throw new RestApiException("Row " + rowNumber + ": To-One Association: '" + toOneEntityName + "' does not exist on " + entity.getClass().getSimpleName());
            }

            Class<B> toOneEntityClass = (Class<B>) method.getParameterTypes()[0];
            B toOneEntity = getToOneEntity(field, fieldName, toOneEntityClass);
            method.invoke(entity, toOneEntity);
        }
    }

    protected B getToOneEntity(String field, String fieldName, Class<B> toOneEntityClass) {
        Class fieldType = getFieldType(toOneEntityClass, field, fieldName);
        return findEntity(field, fieldName, toOneEntityClass, fieldType);
    }

    protected B findEntity(String field, String fieldName, Class<B> toOneEntityClass, Class fieldType) {
        List<B> list;
        String value = dataMap.get(field);

        if (SearchEntity.class.isAssignableFrom(toOneEntityClass)) {
            list = searchForEntity(fieldName, value, fieldType, toOneEntityClass, null);
        } else {
            list = queryForEntity(fieldName, value, fieldType, toOneEntityClass, null);
        }

        validateListFromRestCall(field, list, value);

        return list.get(0);
    }

    protected void validateListFromRestCall(String field, List<B> list, String value) {
        if (list == null || list.isEmpty()) {
            throw new RestApiException("Row " + rowNumber + ": Cannot find To-One Association: '" + field + "' with value: '" + value + "'");
        } else if (list.size() > 1) {
            throw new RestApiException("Row " + rowNumber + ": Found " + list.size() + " duplicate To-One Associations: '" + field + "' with value: '" + value + "'");
        }
    }

    private void handleAddress(String toOneEntityName, String field, String fieldName) throws InvocationTargetException, IllegalAccessException {
        if (!addressMap.containsKey(toOneEntityName)) {
            addressMap.put(toOneEntityName, new Address());
        }
        if (fieldName.contains("country")) {
            // Allow for the use of a country name or internal Bullhorn ID
            if (countryNameToIdMap.containsKey(dataMap.get(field))) {
                methodMap.get("countryid").invoke(addressMap.get(toOneEntityName), countryNameToIdMap.get(dataMap.get(field)));
            } else {
                methodMap.get("countryid").invoke(addressMap.get(toOneEntityName), Integer.valueOf(dataMap.get(field)));
            }
        } else {
            Method method = methodMap.get(fieldName);
            if (method == null) {
                throw new RestApiException("Row " + rowNumber + ": Invalid field: '" + field + "' - '" + fieldName + "' does not exist on the Address object");
            }

            method.invoke(addressMap.get(toOneEntityName), dataMap.get(field));
        }
    }

    private boolean verifyIfOneToMany(String field) {
        List<AssociationField<AssociationEntity, BullhornEntity>> associationFieldList = AssociationUtil.getAssociationFields((Class<AssociationEntity>) entityInfo.getEntityClass());
        for (AssociationField associationField : associationFieldList) {
            if (associationField.getAssociationFieldName().equalsIgnoreCase(field.substring(0, field.indexOf(".")))) {
                associationMap.put(field, associationField);
                return true;
            }
        }
        return false;
    }

    private void createNewAssociations() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        for (String associationName : associationMap.keySet()) {
            if (dataMap.get(associationName) != null && !dataMap.get(associationName).equals("")) {
                addAssociationToEntity(associationName, associationMap.get(associationName));
            }
        }
    }

    protected void addAssociationToEntity(String field, AssociationField associationField) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        List<Integer> newAssociationIdList = getNewAssociationIdList(field, associationField);
        try {
            if (entityInfo == EntityInfo.NOTE) {
                addAssociationsToNote((Note) entity, associationField.getAssociationType(), newAssociationIdList);
            } else {
                bullhornRestApi.associateWithEntity((Class<A>) entityInfo.getEntityClass(), entityID, associationField, Sets.newHashSet(newAssociationIdList));
            }
        } catch (RestApiException e) {
            // Provide a simpler duplication error message with all of the essential data
            if (e.getMessage().contains("an association between " + entityInfo.getEntityName()) && e.getMessage().contains(entityID + " and " + associationField.getAssociationType().getSimpleName() + " ")) {
                printUtil.log(Level.INFO, "Association from " + entityInfo.getEntityName() + " entity " + entityID + " to " + associationField.getAssociationType().getSimpleName() + " entities " + newAssociationIdList.toString() + " already exists.");
            } else {
                throw e;
            }
        }
    }

    protected void addAssociationsToNote(Note note, Class type, List<Integer> associationIdList) {
        for (Integer associationId : associationIdList) {
            try {
                if (Candidate.class.equals(type) || ClientContact.class.equals(type) || Lead.class.equals(type)) {
                    addNoteEntity(note, "User", associationId);
                } else if (JobOrder.class.equals(type)) {
                    addNoteEntity(note, "JobPosting", associationId);
                } else if (Opportunity.class.equals(type)) {
                    addNoteEntity(note, "Opportunity", associationId);
                } else if (Placement.class.equals(type)) {
                    addNoteEntity(note, "Placement", associationId);
                }
            } catch (RestApiException e) {
                // Provide a simpler duplication error message with all of the essential data
                if (e.getMessage().contains("error persisting an entity of type: NoteEntity") && e.getMessage().contains("\"type\" : \"DUPLICATE_VALUE\"")) {
                    printUtil.log(Level.INFO, "Association from " + entityInfo.getEntityName() + " entity " + entityID + " to " + type.getSimpleName() + " entity " + associationId + " already exists.");
                } else {
                    throw e;
                }
            }
        }
    }

    protected void addNoteEntity(Note noteAdded, String targetEntityName, Integer targetEntityID) {
        NoteEntity noteEntity = new NoteEntity();
        noteEntity.setNote(noteAdded);
        noteEntity.setTargetEntityID(targetEntityID);
        noteEntity.setTargetEntityName(targetEntityName);
        bullhornRestApi.insertEntity(noteEntity);
    }

    protected List<Integer> getNewAssociationIdList(String field, AssociationField associationField) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        String associationName = field.substring(0, field.indexOf("."));
        String fieldName = field.substring(field.indexOf(".") + 1);

        Set<String> valueSet = Sets.newHashSet(dataMap.get(field).split(propertyFileUtil.getListDelimiter()));
        Method method = getGetMethod(associationField, fieldName);
        List<B> existingAssociations = getExistingAssociations(field, associationField, valueSet);

        if (existingAssociations.size() != valueSet.size()) {
            Set<String> existingAssociationSet = getExistingAssociationValues(method, existingAssociations);

            if (existingAssociations.size() > valueSet.size()) {
                String duplicateAssociations = existingAssociationSet.stream().map(n -> "\t" + n).collect(Collectors.joining("\n"));
                throw new RestApiException("Row " + rowNumber + ": Found " + existingAssociations.size() + " duplicate To-Many Associations: '" + field + "' with value:\n" + duplicateAssociations);
            } else {
                String missingAssociations = valueSet.stream().filter(n -> !existingAssociationSet.contains(n)).map(n -> "\t" + n).collect(Collectors.joining("\n"));
                throw new RestApiException("Row " + rowNumber + ": Error occurred: " + associationName + " does not exist with " + fieldName + " of the following values:\n" + missingAssociations);
            }
        }

        List<Integer> associationIdList = findIdsOfAssociations(valueSet, existingAssociations, method);
        return associationIdList;
    }

    private Set<String> getExistingAssociationValues(Method method, List<B> existingAssociations) {
        return existingAssociations.stream().map(n -> {
            try {
                return method.invoke(n).toString();
            } catch (Exception shouldNeverHappen) {
                return null;
            }
        }).collect(Collectors.toSet());
    }

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
    private <Q extends QueryEntity, S extends SearchEntity> List<B> getExistingAssociations(String field, AssociationField
        associationField, Set<String> valueSet) {
        Integer COUNT_PARAMETER = 500;
        List<B> list;
        Class<B> associationClass = associationField.getAssociationType();

        if (SearchEntity.class.isAssignableFrom(associationClass)) {
            String where = getQueryStatement(valueSet, field, associationClass);
            SearchParams searchParams = ParamFactory.searchParams();
            searchParams.setCount(COUNT_PARAMETER);
            list = (List<B>) bullhornRestApi.search((Class<S>) associationClass, where, null, searchParams).getData();
        } else {
            String where = getWhereStatement(valueSet, field, associationClass);
            QueryParams queryParams = ParamFactory.queryParams();
            queryParams.setCount(COUNT_PARAMETER);
            list = (List<B>) bullhornRestApi.query((Class<Q>) associationClass, where, null, queryParams).getData();
        }

        return list;
    }

    protected Method getGetMethod(AssociationField associationField, String associationName) throws NoSuchMethodException {
        String methodName = "get" + associationName.substring(0, 1).toUpperCase() + associationName.substring(1);
        try {
            return associationField.getAssociationType().getMethod(methodName);
        } catch (NoSuchMethodException e) {
            throw e;
        }
    }

    private String getQueryStatement(Set<String> valueSet, String field, Class<B> associationClass) {
        String fieldName = field.substring(field.indexOf(".") + 1, field.length());
        return valueSet.stream().map(n -> getQueryStatement(fieldName, n, getFieldType(associationClass, field, fieldName), associationClass)).collect(Collectors.joining(" OR "));
    }

    private String getWhereStatement(Set<String> valueSet, String field, Class<B> associationClass) {
        String fieldName = field.substring(field.indexOf(".") + 1, field.length());
        return valueSet.stream().map(n -> getWhereStatement(fieldName, n, getFieldType(associationClass, field, fieldName))).collect(Collectors.joining(" OR "));
    }
}

package com.bullhorn.dataloader.task;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;

import com.bullhorn.dataloader.consts.TaskConsts;
import com.bullhorn.dataloader.service.Command;
import com.bullhorn.dataloader.service.csv.CsvFileWriter;
import com.bullhorn.dataloader.service.csv.Result;
import com.bullhorn.dataloader.util.ActionTotals;
import com.bullhorn.dataloader.util.PrintUtil;
import com.bullhorn.dataloader.util.PropertyFileUtil;
import com.bullhornsdk.data.api.BullhornData;
import com.bullhornsdk.data.exception.RestApiException;
import com.bullhornsdk.data.model.entity.association.AssociationFactory;
import com.bullhornsdk.data.model.entity.association.AssociationField;
import com.bullhornsdk.data.model.entity.association.EntityAssociations;
import com.bullhornsdk.data.model.entity.core.standard.Candidate;
import com.bullhornsdk.data.model.entity.core.standard.Category;
import com.bullhornsdk.data.model.entity.core.standard.ClientContact;
import com.bullhornsdk.data.model.entity.core.standard.ClientCorporation;
import com.bullhornsdk.data.model.entity.core.standard.CorporateUser;
import com.bullhornsdk.data.model.entity.core.standard.JobOrder;
import com.bullhornsdk.data.model.entity.core.standard.Lead;
import com.bullhornsdk.data.model.entity.core.standard.Note;
import com.bullhornsdk.data.model.entity.core.standard.NoteEntity;
import com.bullhornsdk.data.model.entity.core.standard.Opportunity;
import com.bullhornsdk.data.model.entity.core.standard.Placement;
import com.bullhornsdk.data.model.entity.core.standard.Tearsheet;
import com.bullhornsdk.data.model.entity.core.type.AssociationEntity;
import com.bullhornsdk.data.model.entity.core.type.BullhornEntity;
import com.bullhornsdk.data.model.entity.core.type.CreateEntity;
import com.bullhornsdk.data.model.entity.core.type.QueryEntity;
import com.bullhornsdk.data.model.entity.core.type.SearchEntity;
import com.bullhornsdk.data.model.entity.core.type.UpdateEntity;
import com.bullhornsdk.data.model.entity.embedded.Address;
import com.bullhornsdk.data.model.parameter.standard.ParamFactory;
import com.bullhornsdk.data.model.response.crud.CrudResponse;
import com.google.common.collect.Sets;

public class LoadTask<A extends AssociationEntity, B extends BullhornEntity> extends AbstractTask<B> {
    static private Map<Class<AssociationEntity>, List<AssociationField<AssociationEntity, BullhornEntity>>> entityClassToAssociationsMap = new HashMap<>();

    protected B entity;
    protected Integer entityID;
    private Map<String, Method> methodMap;
    private Map<String, Integer> countryNameToIdMap;
    private Map<String, AssociationField> associationMap = new HashMap<>();
    private Map<String, Address> addressMap = new HashMap<>();
    private boolean isNewEntity = true;

    public LoadTask(Command command,
                    Integer rowNumber,
                    Class entityClass,
                    LinkedHashMap<String, String> dataMap,
                    Map<String, Method> methodMap,
                    Map<String, Integer> countryNameToIdMap,
                    CsvFileWriter csvWriter,
                    PropertyFileUtil propertyFileUtil,
                    BullhornData bullhornData,
                    PrintUtil printUtil,
                    ActionTotals actionTotals) {
        super(command, rowNumber, entityClass, dataMap, csvWriter, propertyFileUtil, bullhornData, printUtil, actionTotals);
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
            String attachmentFilePath = getAttachmentFilePath(entityClass.getSimpleName(), dataMap.get("externalID"));
            File convertedAttachment = new File(attachmentFilePath);
            if (convertedAttachment.exists()) {
                String description = FileUtils.readFileToString(convertedAttachment);
                methodMap.get(descriptionMethod).invoke(entity, description);
            }
        }
    }

    protected String getDescriptionMethod() {
        List<String> descriptionMethods = methodMap.keySet().stream().filter(n -> n.contains(TaskConsts.DESCRIPTION)).collect(Collectors.toList());
        if (descriptionMethods.size() > 0) {
            if (descriptionMethods.indexOf(TaskConsts.DESCRIPTION) > -1) {
                return TaskConsts.DESCRIPTION;
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

    private Result createResult() {
        if (isNewEntity) {
            return Result.Insert(entityID);
        } else {
            return Result.Update(entityID);
        }
    }

    private void createEntityObject() throws Exception {
        List<B> existingEntityList = findEntityList();
        if (!existingEntityList.isEmpty()) {
            if (existingEntityList.size() > 1) {
                throw new RestApiException("Row " + rowNumber + ": Cannot Perform Update - Multiple Records Exist. Found " +
                    existingEntityList.size() + " " + entityClass.getSimpleName() +
                    " records with the same ExistField criteria of: " + getEntityExistFieldsMap());
            } else {
                isNewEntity = false;
                entity = existingEntityList.get(0);
                entityID = entity.getId();
            }
        } else {
            entity = entityClass.newInstance();
        }
    }

    private void insertOrUpdateEntity() throws IOException {
        if (isNewEntity) {
            try {
                CrudResponse response = bullhornData.insertEntity((CreateEntity) entity);
                checkForRestSdkErrorMessages(response);
                entityID = response.getChangedEntityId();
                entity.setId(entityID);
            }
            catch (RestApiException e) {
                checkForRequiredFieldsError(e);
            }
        } else {
            CrudResponse response = bullhornData.updateEntity((UpdateEntity) entity);
            checkForRestSdkErrorMessages(response);
        }
    }

    private void handleData() throws InvocationTargetException, IllegalAccessException, ParseException {
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

    private boolean validField(String field) {
        if (!isNewEntity) {
            return !"username".equalsIgnoreCase(field);
        }
        return true;
    }

    private void populateFieldOnEntity(String field) throws ParseException, InvocationTargetException, IllegalAccessException {
        populateFieldOnEntity(field, dataMap.get(field), entity, methodMap);
    }

    private void handleAssociations(String field) throws InvocationTargetException, IllegalAccessException {
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

    private B getToOneEntity(String field, String fieldName, Class<B> toOneEntityClass) {
        Class fieldType = getFieldType(toOneEntityClass, fieldName);
        return findEntity(field, fieldName, toOneEntityClass, fieldType);
    }

    protected B findEntity(String field, String fieldName, Class<B> toOneEntityClass, Class fieldType) {
        List<B> list;
        String value = dataMap.get(field);

        if (SearchEntity.class.isAssignableFrom(toOneEntityClass)) {
            list = searchForEntity(fieldName, value, fieldType, toOneEntityClass);
        } else {
            list = queryForEntity(fieldName, value, fieldType, toOneEntityClass);
        }

        if (list == null || list.isEmpty()) {
            throw new RestApiException("Row " + rowNumber + ": Cannot find To-One Association: '" + field + "' with value: '" + value + "'");
        } else if (list.size() > 1) {
            throw new RestApiException("Row " + rowNumber + ": Found " + list.size() + " duplicate To-One Associations: '" + field + "' with value: '" + value + "'");
        }

        return list.get(0);
    }

    private void handleAddress(String toOneEntityName, String field, String fieldName) throws InvocationTargetException, IllegalAccessException {
        if (!addressMap.containsKey(toOneEntityName)) {
            addressMap.put(toOneEntityName, new Address());
        }
        if (fieldName.contains("country")) {
            methodMap.get("countryid").invoke(addressMap.get(toOneEntityName), countryNameToIdMap.get(dataMap.get(field)));
        } else {
            Method method = methodMap.get(fieldName);
            if (method == null) {
                throw new RestApiException("Row " + rowNumber + ": Invalid field: '" + field + "' - '" + fieldName + "' does not exist on the Address object");
            }

            method.invoke(addressMap.get(toOneEntityName), dataMap.get(field));
        }
    }

    private boolean verifyIfOneToMany(String field) {
        List<AssociationField<AssociationEntity, BullhornEntity>> associationFieldList = getAssociationFields((Class<AssociationEntity>) entityClass);
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
            if (dataMap.get(associationName) != null && dataMap.get(associationName) != "") {
                addAssociationToEntity(associationName, associationMap.get(associationName));
            }
        }
    }

    protected void addAssociationToEntity(String field, AssociationField associationField) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        List<Integer> newAssociationIdList = getNewAssociationIdList(field, associationField);
        for (Integer associationId : newAssociationIdList) {
            try {
                if (entityClass == Note.class) {
                    addAssociationToNote((Note) entity, associationField.getAssociationType(), associationId);
                } else {
                    bullhornData.associateWithEntity((Class<A>) entityClass, entityID, associationField, Sets.newHashSet(associationId));
                }
            } catch (RestApiException e) {
                if (!e.getMessage().contains("an association between " + entityClass.getSimpleName() + " " + entityID + " and " + associationField.getAssociationType().getSimpleName() + " " + associationId + " already exists")) {
                    throw e;
                }
            }
        }
    }

    protected void addAssociationToNote(Note note, Class type, Integer associationID) {
        if (Candidate.class.equals(type) || ClientContact.class.equals(type) || Lead.class.equals(type)) {
            addNoteEntity(note, "User", associationID);
        } else if (JobOrder.class.equals(type)) {
            addNoteEntity(note, "JobPosting", associationID);
        } else if (Opportunity.class.equals(type)) {
            addNoteEntity(note, "Opportunity", associationID);
        } else if (Placement.class.equals(type)) {
            addNoteEntity(note, "Placement", associationID);
        }
    }

    protected void addNoteEntity(Note noteAdded, String targetEntityName, Integer targetEntityID) {
        NoteEntity noteEntity = new NoteEntity();
        noteEntity.setNote(noteAdded);
        noteEntity.setTargetEntityID(targetEntityID);
        noteEntity.setTargetEntityName(targetEntityName);
        bullhornData.insertEntity(noteEntity);
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

    private <Q extends QueryEntity, S extends SearchEntity> List<B> getExistingAssociations(String field, AssociationField
        associationField, Set<String> valueSet) {
        List<B> list;
        Class<B> associationClass = associationField.getAssociationType();

        if (SearchEntity.class.isAssignableFrom(associationClass)) {
            String where = getQueryStatement(valueSet, field, associationClass);
            list = (List<B>) bullhornData.search((Class<S>) associationClass, where, null, ParamFactory.searchParams()).getData();
        } else {
            String where = getWhereStatement(valueSet, field, associationClass);
            list = (List<B>) bullhornData.query((Class<Q>) associationClass, where, null, ParamFactory.queryParams()).getData();
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
        return valueSet.stream().map(n -> getQueryStatement(fieldName, n, getFieldType(associationClass, fieldName))).collect(Collectors.joining(" OR "));
    }

    private String getWhereStatement(Set<String> valueSet, String field, Class<B> associationClass) {
        String fieldName = field.substring(field.indexOf(".") + 1, field.length());
        return valueSet.stream().map(n -> getWhereStatment(fieldName, n, getFieldType(associationClass, fieldName))).collect(Collectors.joining(" OR "));
    }

    private static synchronized List<AssociationField<AssociationEntity, BullhornEntity>> getAssociationFields(Class<AssociationEntity> entityClass) {
        try {
            if (entityClassToAssociationsMap.containsKey(entityClass)) {
                return entityClassToAssociationsMap.get(entityClass);
            } else {
                EntityAssociations entityAssociations = getEntityAssociations((Class<AssociationEntity>) entityClass);
                List<AssociationField<AssociationEntity, BullhornEntity>> associationFields = entityAssociations.allAssociations();
                entityClassToAssociationsMap.put((Class<AssociationEntity>) entityClass, associationFields);
                return associationFields;
            }
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    private static synchronized EntityAssociations getEntityAssociations(Class entityClass) {
        return (entityClass == Candidate.class ? AssociationFactory.candidateAssociations() :
            (entityClass == Category.class ? AssociationFactory.categoryAssociations() :
                (entityClass == ClientContact.class ? AssociationFactory.clientContactAssociations() :
                    (entityClass == ClientCorporation.class ? AssociationFactory.clientCorporationAssociations() :
                        (entityClass == CorporateUser.class ? AssociationFactory.corporateUserAssociations() :
                            (entityClass == JobOrder.class ? AssociationFactory.jobOrderAssociations() :
                                (entityClass == Note.class ? AssociationFactory.noteAssociations() :
                                    (entityClass == Placement.class ? AssociationFactory.placementAssociations() :
                                        (entityClass == Opportunity.class ? AssociationFactory.opportunityAssociations() :
                                            (entityClass == Lead.class ? AssociationFactory.leadAssociations() :
                                                entityClass == Tearsheet.class ? AssociationFactory.tearsheetAssociations() : null))))))))));
    }

}

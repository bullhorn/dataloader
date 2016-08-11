package com.bullhorn.dataloader.task;

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
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class LoadTask< A extends AssociationEntity, E extends EntityAssociations, B extends BullhornEntity> extends AbstractTask<B> {
    private static final Logger log = LogManager.getLogger(LoadTask.class);
    private Map<String, Method> methodMap;
    private Map<String, Integer> countryNameToIdMap;
    private Map<String, AssociationField> associationMap = new HashMap<>();
    private Map<String, Address> addressMap = new HashMap<>();
    protected B entity;
    private Integer entityID;
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
        } catch(Exception e){
            result = handleFailure(e, entityID);
        }
        writeToResultCSV(result);
    }

    private Result handle() throws Exception {
        createEntityObject();
        handleData();
        insertAttachmentToDescription();
        insertOrUpdateEntity();
        createNewAssociations();
        return createResult();
    }

    protected void insertAttachmentToDescription() throws IOException, InvocationTargetException, IllegalAccessException {
        String descriptionMethod = getDescriptionMethod();
        if (!"".equals(descriptionMethod)){
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
            }
            else {
                return descriptionMethods.get(0);
            }
        }
        else {
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
                throw new RestApiException("Cannot Perform Update - Multiple Records Exist. Found " +
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
            CrudResponse response = bullhornData.insertEntity((CreateEntity) entity);
            checkForRestSdkErrorMessages(response);
            entityID = response.getChangedEntityId();
        } else {
            CrudResponse response = bullhornData.updateEntity((UpdateEntity) entity);
            checkForRestSdkErrorMessages(response);
        }
    }

    private void handleData() throws InvocationTargetException, IllegalAccessException {
        for (String field : dataMap.keySet()){
            if (validField(field)) {
                if (field.contains(".")) {
                    handleAssociations(field);
                } else {
                    populateFieldOnEntity(field);
                }
            }
        }
        for (String addressField : addressMap.keySet()){
            methodMap.get(addressField.toLowerCase()).invoke(entity,addressMap.get(addressField));
        }
    }

    private boolean validField(String field) {
        if (!isNewEntity) {
            return !"username".equalsIgnoreCase(field);
        }
        return true;
    }

    private void populateFieldOnEntity(String field) {
        populateFieldOnEntity(field, dataMap.get(field), entity, methodMap);
    }

    private void handleAssociations(String field) throws InvocationTargetException, IllegalAccessException {
        boolean isOneToMany = verifyIfOneToMany(field);
        if (!isOneToMany) {
            handleOneToOne(field);
        }
    }

    private <S extends SearchEntity> void handleOneToOne(String field) throws InvocationTargetException, IllegalAccessException {
        String toOneEntityName = field.substring(0, field.indexOf("."));
        String fieldName = field.substring(field.indexOf(".") + 1, field.length());
        if (toOneEntityName.toLowerCase().contains("address")){
            handleAddress(toOneEntityName, field, fieldName);
        }
        else {
            Class<B> toOneEntityClass = (Class<B>) methodMap.get(toOneEntityName.toLowerCase()).getParameterTypes()[0];
            B toOneEntity = getToOneEntity(field, fieldName, toOneEntityClass);
            methodMap.get(toOneEntityName.toLowerCase()).invoke(entity, toOneEntity);
        }
    }

    private B getToOneEntity(String field, String fieldName, Class<B> toOneEntityClass) {
        Class fieldType = getFieldType(toOneEntityClass, fieldName);
        return findEntity(field, fieldName, toOneEntityClass, fieldType);
    }

    private B findEntity(String field, String fieldName, Class<B> toOneEntityClass, Class fieldType) {
        if (SearchEntity.class.isAssignableFrom(toOneEntityClass)){
            return searchForEntity(fieldName, dataMap.get(field), fieldType, toOneEntityClass).get(0);
        } else {
            return queryForEntity(fieldName, dataMap.get(field), fieldType, toOneEntityClass).get(0);
        }
    }

    private void handleAddress(String toOneEntityName, String field, String fieldName) throws InvocationTargetException, IllegalAccessException {
        if (!addressMap.containsKey(toOneEntityName)) {
            addressMap.put(toOneEntityName, new Address());
        }
        if (fieldName.contains("country")) {
            methodMap.get("countryid").invoke(addressMap.get(toOneEntityName), countryNameToIdMap.get(dataMap.get(field)));
        } else {
            methodMap.get(fieldName).invoke(addressMap.get(toOneEntityName), dataMap.get(field));
        }
    }

    private boolean verifyIfOneToMany(String field) {
        List<AssociationField<A, B>> associationFieldList = getAssociationFields();
        for (AssociationField associationField : associationFieldList){
            if (associationField.getAssociationFieldName().equalsIgnoreCase(field.substring(0,field.indexOf(".")))) {
                associationMap.put(field, associationField);
                return true;
            }
        }
        return false;
    }

    private void createNewAssociations() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        for (String associationName : associationMap.keySet()){
            if (dataMap.get(associationName) != null && dataMap.get(associationName) != "") {
                addAssociationToEntity(associationName, associationMap.get(associationName));
            }
        }
    }

    private void addAssociationToEntity(String field, AssociationField associationField) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        List<Integer> newAssociationIdList = getNewAssociationIdList(field, associationField);
        for (Integer associationId : newAssociationIdList) {
            try {
                bullhornData.associateWithEntity((Class<A>) entityClass, entityID, associationField, Sets.newHashSet(associationId));
            } catch(Exception e){
                if (!e.getMessage().contains("an association between " + entityClass.getSimpleName() + " " + entityID + " and " + associationField.getAssociationType().getSimpleName() + " " + associationId + " already exists")){
                    throw e;
                }
            }
        }
    }

    private List<Integer> getNewAssociationIdList(String field, AssociationField associationField) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        String associationName = field.substring(0, field.indexOf("."));
        String fieldName = field.substring(field.indexOf(".") + 1);

        Set<String> valueSet = Sets.newHashSet(dataMap.get(field).split(propertyFileUtil.getListDelimiter()));
        Method method = getGetMethod(associationField, fieldName);
        List<B> existingAssociations = getExistingAssociations(field, associationField, valueSet);
        if (existingAssociations.size()!=valueSet.size()){
            Set<String> existingAssociationValues = getExistingAssociationValues(method, existingAssociations);
            String missingAssociations = valueSet.stream().filter(n -> !existingAssociationValues.contains(n)).map(n -> "\t" + n).collect(Collectors.joining("\n"));
            throw new RestApiException("Error occurred: " + associationName + " does not exist with " + fieldName + " of the following values:\n" + missingAssociations);
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
        for (B association : existingAssociations){
            String returnedValue = String.valueOf(method.invoke(association));
            if (valueSet.contains(returnedValue)){
                associationIdList.add(association.getId());
            }
        }
        return associationIdList;
    }

    private <Q extends QueryEntity> List<B> getExistingAssociations(String field, AssociationField associationField, Set<String> valueSet) {
        Class<B> associationClass = associationField.getAssociationType();
        String where = getWhereStatement(valueSet, field, associationClass);
        return (List<B>) bullhornData.query((Class<Q>) associationClass, where, null, ParamFactory.queryParams()).getData();
    }

    private Method getGetMethod(AssociationField associationField, String associationName) throws NoSuchMethodException {
        String methodName = "get" + associationName.substring(0, 1).toUpperCase() + associationName.substring(1);
        try {
            return associationField.getAssociationType().getMethod(methodName);
        } catch (NoSuchMethodException e) {
            throw e;
        }
    }

    private String getWhereStatement(Set<String> valueSet, String field, Class<B> associationClass) {
        String fieldName = field.substring(field.indexOf(".") + 1, field.length());
        return valueSet.stream().map(n -> getWhereStatment(fieldName, n, getFieldType(associationClass, fieldName))).collect(Collectors.joining(" OR "));
    }

    private List<AssociationField<A, B>> getAssociationFields() {
        try {
            E entityAssociations = getEntityAssociations((Class<A>) entityClass);
            return entityAssociations.allAssociations();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    private E getEntityAssociations(Class<A> entityClass) {
        return (entityClass == Candidate.class? (E) AssociationFactory.candidateAssociations() :(entityClass == Category.class? (E) AssociationFactory.categoryAssociations() :(entityClass == ClientContact.class? (E) AssociationFactory.clientContactAssociations() :(entityClass == ClientCorporation.class? (E) AssociationFactory.clientCorporationAssociations() :(entityClass == CorporateUser.class? (E) AssociationFactory.corporateUserAssociations() :(entityClass == JobOrder.class? (E) AssociationFactory.jobOrderAssociations() :(entityClass == Note.class? (E) AssociationFactory.noteAssociations() :(entityClass == Placement.class? (E) AssociationFactory.placementAssociations() :(entityClass == Opportunity.class? (E) AssociationFactory.opportunityAssociations() :(entityClass == Lead.class? (E) AssociationFactory.leadAssociations() : entityClass == Tearsheet.class? (E) AssociationFactory.tearsheetAssociations() :null))))))))));
    }

}

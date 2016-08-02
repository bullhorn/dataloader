package com.bullhorn.dataloader.task;

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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.bullhorn.dataloader.service.Command;
import com.bullhorn.dataloader.service.csv.CsvFileWriter;
import com.bullhorn.dataloader.service.csv.Result;
import com.bullhorn.dataloader.util.ActionTotals;
import com.bullhorn.dataloader.util.PrintUtil;
import com.bullhorn.dataloader.util.PropertyFileUtil;
import com.bullhornsdk.data.api.BullhornData;
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
import com.bullhornsdk.data.model.entity.core.type.SearchEntity;
import com.bullhornsdk.data.model.entity.core.type.UpdateEntity;
import com.bullhornsdk.data.model.enums.BullhornEntityInfo;
import com.bullhornsdk.data.model.parameter.standard.ParamFactory;
import com.google.common.collect.Sets;

public class LoadTask< A extends AssociationEntity, E extends EntityAssociations, B extends BullhornEntity> extends AbstractTask<B> {
    private static final Logger log = LogManager.getLogger(LoadTask.class);
    private Map<String, Method> methodMap;
    private Map<String, AssociationField> associationMap = new HashMap<>();
    private B entity;
    private Integer entityID;
    private boolean isNewEntity = true;

    public LoadTask(Command command,
                    Integer rowNumber,
                    Class<B> entityClass,
                    LinkedHashMap<String, String> dataMap,
                    Map<String, Method> methodMap,
                    CsvFileWriter csvWriter,
                    PropertyFileUtil propertyFileUtil,
                    BullhornData bullhornData,
                    PrintUtil printUtil,
                    ActionTotals actionTotals) {
        super(command, rowNumber, entityClass, dataMap, csvWriter, propertyFileUtil, bullhornData, printUtil, actionTotals);
        this.methodMap = methodMap;
    }

    @Override
    public void run() {
        Result result;
        try {
            result = handle();
        } catch(Exception e){
            result = handleFailure(e);
        }
        writeToResultCSV(result);
    }

    private Result handle() throws Exception {
        createEntityObject();
        parseData();
        insertOrUpdateEntity();
        createNewAssociations();
        return Result.Insert(entityID);
    }

    private void createEntityObject() throws InstantiationException, IllegalAccessException {
        List<B> existingEntityList = searchForEntity();
        if (!existingEntityList.isEmpty()){
            isNewEntity = false;
            entity = existingEntityList.get(0);
            entityID = entity.getId();
        } else {
            entity = entityClass.newInstance();
        }
    }

    private void insertOrUpdateEntity() throws IOException {
        if (isNewEntity) {
            entityID = bullhornData.insertEntity((CreateEntity) entity).getChangedEntityId();
        } else {
            bullhornData.updateEntity((UpdateEntity) entity);
        }
    }

    private void parseData() throws InvocationTargetException, IllegalAccessException {
        for (String field : dataMap.keySet()){
            if (field.contains(".")) {
                handleAssociations(field);
            }
            else {
                populateFieldOnEntity(field);
            }
        }
    }



    private void populateFieldOnEntity(String field) {
        try {
            methodMap.get(field.toLowerCase()).invoke(entity,dataMap.get(field));
        } catch (Exception e) {
            printUtil.printAndLog(e.toString());
        }
    }

    private void handleAssociations(String field) throws InvocationTargetException, IllegalAccessException {
        List<AssociationField<A, B>> associationFieldList = getAssociationFields();
        boolean isOneToMany = verifyIfOneToMany(field, associationFieldList);
        if (!isOneToMany){
            handleOneToOne(field);
        }
    }

    private <S extends SearchEntity> void handleOneToOne(String field) throws InvocationTargetException, IllegalAccessException {
        String toOneEntityName = field.substring(0, field.indexOf("."));
        Class<B> toOneEntityClass = BullhornEntityInfo.getTypeFromName(toOneEntityName).getType();
        B toOneEntity = searchForEntity(field.substring(field.indexOf("."), field.length()), dataMap.get(field), toOneEntityClass).get(0);
        methodMap.get(field.toLowerCase()).invoke(entity,toOneEntity);
    }

    private boolean verifyIfOneToMany(String field, List<AssociationField<A, B>> associationFieldList) {
        boolean isOneToMany = false;
        for (AssociationField associationField : associationFieldList){
            if (associationField.getAssociationFieldName().equalsIgnoreCase(field.substring(0,field.indexOf(".")))) {
                associationMap.put(field.toLowerCase(), associationField);
                isOneToMany = true;
                break;
            }
        }
        return isOneToMany;
    }

    private void createNewAssociations() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        for (String associationName : associationMap.keySet()){
            addAssociationToEntity(associationName, associationMap.get(associationName));
        }
    }

    private void addAssociationToEntity(String field, AssociationField associationField) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        List<Integer> newAssociationIdList = getNewAssociationIdList(field, associationField);
        for (Integer associationId : newAssociationIdList) {
            bullhornData.associateWithEntity((Class<A>) entityClass, entityID, associationField, Sets.newHashSet(associationId));
        }
    }

    private List<Integer> getNewAssociationIdList(String field, AssociationField associationField) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        String associationName = field.substring(0,field.indexOf("."));

        Set<String> valueSet = Sets.newHashSet(dataMap.get(field).split(propertyFileUtil.getListDelimiter()));
        List<B> existingAssociations = getExistingAssociations(field, associationField, valueSet);
        Method method = getGetMethod(associationField, associationName);

        List<Integer> newAssociationList = filterOutExistingAssociations(valueSet, existingAssociations, method);
        return newAssociationList;
    }

    private List<Integer> filterOutExistingAssociations(Set<String> valueSet, List<B> existingAssociations, Method method) throws IllegalAccessException, InvocationTargetException {
        List<Integer> newAssociationList = new ArrayList<>();
        for (B association : existingAssociations){
            String returnedValue = String.valueOf(method.invoke(association));
            if (!valueSet.contains(returnedValue)){
                newAssociationList.add(association.getId());
            }
        }
        return newAssociationList;
    }

    private List<B> getExistingAssociations(String field, AssociationField associationField, Set<String> valueSet) {
        String where = getWhereStatement(valueSet, field);
        return bullhornData.query(associationField.getAssociationType(), where, Sets.newHashSet("id"), ParamFactory.queryParams()).getData();
    }

    private Method getGetMethod(AssociationField associationField, String associationName) throws NoSuchMethodException {
        String methodName = "get" + associationName.substring(0, 1).toUpperCase() + associationName.substring(1);
        try {
            return associationField.getAssociationType().getMethod(methodName);
        } catch (NoSuchMethodException e) {
            throw e;
        }
    }

    private String getWhereStatement(Set<String> valueSet, String field) {
        String fieldName = field.substring(field.indexOf("."), field.length());
        return fieldName + valueSet.stream().map(n -> fieldName + " = " + n).collect(Collectors.joining(" OR "));
    }

    private List<AssociationField<A, B>> getAssociationFields() {
        E entityAssociations = getEntityAssociations((Class<A>) entityClass);
        return entityAssociations.allAssociations();
    }

    private E getEntityAssociations(Class<A> entityClass) {
        return (entityClass == Candidate.class? (E) AssociationFactory.candidateAssociations() :(entityClass == Category.class? (E) AssociationFactory.categoryAssociations() :(entityClass == ClientContact.class? (E) AssociationFactory.clientContactAssociations() :(entityClass == ClientCorporation.class? (E) AssociationFactory.clientCorporationAssociations() :(entityClass == CorporateUser.class? (E) AssociationFactory.corporateUserAssociations() :(entityClass == JobOrder.class? (E) AssociationFactory.jobOrderAssociations() :(entityClass == Note.class? (E) AssociationFactory.noteAssociations() :(entityClass == Placement.class? (E) AssociationFactory.placementAssociations() :(entityClass == Opportunity.class? (E) AssociationFactory.opportunityAssociations() :(entityClass == Lead.class? (E) AssociationFactory.leadAssociations() : entityClass == Tearsheet.class? (E) AssociationFactory.tearsheetAssociations() :null))))))))));
    }

}

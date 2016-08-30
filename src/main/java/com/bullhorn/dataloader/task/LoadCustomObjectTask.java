package com.bullhorn.dataloader.task;

import com.bullhorn.dataloader.meta.EntityInfo;
import com.bullhorn.dataloader.service.Command;
import com.bullhorn.dataloader.service.csv.CsvFileWriter;
import com.bullhorn.dataloader.service.csv.Result;
import com.bullhorn.dataloader.util.ActionTotals;
import com.bullhorn.dataloader.util.PrintUtil;
import com.bullhorn.dataloader.util.PropertyFileUtil;
import com.bullhornsdk.data.api.BullhornData;
import com.bullhornsdk.data.exception.RestApiException;
import com.bullhornsdk.data.model.entity.association.EntityAssociations;
import com.bullhornsdk.data.model.entity.core.standard.Candidate;
import com.bullhornsdk.data.model.entity.core.standard.ClientContact;
import com.bullhornsdk.data.model.entity.core.type.AssociationEntity;
import com.bullhornsdk.data.model.entity.core.type.BullhornEntity;
import com.bullhornsdk.data.model.entity.core.type.QueryEntity;
import com.bullhornsdk.data.model.entity.core.type.SearchEntity;
import com.bullhornsdk.data.model.entity.core.type.UpdateEntity;
import com.bullhornsdk.data.model.entity.embedded.OneToMany;
import com.bullhornsdk.data.model.entity.meta.Field;
import com.bullhornsdk.data.model.entity.meta.MetaData;
import com.bullhornsdk.data.model.enums.BullhornEntityInfo;
import com.bullhornsdk.data.model.enums.MetaParameter;
import com.bullhornsdk.data.model.parameter.standard.ParamFactory;
import com.bullhornsdk.data.model.response.crud.CrudResponse;
import com.google.common.collect.Sets;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class LoadCustomObjectTask<A extends AssociationEntity, E extends EntityAssociations, B extends BullhornEntity> extends LoadTask<A, E, B> {
    protected B parentEntity;
    protected Class<B> parentEntityClass;
    private String instanceNumber;
    protected String parentField;
    protected Boolean parentEntityUpdateDone = false;

    public LoadCustomObjectTask(Command command,
                                Integer rowNumber,
                                EntityInfo entityInfo,
                                LinkedHashMap<String, String> dataMap,
                                Map<String, Method> methodMap,
                                Map<String, Integer> countryNameToIdMap,
                                CsvFileWriter csvWriter,
                                PropertyFileUtil propertyFileUtil,
                                BullhornData bullhornData,
                                PrintUtil printUtil,
                                ActionTotals actionTotals) {
        super(command, rowNumber, entityInfo, dataMap, methodMap, countryNameToIdMap, csvWriter, propertyFileUtil, bullhornData, printUtil, actionTotals);
    }

    @Override
    public void run() {
        init();
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
        getCustomObjectId();
        prepParentEntityForCustomObject();
        insertOrUpdateEntity();
        getCustomObjectId();
        return createResult();
    }

    @Override
    protected boolean validField(String field) {
        if (field.contains("_")){
            return false;
        }
        return super.validField(field);
    }

        @Override
    protected void insertOrUpdateEntity() throws IOException {
        try {
            CrudResponse response = bullhornData.updateEntity((UpdateEntity) parentEntity);
            checkForRestSdkErrorMessages(response);
            parentEntityUpdateDone = true;
        } catch (RestApiException e) {
            checkIfCouldUpdateCustomObject(e);
        }
    }

    private void checkIfCouldUpdateCustomObject(RestApiException e) {
        String stringPriorToMessage = "error persisting an entity of type: Update Failed: You do not have permission for ";
        if (e.getMessage().contains(stringPriorToMessage)) {
            int startIndex = e.getMessage().indexOf(stringPriorToMessage) + stringPriorToMessage.length();
            int endIndex = e.getMessage().indexOf("field customObject", startIndex);
            String cleanedExceptionMessage = e.getMessage().substring(startIndex, endIndex) + instanceNumber + " is not set up.";
            throw new RestApiException(cleanedExceptionMessage);
        } else {
            throw e;
        }
    }

    protected void getCustomObjectId() throws Exception {
        if (entityID == null) {
            List<B> matchingCustomObjectList = queryForMatchingCustomObject();
            checkForDuplicates(matchingCustomObjectList);
        }
    }

    private <Q extends QueryEntity> List<B> queryForMatchingCustomObject() throws InvocationTargetException, IllegalAccessException {
        Map<String, String> scrubbedDataMap = getDataMapWithoutUnusedFields();
        String where = scrubbedDataMap.keySet().stream().map(n -> getWhereStatment(n, (String) dataMap.get(n), getFieldType(entityClass, n))).collect(Collectors.joining(" AND "));
        List<B> matchingCustomObjectList = (List<B>) bullhornData.query((Class<Q>) entityClass, where, Sets.newHashSet("id"), ParamFactory.queryParams()).getData();
        return matchingCustomObjectList;
    }

    private Map<String, String> getDataMapWithoutUnusedFields() throws InvocationTargetException, IllegalAccessException {
        Map<String, String> scrubbedDataMap = new HashMap<>(dataMap);
        MetaData meta = bullhornData.getMetaData(entityClass, MetaParameter.BASIC, null);
        for (String fieldName : dataMap.keySet()) {
            boolean fieldIsInMeta = ((List<Field>) meta.getFields()).stream().map(n -> n.getName()).anyMatch(n -> n.equals(fieldName));
            if ((!fieldIsInMeta && !fieldName.contains(".")) || (fieldName.contains("_"))) {
                scrubbedDataMap.remove(fieldName);
            }
        }
        return scrubbedDataMap;
    }

    private void checkForDuplicates(List<B> matchingCustomObjectList) throws Exception {
        if (!matchingCustomObjectList.isEmpty()) {
            if (matchingCustomObjectList.size() > 1) {
                throw new RestApiException("Row " + rowNumber + ": Found duplicate.");
            } else {
                entityID = matchingCustomObjectList.get(0).getId();
                entity.setId(entityID);
                if (!parentEntityUpdateDone) {
                    isNewEntity = false;
                } else {
                    isNewEntity = true;
                }
            }
        }
    }

    @Override
    protected void handleAssociations(String field) throws Exception {
        getParentEntity(field);
    }

    protected void prepParentEntityForCustomObject() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Map<String, Method> parentCustomObjectMethods = getParentCustomObjectMethods();
        OneToMany oneToManyObject = getOneToMany(parentCustomObjectMethods);
        oneToManyObject = updateOneToManyObject(oneToManyObject);
        parentCustomObjectMethods.get("set").invoke(parentEntity, oneToManyObject);
    }

    private OneToMany updateOneToManyObject(OneToMany oneToManyObject) {
        List<B> customObjectData = Arrays.asList((B) entity);
        oneToManyObject.setData(customObjectData);
        oneToManyObject.setTotal(1);
        isNewEntity = false;
        return oneToManyObject;
    }

    private OneToMany getOneToMany(Map<String, Method> parentCustomObjectMethods) throws IllegalAccessException, InvocationTargetException {
        OneToMany customObjects = (OneToMany) parentCustomObjectMethods.get("get").invoke(parentEntity);
        customObjects = customObjects == null ? new OneToMany() : customObjects;
        customObjects.setTotal(customObjects.getTotal() == null ? 0 : customObjects.getTotal());
        return customObjects;
    }

    protected Map<String, Method> getParentCustomObjectMethods() throws NoSuchMethodException {
        Map<String, Method> parentCustomObjectMethods = new HashMap<>();
        parentCustomObjectMethods.put("set", parentEntityClass.getMethod("setCustomObject" + instanceNumber + "s", OneToMany.class));
        parentCustomObjectMethods.put("get", parentEntityClass.getMethod("getCustomObject" + instanceNumber + "s"));
        return parentCustomObjectMethods;
    }

    protected B getCustomObjectParent(String field, String fieldName, Class<B> parentEntityClass) {
        parentField = field;
        List<B> list;
        String value = dataMap.get(field);
        Class fieldType = getFieldType(parentEntityClass, fieldName);

        if (SearchEntity.class.isAssignableFrom(parentEntityClass)) {
            list = searchForEntity(fieldName, value, fieldType, parentEntityClass, Sets.newHashSet("id", "customObject" + instanceNumber + "s(*)"));
        } else {
            list = queryForEntity(fieldName, value, fieldType, parentEntityClass, Sets.newHashSet("id", "customObject" + instanceNumber + "s(*)"));
        }

        validateListFromRestCall(field, list, value);

        return list.get(0);
    }

    protected void getParentEntity(String field) throws Exception {
        String entityName = entityClass.getSimpleName();
        instanceNumber = entityName.substring(entityName.length() - 1, entityName.length());
        String fieldName = field.substring(field.indexOf(".") + 1, field.length());

        if (entityName.toLowerCase().contains("person")) {
            getPersonCustomObjectParentEntityClass(entityName);
        } else {
            getParentEntityClass(field);
        }
        parentEntity = getCustomObjectParent(field, fieldName, parentEntityClass);
    }

    protected void getParentEntityClass(String field) {
        String toOneEntityName = field.substring(0, field.indexOf("."));
        String parentEntityName = entityInfo.getEntityName().substring(0, entityInfo.getEntityName().indexOf("CustomObjectInstance"));
        if (!toOneEntityName.equalsIgnoreCase(parentEntityName)) {
            throw new RestApiException("Row " + rowNumber + ": To-One Association: '" + toOneEntityName + "' does not exist on " + entity.getClass().getSimpleName());
        }
        parentEntityClass = BullhornEntityInfo.getTypeFromName(toOneEntityName).getType();
    }

    protected void getPersonCustomObjectParentEntityClass(String entityName) throws Exception {
        String personSubtype = dataMap.get("person._subtype");
        if ("candidate".equalsIgnoreCase(personSubtype)){
            parentEntityClass = (Class<B>) Candidate.class;
        } else if ("clientcontact".equalsIgnoreCase(personSubtype) || "client contact".equalsIgnoreCase(personSubtype)){
            parentEntityClass = (Class<B>) ClientContact.class;
        } else if (personSubtype == null) {
            throw new Exception("Row " + rowNumber + ": The required field person._subType is missing. This field must be included to load " + entityName);
        } else {
            throw new Exception("Row " + rowNumber + ": The person._subType field must be either Candidate or ClientContact");
        }
    }

    @Override
    protected Map<String, String> getEntityExistFieldsMap() throws IOException {
        Map<String, String> entityExistFieldsMap = super.getEntityExistFieldsMap();
        if (!entityExistFieldsMap.isEmpty() && !entityExistFieldsMap.keySet().stream().anyMatch(n -> n.contains("."))){
            try {
                String parentEntityField = dataMap.keySet().stream().filter(n -> n.contains(".")).collect(Collectors.toList()).get(0);
                entityExistFieldsMap.put(parentEntityField, dataMap.get(parentEntityField));
            } catch (Exception e){
                throw new IOException("Parent entity must be included within csv.");
            }
        }
        return  entityExistFieldsMap;
    }
}

package com.bullhorn.dataloader.task;

import com.bullhorn.dataloader.service.Command;
import com.bullhorn.dataloader.service.csv.CsvFileWriter;
import com.bullhorn.dataloader.service.csv.Result;
import com.bullhorn.dataloader.util.ActionTotals;
import com.bullhorn.dataloader.util.PrintUtil;
import com.bullhorn.dataloader.util.PropertyFileUtil;
import com.bullhornsdk.data.api.BullhornData;
import com.bullhornsdk.data.exception.RestApiException;
import com.bullhornsdk.data.model.entity.association.EntityAssociations;
import com.bullhornsdk.data.model.entity.core.type.AssociationEntity;
import com.bullhornsdk.data.model.entity.core.type.BullhornEntity;
import com.bullhornsdk.data.model.entity.core.type.SearchEntity;
import com.bullhornsdk.data.model.entity.embedded.OneToMany;
import com.google.common.collect.Sets;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class LoadCustomObjectTask<A extends AssociationEntity, E extends EntityAssociations, B extends BullhornEntity> extends LoadTask {
    private B parentEntity;
    private Class<B> parentEntityClass;
    protected Class<B> originalClass;
    private String instanceNumber;

    public LoadCustomObjectTask(Command command,
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
        super(command, rowNumber, entityClass, dataMap, methodMap , countryNameToIdMap, csvWriter, propertyFileUtil, bullhornData, printUtil, actionTotals);
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
        prepParentEntityForCustomObject();
        insertOrUpdateEntity();
        getCustomObjectId();
        return createResult();
    }

    protected void getCustomObjectId() throws Exception {
        if (entityID == null){
            entityClass = originalClass;
            createEntityObject();
            isNewEntity = true;
        }
    }

    @Override
    protected void handleAssociations(String field) throws InvocationTargetException, IllegalAccessException {
        getParentEntity(field);
    }

    protected void prepParentEntityForCustomObject() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Map<String,Method> parentCustomObjectMethods = getParentCustomObjectMethods();
        OneToMany oneToManyObject = getOneToMany(parentCustomObjectMethods);
        oneToManyObject = updateOneToManyObject(oneToManyObject);
        parentCustomObjectMethods.get("set").invoke(parentEntity, oneToManyObject);
        entity = parentEntity;
        originalClass = entityClass;
        entityClass = parentEntityClass;
    }

    private OneToMany updateOneToManyObject(OneToMany oneToManyObject) {
        if (isNewEntity) {
            oneToManyObject.getData().add(entity);
            oneToManyObject.setTotal(oneToManyObject.getTotal() + 1);
            isNewEntity = false;
        } else {
            oneToManyObject = updateCustomObject(oneToManyObject);
        }
        return oneToManyObject;
    }

    private OneToMany getOneToMany(Map<String, Method> parentCustomObjectMethods) throws IllegalAccessException, InvocationTargetException {
        OneToMany customObjects = (OneToMany) parentCustomObjectMethods.get("get").invoke(parentEntity);
        customObjects = customObjects == null ? new OneToMany() : customObjects;
        customObjects.setTotal(customObjects.getTotal() == null ? 0 : customObjects.getTotal());
        return customObjects;
    }

    protected OneToMany updateCustomObject(OneToMany customObjects) {
        OneToMany updatedCustomObjects = new OneToMany();
        for (B customObject : (List<B>) customObjects.getData()){
            if (entity.getId().equals(customObject.getId())){
                updatedCustomObjects.getData().add(entity);
            } else {
                updatedCustomObjects.getData().add(customObject);
            }
        }
        updatedCustomObjects.setTotal(updatedCustomObjects.getData().size());
        return updatedCustomObjects;
    }

    protected Map<String, Method> getParentCustomObjectMethods() throws NoSuchMethodException {
        Map<String, Method> parentCustomObjectMethods = new HashMap<>();
        parentCustomObjectMethods.put("set", parentEntityClass.getMethod("setCustomObject" + instanceNumber + "s", OneToMany.class));
        parentCustomObjectMethods.put("get", parentEntityClass.getMethod("getCustomObject" + instanceNumber + "s"));
        return parentCustomObjectMethods;
    }

    protected B getCustomObjectParent(String field, String fieldName, Class<B> parentEntityClass) {
        List<B> list;
        String value = (String) dataMap.get(field);
        Class fieldType = getFieldType(parentEntityClass, fieldName);

        if (SearchEntity.class.isAssignableFrom(parentEntityClass)) {
            list = searchForEntity(fieldName, value, fieldType, parentEntityClass, Sets.newHashSet("id", "customObject" + instanceNumber + "s(*)"));
        } else {
            list = queryForEntity(fieldName, value, fieldType, parentEntityClass, Sets.newHashSet("id", "customObject" + instanceNumber + "s(*)"));
        }

        validateListFromRestCall(field, list, value);

        return list.get(0);
    }

    protected void getParentEntity(String field) throws InvocationTargetException, IllegalAccessException {
        String entityName = entityClass.getSimpleName();
        instanceNumber = entityName.substring(entityName.length()-1,entityName.length());
        String toOneEntityName = field.substring(0, field.indexOf("."));
        String fieldName = field.substring(field.indexOf(".") + 1, field.length());


        Method method = (Method) methodMap.get(toOneEntityName.toLowerCase());
        if (method == null) {
            throw new RestApiException("Row " + rowNumber + ": To-One Association: '" + toOneEntityName + "' does not exist on " + entity.getClass().getSimpleName());
        }

        parentEntityClass = (Class<B>) method.getParameterTypes()[0];
        parentEntity = (B) getCustomObjectParent(field, fieldName, parentEntityClass);
    }

}

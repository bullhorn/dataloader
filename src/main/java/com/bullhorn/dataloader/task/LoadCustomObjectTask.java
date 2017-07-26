package com.bullhorn.dataloader.task;

import com.bullhorn.dataloader.data.ActionTotals;
import com.bullhorn.dataloader.data.Cell;
import com.bullhorn.dataloader.data.CsvFileWriter;
import com.bullhorn.dataloader.data.Result;
import com.bullhorn.dataloader.data.Row;
import com.bullhorn.dataloader.enums.EntityInfo;
import com.bullhorn.dataloader.rest.Preloader;
import com.bullhorn.dataloader.rest.RestApi;
import com.bullhorn.dataloader.util.PrintUtil;
import com.bullhorn.dataloader.util.PropertyFileUtil;
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
import com.google.common.collect.Sets;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class LoadCustomObjectTask<A extends AssociationEntity, E extends EntityAssociations, B extends BullhornEntity> extends LoadTask<A, E, B> {
    protected B parentEntity;
    protected Class<B> parentEntityClass;
    private String instanceNumber;
    protected String parentField;
    protected Boolean parentEntityUpdateDone = false;

    public LoadCustomObjectTask(EntityInfo entityInfo,
                                Row row,
                                Preloader preloader,
                                CsvFileWriter csvFileWriter,
                                PropertyFileUtil propertyFileUtil,
                                RestApi restApi,
                                PrintUtil printUtil,
                                ActionTotals actionTotals) {
        super(entityInfo, row, preloader, csvFileWriter, propertyFileUtil, restApi, printUtil, actionTotals);
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
        if (field.contains("_")) {
            return false;
        }
        return super.validField(field);
    }

    @Override
    protected void insertOrUpdateEntity() throws IOException {
        try {
            restApi.updateEntity((UpdateEntity) parentEntity);
            parentEntityUpdateDone = true;
        } catch (RestApiException e) {
            checkIfCouldUpdateCustomObject(e);
        }
    }

    private void checkIfCouldUpdateCustomObject(RestApiException exception) {
        String stringPriorToMessage = "error persisting an entity of type: Update Failed: You do not have permission for ";
        if (exception.getMessage().contains(stringPriorToMessage)) {
            int startIndex = exception.getMessage().indexOf(stringPriorToMessage) + stringPriorToMessage.length();
            int endIndex = exception.getMessage().indexOf("field customObject", startIndex);
            String cleanedExceptionMessage = exception.getMessage().substring(startIndex, endIndex) + instanceNumber + " is not set up.";
            throw new RestApiException(cleanedExceptionMessage);
        } else {
            throw exception;
        }
    }

    protected void getCustomObjectId() throws Exception {
        if (entityId == null) {
            List<B> matchingCustomObjectList = queryForMatchingCustomObject();
            checkForDuplicates(matchingCustomObjectList);
        }
    }

    private <Q extends QueryEntity> List<B> queryForMatchingCustomObject() throws InvocationTargetException, IllegalAccessException {
        Row scrubbedRow = getRowWithoutUnusedFields();
        String where = scrubbedRow.getNames().stream().map(
            n -> getWhereStatement(
                n,
                row.getValue(n),
                getFieldType(entityInfo.getEntityClass(), n, n)))
            .collect(Collectors.joining(" AND "));
        List<B> matchingCustomObjectList = (List<B>) restApi.queryForList((Class<Q>) entityInfo.getEntityClass(), where, Sets.newHashSet("id"), ParamFactory.queryParams());
        return matchingCustomObjectList;
    }

    private Row getRowWithoutUnusedFields() throws InvocationTargetException, IllegalAccessException {
        Row scrubbedRow = new Row(row.getNumber());
        MetaData meta = restApi.getMetaData(entityInfo.getEntityClass(), MetaParameter.BASIC, null);

        for (Cell cell : row.getCells()) {
            boolean fieldIsInMeta = ((List<Field>) meta.getFields()).stream().map(n -> n.getName()).anyMatch(n -> n.equals(cell.getName()));
            if ((fieldIsInMeta || cell.isAssociation()) && (!cell.getName().contains("_"))) {
                scrubbedRow.addCell(cell);
            }
        }
        return scrubbedRow;
    }

    private void checkForDuplicates(List<B> matchingCustomObjectList) throws Exception {
        if (!matchingCustomObjectList.isEmpty()) {
            if (matchingCustomObjectList.size() > 1) {
                throw new RestApiException("Found duplicate.");
            } else {
                entityId = matchingCustomObjectList.get(0).getId();
                entity.setId(entityId);
                isNewEntity = parentEntityUpdateDone;
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
        String value = row.getValue(field);
        Class fieldType = getFieldType(parentEntityClass, field, fieldName);

        if (SearchEntity.class.isAssignableFrom(parentEntityClass)) {
            list = searchForEntity(fieldName, value, fieldType, parentEntityClass, Sets.newHashSet("id", "customObject" + instanceNumber + "s(*)"));
        } else {
            list = queryForEntity(fieldName, value, fieldType, parentEntityClass, Sets.newHashSet("id", "customObject" + instanceNumber + "s(*)"));
        }

        validateListFromRestCall(field, list, value);

        return list.get(0);
    }

    protected void getParentEntity(String field) throws Exception {
        String entityName = entityInfo.getEntityName();
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
            throw new RestApiException("To-One Association: '" + toOneEntityName + "' does not exist on " + entity.getClass().getSimpleName());
        }
        parentEntityClass = BullhornEntityInfo.getTypeFromName(toOneEntityName).getType();
    }

    protected void getPersonCustomObjectParentEntityClass(String entityName) throws Exception {
        String personSubtype = row.getValue("person._subtype");
        if ("candidate".equalsIgnoreCase(personSubtype)) {
            parentEntityClass = (Class<B>) Candidate.class;
        } else if ("clientcontact".equalsIgnoreCase(personSubtype) || "client contact".equalsIgnoreCase(personSubtype)) {
            parentEntityClass = (Class<B>) ClientContact.class;
        } else if (personSubtype == null) {
            throw new Exception("The required field person._subType is missing. This field must be included to load " + entityName);
        } else {
            throw new Exception("The person._subType field must be either Candidate or ClientContact");
        }
    }

    @Override
    protected Map<String, String> getEntityExistFieldsMap() throws IOException {
        Map<String, String> entityExistFieldsMap = super.getEntityExistFieldsMap();
        if (!entityExistFieldsMap.isEmpty() && !entityExistFieldsMap.keySet().stream().anyMatch(n -> n.contains("."))) {
            try {
                String parentEntityField = row.getNames().stream().filter(n -> n.contains(".")).collect(Collectors.toList()).get(0);
                entityExistFieldsMap.put(parentEntityField, row.getValue(parentEntityField));
            } catch (Exception e) {
                throw new IOException("Missing parent entity locator column, for example: 'candidate.id', "
                    + "'candidate.externalID', or 'candidate.whatever' so that the custom object can be loaded "
                    + "to the correct parent entity.");
            }
        }
        return entityExistFieldsMap;
    }
}

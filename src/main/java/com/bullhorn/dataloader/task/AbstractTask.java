package com.bullhorn.dataloader.task;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;

import com.bullhorn.dataloader.consts.TaskConsts;
import com.bullhorn.dataloader.service.Command;
import com.bullhorn.dataloader.service.csv.CsvFileWriter;
import com.bullhorn.dataloader.service.csv.Result;
import com.bullhorn.dataloader.util.ActionTotals;
import com.bullhorn.dataloader.util.PrintUtil;
import com.bullhorn.dataloader.util.PropertyFileUtil;
import com.bullhornsdk.data.api.BullhornData;
import com.bullhornsdk.data.exception.RestApiException;
import com.bullhornsdk.data.model.entity.core.type.BullhornEntity;
import com.bullhornsdk.data.model.entity.core.type.QueryEntity;
import com.bullhornsdk.data.model.entity.core.type.SearchEntity;
import com.bullhornsdk.data.model.enums.BullhornEntityInfo;
import com.bullhornsdk.data.model.parameter.standard.ParamFactory;
import com.bullhornsdk.data.model.response.crud.CrudResponse;
import com.bullhornsdk.data.model.response.crud.Message;
import com.google.common.collect.Sets;

public abstract class AbstractTask<B extends BullhornEntity> implements Runnable, TaskConsts {

    protected static AtomicInteger rowProcessedCount = new AtomicInteger(0);
    protected Command command;
    protected Integer rowNumber;
    protected Class<B> entityClass;
    protected Integer bullhornParentId;
    protected Map<String, String> dataMap;
    protected CsvFileWriter csvWriter;
    protected PropertyFileUtil propertyFileUtil;
    protected BullhornData bullhornData;
    protected PrintUtil printUtil;
    protected ActionTotals actionTotals;

    public AbstractTask(Command command,
                        Integer rowNumber,
                        Class<B> entityClass,
                        LinkedHashMap<String, String> dataMap,
                        CsvFileWriter csvWriter,
                        PropertyFileUtil propertyFileUtil,
                        BullhornData bullhornData,
                        PrintUtil printUtil,
                        ActionTotals actionTotals) {
        this.command = command;
        this.rowNumber = rowNumber;
        this.entityClass = entityClass;
        this.dataMap = dataMap;
        this.csvWriter = csvWriter;
        this.propertyFileUtil = propertyFileUtil;
        this.bullhornData = bullhornData;
        this.printUtil = printUtil;
        this.actionTotals = actionTotals;
    }

    protected void addParentEntityIDtoDataMap() {
        dataMap.put(TaskConsts.PARENT_ENTITY_ID, bullhornParentId.toString());
    }

    protected void writeToResultCSV(Result result) {
        int attempts = 0;
        while (attempts < 3) {
            try {
                csvWriter.writeRow(dataMap.values().toArray(new String[0]), result);
                updateActionTotals(result);
                updateRowProcessedCounts();
                break;
            } catch (IOException e) {
                printUtil.printAndLog(e);
                attempts++;
            }
        }
    }

    protected void updateRowProcessedCounts() {
        rowProcessedCount.incrementAndGet();
        if (rowProcessedCount.intValue() % 111 == 0) {
            printUtil.printAndLog("Processed: " + NumberFormat.getNumberInstance(Locale.US).format(rowProcessedCount) + " records.");
        }
    }

    private void updateActionTotals(Result result) {
        actionTotals.incrementActionTotal(result.getAction());
    }

    protected Result handleFailure(Exception e) {
        printUtil.printAndLog(e);
        return Result.Failure(e);
    }

    protected Result handleFailure(Exception e, Integer entityID) {
        printUtil.printAndLog(e);
        if (entityID != null) {
            return Result.Failure(e, entityID);
        }
        return Result.Failure(e);
    }

    protected <S extends SearchEntity> List<B> searchForEntity(String field, String value, Class fieldType, Class<B> entityClass) {
        String query = getQueryStatement(field, value, fieldType);
        return (List<B>) bullhornData.search((Class<S>) entityClass, query, Sets.newHashSet("id"), ParamFactory.searchParams()).getData();
    }

    protected String getQueryStatement(String field, String value, Class fieldType) {
        if (Integer.class.equals(fieldType)) {
            return field + ":" + value;
        } else if (String.class.equals(fieldType)) {
            return field + ":\"" + value + "\"";
        } else {
            throw new RestApiException("Row " + rowNumber + ": Failed to create lucene search string for: '" + field + "' with unsupported field type: " + fieldType);
        }
    }

    protected List<B> findEntityList() {
        Map<String, String> entityExistFieldsMap = getEntityExistFieldsMap();

        if (!entityExistFieldsMap.isEmpty()) {
            if (SearchEntity.class.isAssignableFrom(entityClass)) {
                return searchForEntity(entityExistFieldsMap);
            } else {
                return queryForEntity(entityExistFieldsMap);
            }
        }

        return new ArrayList<B>();
    }

    private <S extends SearchEntity> List<B> searchForEntity(Map<String, String> entityExistFieldsMap) {
        String query = entityExistFieldsMap.keySet().stream().map(n -> getQueryStatement(n, entityExistFieldsMap.get(n), getFieldType(entityClass, n))).collect(Collectors.joining(" AND "));
        return (List<B>) bullhornData.search((Class<S>) entityClass, query, Sets.newHashSet("id"), ParamFactory.searchParams()).getData();
    }

    private <Q extends QueryEntity> List<B> queryForEntity(Map<String, String> entityExistFieldsMap) {
        String query = entityExistFieldsMap.keySet().stream().map(n -> getWhereStatment(n, entityExistFieldsMap.get(n), getFieldType(entityClass, n))).collect(Collectors.joining(" AND "));
        return (List<B>) bullhornData.query((Class<Q>) entityClass, query, Sets.newHashSet("id"), ParamFactory.queryParams()).getData();
    }

    protected <Q extends QueryEntity> List<B> queryForEntity(String field, String value, Class fieldType, Class<B> entityClass) {
        String where = getWhereStatment(field, value, fieldType);
        return (List<B>) bullhornData.query((Class<Q>) entityClass, where, Sets.newHashSet("id"), ParamFactory.queryParams()).getData();
    }

    protected String getWhereStatment(String field, String value, Class fieldType) {
        if (Integer.class.equals(fieldType)) {
            return field + "=" + value;
        } else if (String.class.equals(fieldType)) {
            return field + "='" + value + "'";
        } else {
            throw new RestApiException("Row " + rowNumber + ": Failed to create query where clause for: '" + field + "' with unsupported field type: " + fieldType);
        }
    }

    protected Map<String, String> getEntityExistFieldsMap() {
        Map<String, String> entityExistFieldsMap = new HashMap<>();

        Optional<List<String>> existFields = propertyFileUtil.getEntityExistFields(entityClass.getSimpleName());
        if (existFields.isPresent()) {
            for (String existField : existFields.get()) {
                entityExistFieldsMap.put(existField, dataMap.get(existField));
            }
        }

        return entityExistFieldsMap;
    }

    protected Object convertStringToClass(Method method, String value) throws ParseException {
        if (StringUtils.isEmpty(value)) {
            return null;
        }
        Class convertToClass = method.getParameterTypes()[0];
        value = value.trim();
        if (String.class.equals(convertToClass)) {
            return value;
        } else if (Integer.class.equals(convertToClass)) {
            return Integer.parseInt(value);
        } else if (Double.class.equals(convertToClass)) {
            return Double.parseDouble(value);
        } else if (Boolean.class.equals(convertToClass)) {
            return Boolean.getBoolean(value);
        } else if (DateTime.class.equals(convertToClass)) {
            DateTimeFormatter formatter = propertyFileUtil.getDateParser();
            return formatter.parseDateTime(value);
        } else if (BigDecimal.class.equals(convertToClass)) {
            DecimalFormat decimalFormat = new DecimalFormat();
            decimalFormat.setParseBigDecimal(true);
            return (BigDecimal) decimalFormat.parse(value);
        }
        return null;
    }

    protected Class getFieldType(Class<B> toOneEntityClass, String fieldName) {
        if (fieldName.indexOf(".") > -1) {
            toOneEntityClass = BullhornEntityInfo.getTypeFromName(fieldName.substring(0, fieldName.indexOf("."))).getType();
            fieldName = fieldName.substring(fieldName.indexOf(".") + 1);
        }
        String getMethodName = "get" + fieldName;
        List<Method> methods = Arrays.asList(toOneEntityClass.getMethods()).stream().filter(n -> getMethodName.equalsIgnoreCase(n.getName())).collect(Collectors.toList());
        if (methods.isEmpty()) {
            throw new RestApiException("Row " + rowNumber + ": To-One Association field: '" + fieldName + "' does not exist on " + toOneEntityClass.getSimpleName());
        }

        return methods.get(0).getReturnType();
    }

    protected void checkForRestSdkErrorMessages(CrudResponse response) {
        if (!response.getMessages().isEmpty() && response.getChangedEntityId() == null) {
            StringBuilder sb = new StringBuilder();
            for (Message message : response.getMessages()) {
                sb.append("\tError occurred on field " + message.getPropertyName() + " due to the following: " + message.getDetailMessage());
                sb.append("\n");
            }
            throw new RestApiException("Row " + rowNumber + ": Error occurred when making " + response.getChangeType().toString() + " REST call:\n" + sb.toString());
        }
    }

    protected void checkForRequiredFieldsError(Exception e) {
        if (e.getMessage().indexOf("\"type\" : \"DUPLICATE_VALUE\"") > -1 && e.getMessage().indexOf("\"propertyName\" : null") > -1) {
            throw new RestApiException("Missing required fields for " + entityClass.getSimpleName() + ".");
        }
    }

    /**
     * populates a field on an entity using reflection
     *
     * @param field     field to populate
     * @param value     value to populate field with
     * @param entity    the entity to populate
     * @param methodMap map of set methods on entity
     */
    protected void populateFieldOnEntity(String field, String value, Object entity, Map<String, Method> methodMap) throws ParseException, InvocationTargetException, IllegalAccessException {
        Method method = methodMap.get(field.toLowerCase());
        if (method == null) {
            throw new RestApiException("Row " + rowNumber + ": Invalid field: '" + field + "' does not exist on " + entity.getClass().getSimpleName());
        }
        if (isAddressField(field)) {
            throw new RestApiException("Row " + rowNumber + ": Invalid address set up: '" + field + "' Must use 'address." + field + "' in csv header" );
        }

        if (value != null && !"".equalsIgnoreCase(value)) {
            method.invoke(entity, convertStringToClass(method, value));
        }
    }

    private boolean isAddressField(String field) {
        List<String> addressFields = Arrays.asList("address1", "address2", "city", "state", "zip", "countryid");
        return addressFields.indexOf(field.toLowerCase()) > -1;
    }

    protected String getCamelCasedClassToString() {
        return entityClass.getSimpleName().substring(0, 1).toLowerCase() + entityClass.getSimpleName().substring(1);
    }
}

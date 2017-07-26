package com.bullhorn.dataloader.task;

import com.bullhorn.dataloader.data.ActionTotals;
import com.bullhorn.dataloader.data.CsvFileWriter;
import com.bullhorn.dataloader.data.Result;
import com.bullhorn.dataloader.data.Row;
import com.bullhorn.dataloader.enums.EntityInfo;
import com.bullhorn.dataloader.rest.RestApi;
import com.bullhorn.dataloader.util.PrintUtil;
import com.bullhorn.dataloader.util.PropertyFileUtil;
import com.bullhorn.dataloader.util.StringConsts;
import com.bullhornsdk.data.exception.RestApiException;
import com.bullhornsdk.data.model.entity.association.EntityAssociations;
import com.bullhornsdk.data.model.entity.core.standard.Note;
import com.bullhornsdk.data.model.entity.core.type.AssociationEntity;
import com.bullhornsdk.data.model.entity.core.type.BullhornEntity;
import com.bullhornsdk.data.model.entity.core.type.QueryEntity;
import com.bullhornsdk.data.model.entity.core.type.SearchEntity;
import com.bullhornsdk.data.model.enums.BullhornEntityInfo;
import com.bullhornsdk.data.model.parameter.standard.ParamFactory;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public abstract class AbstractTask<A extends AssociationEntity, E extends EntityAssociations, B extends BullhornEntity> implements Runnable {
    static AtomicInteger rowProcessedCount = new AtomicInteger(0);

    protected EntityInfo entityInfo;
    protected Row row;
    protected CsvFileWriter csvFileWriter;
    protected PropertyFileUtil propertyFileUtil;
    protected RestApi restApi;
    protected PrintUtil printUtil;
    protected ActionTotals actionTotals;

    AbstractTask(EntityInfo entityInfo,
                 Row row,
                 CsvFileWriter csvFileWriter,
                 PropertyFileUtil propertyFileUtil,
                 RestApi restApi,
                 PrintUtil printUtil,
                 ActionTotals actionTotals) {
        this.entityInfo = entityInfo;
        this.row = row;
        this.csvFileWriter = csvFileWriter;
        this.propertyFileUtil = propertyFileUtil;
        this.restApi = restApi;
        this.printUtil = printUtil;
        this.actionTotals = actionTotals;
    }

    void writeToResultCSV(Result result) {
        int attempts = 0;
        while (attempts < 3) {
            try {
                csvFileWriter.writeRow(row, result);
                updateActionTotals(result);
                updateRowProcessedCounts();
                break;
            } catch (IOException e) {
                printUtil.printAndLog(e);
                attempts++;
            }
        }
    }

    void updateRowProcessedCounts() {
        rowProcessedCount.incrementAndGet();
        if (rowProcessedCount.intValue() % 111 == 0) {
            printUtil.printAndLog("Processed: " + NumberFormat.getNumberInstance(Locale.US).format(rowProcessedCount) + " records.");
        }
    }

    private void updateActionTotals(Result result) {
        actionTotals.incrementActionTotal(result.getAction());
    }

    Result handleFailure(Exception exception) {
        printUtil.printAndLog(exception);
        return Result.failure(exception);
    }

    /**
     * Generic handling of an error for the row that fails.
     *
     * @param exception the exception that was caught
     * @param entityID  the entity ID (or null if it does not exist)
     * @return a result object that captures the error text
     */
    Result handleFailure(Exception exception, Integer entityID) {
        printUtil.printAndLog("Row " + row.getNumber() + ": " + exception);
        if (entityID != null) {
            return Result.failure(exception, entityID);
        }
        return Result.failure(exception);
    }

    <S extends SearchEntity> List<B> searchForEntity(String field, String value, Class fieldType, Class<B> fieldEntityClass, Set<String> fieldsToReturn) {
        String query = getQueryStatement(field, value, fieldType, fieldEntityClass);
        fieldsToReturn = fieldsToReturn == null ? Sets.newHashSet("id") : fieldsToReturn;
        return (List<B>) restApi.searchForList((Class<S>) fieldEntityClass, query, fieldsToReturn, ParamFactory.searchParams());
    }

    private <S extends SearchEntity> List<B> searchForEntity(Map<String, String> fieldToValueMap) {
        String query = fieldToValueMap.keySet().stream().map(n -> getQueryStatement(n, fieldToValueMap.get(n), getFieldType(entityInfo.getEntityClass(), n, n), getFieldEntityClass(n))).collect(Collectors.joining(" AND "));
        return (List<B>) restApi.searchForList((Class<S>) entityInfo.getEntityClass(), query, Sets.newHashSet("id"), ParamFactory.searchParams());
    }

    <Q extends QueryEntity> List<B> queryForEntity(String field, String value, Class fieldType, Class<B> fieldEntityClass, Set<String> fieldsToReturn) {
        String where = getWhereStatement(field, value, fieldType);
        fieldsToReturn = fieldsToReturn == null ? Sets.newHashSet("id") : fieldsToReturn;
        return (List<B>) restApi.queryForList((Class<Q>) fieldEntityClass, where, fieldsToReturn, ParamFactory.queryParams());
    }

    private <Q extends QueryEntity> List<B> queryForEntity(Map<String, String> fieldToValueMap) {
        String query = fieldToValueMap.keySet().stream().map(n -> getWhereStatement(n, fieldToValueMap.get(n), getFieldType(entityInfo.getEntityClass(), n, n))).collect(Collectors.joining(" AND "));
        return (List<B>) restApi.queryForList((Class<Q>) entityInfo.getEntityClass(), query, Sets.newHashSet("id"), ParamFactory.queryParams());
    }

    String getQueryStatement(String field, String value, Class fieldType, Class<B> fieldEntityClass) {
        if (fieldEntityClass.equals(Note.class) && field.equals(StringConsts.ID)) {
            field = StringConsts.NOTE_ID;
        }

        if (Integer.class.equals(fieldType) || BigDecimal.class.equals(fieldType) || Double.class.equals(fieldType) || Boolean.class.equals(fieldType)) {
            return field + ":" + value;
        } else if (DateTime.class.equals(fieldType) || String.class.equals(fieldType)) {
            return field + ":\"" + value + "\"";
        } else {
            throw new RestApiException("Failed to create lucene search string for: '" + field + "' with unsupported field type: " + fieldType);
        }
    }

    List<B> findEntityList(Map<String, String> fieldToValueMap) {
        if (!fieldToValueMap.isEmpty()) {
            if (SearchEntity.class.isAssignableFrom(entityInfo.getEntityClass())) {
                return searchForEntity(fieldToValueMap);
            } else {
                return queryForEntity(fieldToValueMap);
            }
        }

        return new ArrayList<>();
    }

    String getWhereStatement(String field, String value, Class fieldType) {
        if (Integer.class.equals(fieldType) || BigDecimal.class.equals(fieldType) || Double.class.equals(fieldType)) {
            return field + "=" + value;
        } else if (Boolean.class.equals(fieldType)) {
            return field + "=" + getBooleanWhereStatement(value);
        } else if (String.class.equals(fieldType)) {
            return field + "='" + value + "'";
        } else if (DateTime.class.equals(fieldType)) {
            return field + "=" + getDateQuery(value);
        } else {
            throw new RestApiException("Failed to create query where clause for: '" + field + "' with unsupported field type: " + fieldType);
        }
    }

    String getBooleanWhereStatement(String value) {
        if (value.equals("1")) {
            return "true";
        } else {
            return Boolean.toString(Boolean.valueOf(value));
        }
    }

    String getDateQuery(String value) {
        if (entityInfo.isCustomObject()) {
            DateTimeFormatter formatter = propertyFileUtil.getDateParser();
            DateTime dateTime = formatter.parseDateTime(value);
            return String.valueOf(dateTime.toDate().getTime());
        } else {
            DateTimeFormatter formatter = propertyFileUtil.getDateParser();
            DateTime dateTime = formatter.parseDateTime(value);
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
            return df.format(dateTime.toDate());
        }
    }

    protected Map<String, String> getEntityExistFieldsMap() throws IOException {
        Map<String, String> entityExistFieldsMap = new HashMap<>();

        // TODO: Simplify the entity exist field data structure to not be an optional
        Optional<List<String>> existFields = propertyFileUtil.getEntityExistFields(entityInfo.getEntityClass().getSimpleName());
        if (existFields.isPresent()) {
            for (String existField : existFields.get()) {
                entityExistFieldsMap.put(existField, row.getValue(existField));
            }
        }

        return entityExistFieldsMap;
    }

    Object convertStringToClass(Method method, String value) throws ParseException {
        Class convertToClass = method.getParameterTypes()[0];
        value = value.trim();

        if (String.class.equals(convertToClass)) {
            return value;
        } else if (Integer.class.equals(convertToClass)) {
            if (StringUtils.isEmpty(value)) {
                return 0;
            }
            return Integer.parseInt(value);
        } else if (Double.class.equals(convertToClass)) {
            if (StringUtils.isEmpty(value)) {
                return 0.0;
            }
            return Double.parseDouble(value);
        } else if (Boolean.class.equals(convertToClass)) {
            if (StringUtils.isEmpty(value)) {
                return Boolean.parseBoolean(null);
            }
            return Boolean.parseBoolean(value);
        } else if (DateTime.class.equals(convertToClass)) {
            DateTimeFormatter formatter = propertyFileUtil.getDateParser();
            if (!StringUtils.isEmpty(value)) {
                return formatter.parseDateTime(value);
            }
        } else if (BigDecimal.class.equals(convertToClass)) {
            DecimalFormat decimalFormat = new DecimalFormat();
            decimalFormat.setParseBigDecimal(true);
            if (StringUtils.isEmpty(value)) {
                return decimalFormat.parse(String.valueOf(0.0));
            }
            return decimalFormat.parse(value);
        }
        return null;
    }

    /**
     * Returns the type of the given field on the given entity
     *
     * @param fieldType The type of the field if it already is known, otherwise the type of the parent
     * @param field     The name of the field, like: 'commentingPerson.id', otherwise just the fieldName itself
     * @param fieldName The part of the field after the '.', like: 'id'
     * @return The class type of the field retrieved from the SDK-REST object.
     */
    Class getFieldType(Class<B> fieldType, String field, String fieldName) {
        if (fieldName.contains(".")) {
            fieldType = BullhornEntityInfo.getTypeFromName(fieldName.substring(0, fieldName.indexOf("."))).getType();
            fieldName = fieldName.substring(fieldName.indexOf(".") + 1);
        }
        String getMethodName = "get" + fieldName;
        List<Method> methods = Arrays.stream(fieldType.getMethods()).filter(n -> getMethodName.equalsIgnoreCase(n.getName())).collect(Collectors.toList());
        if (methods.isEmpty()) {
            throw new RestApiException("'" + field + "': '" + fieldName + "' does not exist on " + fieldType.getSimpleName());
        }

        return methods.get(0).getReturnType();
    }

    // TODO: Refactor this to use entityInfo.fromString() and Cell

    /**
     * Returns the Bullhorn entity class for the given field on the given entity
     *
     * @param field The name of the field, like: 'commentingPerson.id', otherwise just the fieldName itself
     * @return The Bullhorn entity class that the field is on
     */
    Class<B> getFieldEntityClass(String field) {
        if (field.contains(".")) {
            return BullhornEntityInfo.getTypeFromName(field.substring(0, field.indexOf("."))).getType();
        } else {
            return entityInfo.getEntityClass();
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
    void populateFieldOnEntity(String field, String value, Object entity, Map<String, Method> methodMap) throws ParseException, InvocationTargetException, IllegalAccessException {
        Method method = methodMap.get(field.toLowerCase());
        if (method == null) {
            throw new RestApiException("Invalid field: '" + field + "' does not exist on " + entity.getClass().getSimpleName());
        }

        if (isAddressField(field) && methodMap.containsKey("address")) {
            throw new RestApiException("Invalid address field format: '" + field + "' Must use 'address." + field + "' in csv header");
        }

        if (value != null) {
            method.invoke(entity, convertStringToClass(method, value));
        }
    }

    // TODO: Move out to utility class
    private boolean isAddressField(String field) {
        List<String> addressFields = Arrays.asList("address1", "address2", "city", "state", "zip", "countryid", "countryname");
        return addressFields.indexOf(field.toLowerCase()) > -1;
    }
}

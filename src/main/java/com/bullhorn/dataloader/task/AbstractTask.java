package com.bullhorn.dataloader.task;

import com.bullhorn.dataloader.data.ActionTotals;
import com.bullhorn.dataloader.data.CsvFileWriter;
import com.bullhorn.dataloader.data.Result;
import com.bullhorn.dataloader.data.Row;
import com.bullhorn.dataloader.enums.EntityInfo;
import com.bullhorn.dataloader.rest.Field;
import com.bullhorn.dataloader.rest.Record;
import com.bullhorn.dataloader.rest.RestApi;
import com.bullhorn.dataloader.util.PrintUtil;
import com.bullhorn.dataloader.util.PropertyFileUtil;
import com.bullhorn.dataloader.util.StringConsts;
import com.bullhornsdk.data.exception.RestApiException;
import com.bullhornsdk.data.model.entity.core.type.BullhornEntity;
import com.bullhornsdk.data.model.entity.core.type.QueryEntity;
import com.bullhornsdk.data.model.entity.core.type.SearchEntity;
import com.bullhornsdk.data.model.parameter.standard.ParamFactory;
import com.google.common.collect.Sets;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public abstract class AbstractTask<B extends BullhornEntity> implements Runnable {
    static AtomicInteger rowProcessedCount = new AtomicInteger(0);

    protected EntityInfo entityInfo;
    protected Row row;
    protected PropertyFileUtil propertyFileUtil;
    protected RestApi restApi;
    protected PrintUtil printUtil;
    protected ActionTotals actionTotals;
    protected Integer entityId;
    private CsvFileWriter csvFileWriter;

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

    /**
     * The public method that is called by the Task Executor on this Runnable object.
     *
     * Calls the handle method on the derived task, handles errors and writes the output files.
     */
    public void run() {
        Result result;
        try {
            result = handle();
        } catch (Exception e) {
            result = handleFailure(e);
        }
        writeToResultCsv(result);
    }

    /**
     * The overridden protected method that performs the task's duties.
     *
     * @return the result object from the task
     */
    protected abstract Result handle() throws Exception;

    /**
     * Generic handling of an error for the row that fails.
     *
     * @param exception the exception that was caught
     * @return a result object that captures the error text
     */
    private Result handleFailure(Exception exception) {
        printUtil.printAndLog("Row " + row.getNumber() + ": " + exception);
        if (entityId != null) {
            return Result.failure(exception, entityId);
        }
        return Result.failure(exception);
    }

    private void updateRowProcessedCounts() {
        rowProcessedCount.incrementAndGet();
        if (rowProcessedCount.intValue() % 111 == 0) {
            printUtil.printAndLog("Processed: "
                + NumberFormat.getNumberInstance(Locale.US).format(rowProcessedCount) + " records.");
        }
    }

    private void updateActionTotals(Result result) {
        actionTotals.incrementActionTotal(result.getAction());
    }

    private void writeToResultCsv(Result result) {
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

    // region Entity Lookup Methods
    List<B> findEntityList(Record record) {
        return findEntityList(record.getEntityExistFields());
    }

    List<B> findEntityList(List<Field> entityExistFields) {
        if (!entityExistFields.isEmpty()) {
            if (entityExistFields.get(0).getEntityInfo().isSearchEntity()) {
                return searchForEntity(entityExistFields);
            } else {
                return queryForEntity(entityExistFields);
            }
        }

        return new ArrayList<>();
    }

    @SuppressWarnings("unchecked")
    private <S extends SearchEntity> List<B> searchForEntity(List<Field> entityExistFields) {
        String query = entityExistFields.stream().map(
            n -> getQueryStatement(n.getCell().getName(), n.getStringValue(), n.getFieldType(), n.getFieldEntity()))
            .collect(Collectors.joining(" AND "));
        return (List<B>) restApi.searchForList((Class<S>) entityInfo.getEntityClass(), query,
            Sets.newHashSet("id"), ParamFactory.searchParams());
    }

    @SuppressWarnings("unchecked")
    <S extends SearchEntity> List<B> searchForEntity(String field, String value, Class fieldType,
                                                     EntityInfo fieldEntityInfo, Set<String> fieldsToReturn) {
        String query = getQueryStatement(field, value, fieldType, fieldEntityInfo);
        fieldsToReturn = fieldsToReturn == null ? Sets.newHashSet("id") : fieldsToReturn;
        return (List<B>) restApi.searchForList((Class<S>) fieldEntityInfo.getEntityClass(), query,
            fieldsToReturn, ParamFactory.searchParams());
    }

    @SuppressWarnings("unchecked")
    private <Q extends QueryEntity> List<B> queryForEntity(List<Field> entityExistFields) {
        String query = entityExistFields.stream().map(
            n -> getWhereStatement(n.getCell().getName(), n.getStringValue(), n.getFieldType()))
            .collect(Collectors.joining(" AND "));
        return (List<B>) restApi.queryForList((Class<Q>) entityInfo.getEntityClass(), query,
            Sets.newHashSet("id"), ParamFactory.queryParams());
    }

    @SuppressWarnings("unchecked")
    <Q extends QueryEntity> List<B> queryForEntity(String field, String value, Class fieldType,
                                                   EntityInfo fieldEntityInfo, Set<String> fieldsToReturn) {
        String where = getWhereStatement(field, value, fieldType);
        fieldsToReturn = fieldsToReturn == null ? Sets.newHashSet("id") : fieldsToReturn;
        return (List<B>) restApi.queryForList((Class<Q>) fieldEntityInfo.getEntityClass(), where,
            fieldsToReturn, ParamFactory.queryParams());
    }

    String getQueryStatement(String field, String value, Class fieldType, EntityInfo fieldEntityInfo) {
        if (fieldEntityInfo == EntityInfo.NOTE && field.equals(StringConsts.ID)) {
            field = StringConsts.NOTE_ID;
        }

        if (Integer.class.equals(fieldType) || BigDecimal.class.equals(fieldType) || Boolean.class.equals(fieldType)) {
            return field + ":" + value;
        } else if (DateTime.class.equals(fieldType) || String.class.equals(fieldType)) {
            return field + ":\"" + value + "\"";
        } else {
            throw new RestApiException("Failed to create lucene search string for: '" + field
                + "' with unsupported field type: " + fieldType);
        }
    }

    String getWhereStatement(String field, String value, Class fieldType) {
        if (Integer.class.equals(fieldType) || BigDecimal.class.equals(fieldType) || Double.class.equals(fieldType)) {
            return field + "=" + value;
        } else if (Boolean.class.equals(fieldType)) {
            return field + "=" + getBooleanWhereStatement(value);
        } else if (String.class.equals(fieldType)) {
            return field + "='" + value + "'";
        } else if (DateTime.class.equals(fieldType)) {
            // TODO: This needs to be of the format: `dateOfBirth:[20170808 TO 20170808235959]` for dates
            // Format: [yyyyMMdd TO yyyyMMddHHmmss] - a date range of one day
            return field + "=" + getDateQuery(value);
        } else {
            throw new RestApiException("Failed to create query where clause for: '" + field
                + "' with unsupported field type: " + fieldType);
        }
    }

    String getBooleanWhereStatement(String value) {
        if (value.equals("1")) {
            return "true";
        } else {
            return Boolean.toString(Boolean.valueOf(value));
        }
    }

    private String getDateQuery(String value) {
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
    // endregion
}

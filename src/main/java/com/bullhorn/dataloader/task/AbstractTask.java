package com.bullhorn.dataloader.task;

import com.bullhorn.dataloader.data.ActionTotals;
import com.bullhorn.dataloader.data.CsvFileWriter;
import com.bullhorn.dataloader.data.Result;
import com.bullhorn.dataloader.data.Row;
import com.bullhorn.dataloader.enums.EntityInfo;
import com.bullhorn.dataloader.rest.CompleteUtil;
import com.bullhorn.dataloader.rest.Field;
import com.bullhorn.dataloader.rest.RestApi;
import com.bullhorn.dataloader.util.FindUtil;
import com.bullhorn.dataloader.util.PrintUtil;
import com.bullhorn.dataloader.util.PropertyFileUtil;
import com.bullhornsdk.data.model.entity.core.type.BullhornEntity;
import com.bullhornsdk.data.model.entity.core.type.QueryEntity;
import com.bullhornsdk.data.model.entity.core.type.SearchEntity;
import com.bullhornsdk.data.model.parameter.standard.ParamFactory;
import com.google.common.collect.Sets;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class AbstractTask<B extends BullhornEntity> implements Runnable {
    static AtomicInteger rowProcessedCount = new AtomicInteger(0);

    protected EntityInfo entityInfo;
    protected Row row;
    protected PropertyFileUtil propertyFileUtil;
    protected RestApi restApi;
    protected PrintUtil printUtil;
    protected ActionTotals actionTotals;
    protected Integer entityId;
    protected CompleteUtil completeUtil;
    private CsvFileWriter csvFileWriter;

    AbstractTask(EntityInfo entityInfo,
                 Row row,
                 CsvFileWriter csvFileWriter,
                 PropertyFileUtil propertyFileUtil,
                 RestApi restApi,
                 PrintUtil printUtil,
                 ActionTotals actionTotals,
                 CompleteUtil completeUtil) {
        this.entityInfo = entityInfo;
        this.row = row;
        this.csvFileWriter = csvFileWriter;
        this.propertyFileUtil = propertyFileUtil;
        this.restApi = restApi;
        this.printUtil = printUtil;
        this.actionTotals = actionTotals;
        this.completeUtil = completeUtil;
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
        if (propertyFileUtil.getResultsFileEnabled()) {
            completeUtil.rowComplete(row, result, actionTotals);
        }
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

    private void writeToResultCsv(Result result) {
        actionTotals.incrementActionTotal(result.getAction());
        updateRowProcessedCounts();

        // Handle the situation where the results files are locked for a brief period of time
        int attempts = 0;
        while (attempts < 3) {
            try {
                csvFileWriter.writeRow(row, result);
                break;
            } catch (IOException e) {
                printUtil.printAndLog(e);
                attempts++;
            }
        }
    }

    // region Entity Lookup Methods
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
        return (List<B>) restApi.searchForList((Class<S>) entityInfo.getEntityClass(),
            FindUtil.getLuceneSearch(entityExistFields, propertyFileUtil),
            Sets.newHashSet("id"), ParamFactory.searchParams());
    }

    @SuppressWarnings("unchecked")
    private <Q extends QueryEntity> List<B> queryForEntity(List<Field> entityExistFields) {
        return (List<B>) restApi.queryForList((Class<Q>) entityInfo.getEntityClass(),
            FindUtil.getSqlQuery(entityExistFields, propertyFileUtil),
            Sets.newHashSet("id"), ParamFactory.queryParams());
    }

    // endregion
}

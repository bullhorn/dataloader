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

import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class AbstractTask implements Runnable {
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

    // TODO: Verify here with current unit tests, then move and update unit tests
    @SuppressWarnings("unchecked")
    public static <B extends BullhornEntity, S extends SearchEntity, Q extends QueryEntity> List<BullhornEntity> findEntities(
        EntityInfo entityInfo, List<Field> entityExistFields, Set<String> returnFields, PropertyFileUtil propertyFileUtil, RestApi restApi) {
        if (!entityExistFields.isEmpty()) {
            if (entityExistFields.get(0).getEntityInfo().isSearchEntity()) {
                List<B> list = (List<B>) restApi.searchForList((Class<S>) entityInfo.getEntityClass(),
                    FindUtil.getLuceneSearch(entityExistFields, propertyFileUtil), returnFields, ParamFactory.searchParams());
                return (List<BullhornEntity>) list;
            } else {
                List<B> list = (List<B>) restApi.queryForList((Class<Q>) entityInfo.getEntityClass(),
                    FindUtil.getSqlQuery(entityExistFields, propertyFileUtil), returnFields, ParamFactory.queryParams());
                return (List<BullhornEntity>) list;
            }
        }
        return new ArrayList<>();
    }
}

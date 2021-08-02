package com.bullhorn.dataloader.task;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Level;

import com.bullhorn.dataloader.data.ActionTotals;
import com.bullhorn.dataloader.data.Cell;
import com.bullhorn.dataloader.data.CsvFileWriter;
import com.bullhorn.dataloader.data.Result;
import com.bullhorn.dataloader.data.Row;
import com.bullhorn.dataloader.enums.EntityInfo;
import com.bullhorn.dataloader.rest.Cache;
import com.bullhorn.dataloader.rest.CompleteUtil;
import com.bullhorn.dataloader.rest.Field;
import com.bullhorn.dataloader.rest.RestApi;
import com.bullhorn.dataloader.util.FindUtil;
import com.bullhorn.dataloader.util.PrintUtil;
import com.bullhorn.dataloader.util.PropertyFileUtil;
import com.bullhorn.dataloader.util.StringConsts;
import com.bullhornsdk.data.model.entity.core.type.BullhornEntity;
import com.bullhornsdk.data.model.entity.core.type.QueryEntity;
import com.bullhornsdk.data.model.entity.core.type.SearchEntity;
import com.bullhornsdk.data.model.parameter.standard.ParamFactory;
import com.google.common.collect.Lists;

public abstract class AbstractTask implements Runnable {
    static final AtomicInteger rowProcessedCount = new AtomicInteger(0);

    private final ActionTotals actionTotals;
    private final CompleteUtil completeUtil;
    final EntityInfo entityInfo;
    Integer entityId;
    protected final PrintUtil printUtil;
    final PropertyFileUtil propertyFileUtil;
    final RestApi restApi;
    private final Cache cache;
    Row row;

    private final CsvFileWriter csvFileWriter;

    AbstractTask(EntityInfo entityInfo,
                 Row row,
                 CsvFileWriter csvFileWriter,
                 PropertyFileUtil propertyFileUtil,
                 RestApi restApi,
                 PrintUtil printUtil,
                 ActionTotals actionTotals,
                 Cache cache,
                 CompleteUtil completeUtil) {
        this.entityInfo = entityInfo;
        this.row = row;
        this.csvFileWriter = csvFileWriter;
        this.propertyFileUtil = propertyFileUtil;
        this.restApi = restApi;
        this.printUtil = printUtil;
        this.actionTotals = actionTotals;
        this.cache = cache;
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
        Result result = entityId != null ? Result.failure(exception, entityId) : Result.failure(exception);
        printUtil.printAndLog(Level.ERROR, "Row " + row.getNumber() + ": Error " + result.getErrorInfo().getCode() + ": " + result.getErrorDetails());
        return result;
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

        // Handle the situation where results files get locked for a brief period of time
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

    /**
     * Abstract find call that checks to see if the records searched for already exist in the client side cache.
     *
     * Find calls must be different between primary and associated entities. This affects custom objects, primarily.
     * Consider the column: `person.externalID` on the PersonCustomObjectInstance1 entity:
     * - When looking for existing Person records to check for existence, we need a Person lookup for `externalID=`
     * - When looking for existing PersonCustomObjectInstance1 records, we need a PersonCustomObjectInstance1 lookup for `person.externalID=`
     *
     * @param entityExistFields the key/value pairs that make up the search/query string
     * @param returnFields      the field names that make up the fields string
     * @param isPrimaryEntity   true = lookup for entity that we are loading, false = lookup for association
     */
    List<BullhornEntity> findEntities(List<Field> entityExistFields,
                                      Set<String> returnFields,
                                      Boolean isPrimaryEntity) throws InvocationTargetException, IllegalAccessException {
        List<BullhornEntity> entities = new ArrayList<>();

        if (!entityExistFields.isEmpty()) {
            EntityInfo entityInfo = entityExistFields.get(0).getEntityInfo(isPrimaryEntity);

            if (propertyFileUtil.getCaching()) {
                // When caching, return search fields so that entity results can be split apart into partial results for advanced caching
                returnFields.addAll(entityExistFields.stream().map(field -> field.getFieldParameterName(isPrimaryEntity))
                    .collect(Collectors.toSet()));

                List<BullhornEntity> cachedEntities = cache.getEntry(entityInfo, entityExistFields, returnFields);
                if (cachedEntities != null) {
                    entities = cachedEntities;
                    printUtil.log(Level.DEBUG, "--> Using Cached Entry: " + entities.size() + " entities: "
                        + entities.stream().map(BullhornEntity::getId).collect(Collectors.toList()));
                } else {
                    entities = findEntitiesRemote(entityExistFields, returnFields, isPrimaryEntity);
                    printUtil.log(Level.DEBUG, "--> Adding Cache Entry from Rest Call: " + entities.size() + " entities: "
                        + entities.stream().map(BullhornEntity::getId).collect(Collectors.toList()));
                    cache.setEntry(entityInfo, entityExistFields, returnFields, entities);
                }
            } else {
                entities = findEntitiesRemote(entityExistFields, returnFields, isPrimaryEntity);
            }
        }

        return entities;
    }

    /**
     * Calls findEntities for only active entities that are not soft-deleted, with special check for disabled corporate users still being active.
     */
    List<BullhornEntity> findActiveEntities(List<Field> entityExistFields,
                                            Set<String> returnFields,
                                            Boolean isPrimaryEntity) throws InvocationTargetException, IllegalAccessException {
        List<BullhornEntity> entities = Lists.newArrayList();
        if (!entityExistFields.isEmpty()) {
            EntityInfo entityInfo = entityExistFields.get(0).getEntityInfo(isPrimaryEntity);
            if (entityInfo == EntityInfo.PERSON) {
                returnFields.add(StringConsts.IS_DELETED);
            } else if (entityInfo.isSoftDeletable()) {
                Cell isDeletedCell = new Cell(StringConsts.IS_DELETED, FindUtil.getIsDeletedValue(entityInfo, false));
                Field isDeletedField = new Field(entityInfo, isDeletedCell, true, propertyFileUtil.getDateParser());
                entityExistFields.add(isDeletedField);
            }

            entities = findEntities(entityExistFields, returnFields, isPrimaryEntity);

            if (entityInfo == EntityInfo.PERSON) {
                entities = entities.stream().filter(FindUtil::isPersonActive).collect(Collectors.toList());
            }
        }
        return entities;
    }

    /**
     * Abstract find call that calls the low-level search/query methods in the RestAPI (As opposed to finding them in the client side cache)
     */
    @SuppressWarnings("unchecked")
    private <B extends BullhornEntity, S extends SearchEntity, Q extends QueryEntity> List<BullhornEntity> findEntitiesRemote(
        List<Field> entityExistFields, Set<String> returnFields, Boolean isPrimaryEntity) {
        EntityInfo entityInfo = entityExistFields.get(0).getEntityInfo(isPrimaryEntity);
        if (entityInfo.isSearchEntity()) {
            String searchString = FindUtil.getLuceneSearch(entityExistFields, propertyFileUtil, isPrimaryEntity);
            List<B> list = (List<B>) restApi.searchForList((Class<S>) entityInfo.getEntityClass(),
                searchString, returnFields, ParamFactory.searchParams());
            return (List<BullhornEntity>) list;
        } else {
            String searchString = FindUtil.getSqlQuery(entityExistFields, propertyFileUtil, isPrimaryEntity);
            List<B> list = (List<B>) restApi.queryForList((Class<Q>) entityInfo.getEntityClass(),
                searchString, returnFields, ParamFactory.queryParams());
            return (List<BullhornEntity>) list;
        }
    }
}

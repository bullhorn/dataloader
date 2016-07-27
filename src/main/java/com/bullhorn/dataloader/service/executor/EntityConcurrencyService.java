package com.bullhorn.dataloader.service.executor;

import com.bullhorn.dataloader.service.api.BullhornAPI;
import com.bullhorn.dataloader.service.api.BullhornApiAssociator;
import com.bullhorn.dataloader.service.csv.CsvFileReader;
import com.bullhorn.dataloader.service.csv.CsvFileWriter;
import com.bullhorn.dataloader.service.csv.JsonRow;
import com.bullhorn.dataloader.service.csv.Result;
import com.bullhorn.dataloader.service.query.EntityQuery;
import com.bullhorn.dataloader.task.DeleteTask;
import com.bullhorn.dataloader.task.LoadTask;
import com.bullhorn.dataloader.util.ActionTotals;
import com.bullhorn.dataloader.util.PrintUtil;
import com.bullhorn.dataloader.util.PropertyFileUtil;
import com.google.common.cache.LoadingCache;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Responsible for executing tasks to process rows in a CSV input file.
 */
public class EntityConcurrencyService {

    private final ExecutorService executorService;
    private final String entityName;
    private final CsvFileReader csvReader;
    private final CsvFileWriter csvWriter;
    private final BullhornAPI bhApi;
    private final BullhornApiAssociator bullhornApiAssociator;
    private final LoadingCache<EntityQuery, Result> associationCache;
    private final PropertyFileUtil propertyFileUtil;
    private final PrintUtil printUtil = new PrintUtil();
    private ActionTotals actionTotals = new ActionTotals();

    private final Logger log = LogManager.getLogger(EntityConcurrencyService.class);

    public EntityConcurrencyService(String entityName,
                                    CsvFileReader csvReader,
                                    CsvFileWriter csvWriter,
                                    BullhornAPI bhApi,
                                    BullhornApiAssociator bullhornApiAssociator,
                                    ExecutorService executorService,
                                    LoadingCache<EntityQuery, Result> associationCache,
                                    PropertyFileUtil propertyFileUtil) {
        this.entityName = entityName;
        this.csvReader = csvReader;
        this.csvWriter = csvWriter;
        this.bhApi = bhApi;
        this.bullhornApiAssociator = bullhornApiAssociator;
        this.executorService = executorService;
        this.associationCache = associationCache;
        this.propertyFileUtil = propertyFileUtil;
    }

    public void runLoadProcess() throws InterruptedException {
        for (JsonRow jsonRow : csvReader) {
            LoadTask loadTask = new LoadTask(entityName, bhApi, bullhornApiAssociator, jsonRow, associationCache, csvWriter, propertyFileUtil);
            executorService.execute(loadTask);
        }
        executorService.shutdown();
        while(!executorService.awaitTermination(1, TimeUnit.MINUTES));
        printUtil.printActionTotals(actionTotals);
    }

    public void runDeleteProcess() throws InterruptedException {
        for (JsonRow jsonRow : csvReader) {
            DeleteTask deleteTask = new DeleteTask(entityName, bhApi, jsonRow, csvWriter, propertyFileUtil);
            executorService.execute(deleteTask);
        }
        executorService.shutdown();
        while(!executorService.awaitTermination(1, TimeUnit.MINUTES));
        printUtil.printActionTotals(actionTotals);
    }
}

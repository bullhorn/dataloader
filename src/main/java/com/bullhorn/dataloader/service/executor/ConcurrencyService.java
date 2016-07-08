package com.bullhorn.dataloader.service.executor;

import java.util.concurrent.ExecutorService;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.bullhorn.dataloader.service.api.BullhornAPI;
import com.bullhorn.dataloader.service.api.BullhornApiAssociator;
import com.bullhorn.dataloader.service.csv.CsvFileReader;
import com.bullhorn.dataloader.service.csv.CsvFileWriter;
import com.bullhorn.dataloader.service.csv.JsonRow;
import com.bullhorn.dataloader.service.csv.Result;
import com.bullhorn.dataloader.service.query.EntityQuery;
import com.bullhorn.dataloader.util.PropertyFileUtil;
import com.google.common.cache.LoadingCache;

/**
 * Responsible for executing tasks to process rows in a CSV input file.
 */
public class ConcurrencyService {

    private final ExecutorService executorService;
    private final String entityName;
    private final CsvFileReader csvReader;
    private final CsvFileWriter csvWriter;
    private final BullhornAPI bhApi;
    private final BullhornApiAssociator bullhornApiAssociator;
    private final LoadingCache<EntityQuery, Result> associationCache;
    private final PropertyFileUtil propertyFileUtil;

    private final Logger log = LogManager.getLogger(ConcurrencyService.class);

    public ConcurrencyService(String entityName,
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

    public void runProcess() {
        try {
            for (JsonRow jsonRow : csvReader) {
                LoadTask loadTask = new LoadTask(entityName, bhApi, bullhornApiAssociator, jsonRow, associationCache, csvWriter, propertyFileUtil);
                loadTask.setEntity(entityName);
                executorService.execute(loadTask);
            }
            executorService.shutdown();
        } catch (Exception e) {
            if (executorService != null) {
                executorService.shutdown();
            }
            log.error(e);
        }
    }
}

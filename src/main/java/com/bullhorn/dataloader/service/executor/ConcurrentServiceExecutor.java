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
import com.google.common.cache.LoadingCache;

public class ConcurrentServiceExecutor {

    private final ExecutorService executorService;
    private final String entityName;
    private final CsvFileReader csvReader;
    private final CsvFileWriter csvWriter;
    private final BullhornAPI bhApi;
    private final BullhornApiAssociator bullhornApiAssociator;
    private final LoadingCache<EntityQuery, Result> associationCache;

    private final Logger log = LogManager.getLogger(ConcurrentServiceExecutor.class);

    public ConcurrentServiceExecutor(String entityName,
                                     CsvFileReader csvReader,
                                     CsvFileWriter csvWriter,
                                     BullhornAPI bhApi,
                                     BullhornApiAssociator bullhornApiAssociator,
                                     ExecutorService executorService,
                                     LoadingCache<EntityQuery, Result> associationCache) {
        this.entityName = entityName;
        this.csvReader = csvReader;
        this.csvWriter = csvWriter;
        this.bhApi = bhApi;
        this.bullhornApiAssociator = bullhornApiAssociator;
        this.executorService = executorService;
        this.associationCache = associationCache;
    }

    public void runProcess() {
        try {
            for (JsonRow row : csvReader) {
                JsonService service = new JsonService(entityName, bhApi, bullhornApiAssociator, row, associationCache, csvWriter);
                service.setEntity(entityName);
                executorService.execute(service);
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

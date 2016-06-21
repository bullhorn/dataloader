package com.bullhorn.dataloader.service.executor;

import java.util.Optional;
import java.util.concurrent.ExecutorService;

import com.bullhorn.dataloader.service.api.BullhornAPI;
import com.bullhorn.dataloader.service.api.BullhornApiAssociator;
import com.bullhorn.dataloader.service.csv.CsvToJson;
import com.bullhorn.dataloader.service.csv.JsonRow;
import com.bullhorn.dataloader.service.query.EntityQuery;
import com.google.common.cache.LoadingCache;
import org.apache.logging.log4j.LogManager;
 import org.apache.logging.log4j.Logger;

public class ConcurrentServiceExecutor {

    private final ExecutorService executorService;
    private final String entityName;
    private final CsvToJson csvItr;
    private final BullhornAPI bhApi;
    private final BullhornApiAssociator bullhornApiAssociator;
    private final LoadingCache<EntityQuery, Optional<Integer>> associationCache;

    private final Logger log = LogManager.getLogger(ConcurrentServiceExecutor.class);

    public ConcurrentServiceExecutor(String entityName,
                                     CsvToJson csvItr,
                                     BullhornAPI bhApi,
                                     BullhornApiAssociator bullhornApiAssociator,
                                     ExecutorService executorService,
                                     LoadingCache<EntityQuery, Optional<Integer>> associationCache) {
        this.entityName = entityName;
        this.bhApi = bhApi;
        this.bullhornApiAssociator = bullhornApiAssociator;
        this.executorService = executorService;
        this.csvItr = csvItr;
        this.associationCache = associationCache;
    }

    public void runProcess() {
        try {
            for(JsonRow row : csvItr) {
                JsonService service = new JsonService(entityName, bhApi, bullhornApiAssociator, row, associationCache);
                service.setEntity(entityName);
                executorService.execute(service);
            }
            executorService.shutdown();
        }
        catch (Exception e) {
            if (executorService != null) {
                executorService.shutdown();
            }
            log.error(e);
        }
    }
}

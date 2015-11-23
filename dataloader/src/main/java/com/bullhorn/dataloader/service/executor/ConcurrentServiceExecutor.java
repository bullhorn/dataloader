package com.bullhorn.dataloader.service.executor;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bullhorn.dataloader.service.api.BullhornAPI;
import com.bullhorn.dataloader.service.api.EntityInstance;
import com.bullhorn.dataloader.service.csv.CsvToJson;
import com.bullhorn.dataloader.service.csv.JsonRow;
import com.bullhorn.dataloader.service.query.EntityQuery;
import com.google.common.cache.LoadingCache;


public class ConcurrentServiceExecutor {

    private final ExecutorService executorService;
    private final String entityName;
    private final CsvToJson csvItr;
    private final BullhornAPI bhApi;
    private final LoadingCache<EntityQuery, Optional<Integer>> associationCache;

    private final Log log = LogFactory.getLog(ConcurrentServiceExecutor.class);
    private final Set<List<EntityInstance>> seenFlag;

    public ConcurrentServiceExecutor(String entityName,
                                     CsvToJson csvItr,
                                     BullhornAPI bhApi,
                                     ExecutorService executorService,
                                     LoadingCache<EntityQuery, Optional<Integer>> associationCache,
                                     Set<List<EntityInstance>> seenFlag) {
        this.entityName = entityName;
        this.bhApi = bhApi;
        this.executorService = executorService;
        this.csvItr = csvItr;
        this.associationCache = associationCache;
        this.seenFlag = seenFlag;
    }

    public void runProcess() {
        try {
            for(JsonRow row : csvItr) {
                JsonService service = new JsonService(entityName, bhApi, row, associationCache, seenFlag);
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

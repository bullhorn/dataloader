package com.bullhorn.dataloader.service.executor;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bullhorn.dataloader.service.api.EntityInstance;
import com.bullhorn.dataloader.service.csv.CsvToJson;
import com.bullhorn.dataloader.service.csv.JsonRow;
import com.bullhorn.dataloader.service.query.AssociationQuery;
import com.bullhorn.dataloader.service.api.BullhornAPI;
import com.google.common.cache.LoadingCache;


public class ConcurrentServiceExecutor {

    private final ExecutorService executorService;
    private final String entityName;
    private final CsvToJson csvItr;
    private final BullhornAPI bhApi;
    private final LoadingCache<AssociationQuery, Optional<Integer>> associationCache;

    private final Log log = LogFactory.getLog(ConcurrentServiceExecutor.class);
    private final Set<EntityInstance> seenFlag;

    public ConcurrentServiceExecutor(String entityName,
                                     CsvToJson csvItr,
                                     BullhornAPI bhApi,
                                     ExecutorService executorService,
                                     LoadingCache<AssociationQuery, Optional<Integer>> assocationCache,
                                     Set<EntityInstance> seenFlag) {
        this.entityName = entityName;
        this.bhApi = bhApi;
        this.executorService = executorService;
        this.csvItr = csvItr;
        this.associationCache = assocationCache;
        this.seenFlag = seenFlag;
    }

    public void runProcess() {
        try {
            // loop through records
            for(JsonRow row : csvItr) {
                JsonService service = new JsonService(entityName, bhApi, row, associationCache, seenFlag);
                service.setEntity(entityName);
                executorService.execute(service);
            }

            // shut the executor service down
            executorService.shutdown();
        }
        // if something fails, shutdown
        catch (Exception e) {
            if (executorService != null) {
                executorService.shutdown();
            }
            log.error(e);
        }
    }
}

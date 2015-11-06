package com.bullhorn.dataloader.service;

import java.util.Map;
import java.util.concurrent.ExecutorService;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bullhorn.dataloader.util.BullhornAPI;
import com.bullhorn.dataloader.util.CsvToJson;


public class ConcurrentServiceExecutor {

    private final ExecutorService executorService;
    private final String entityName;
    private final CsvToJson csvItr;
    private final BullhornAPI bhApi;

    private final Log log = LogFactory.getLog(ConcurrentServiceExecutor.class);

    public ConcurrentServiceExecutor(String entityName, CsvToJson csvItr, BullhornAPI bhApi, ExecutorService executorService) {
        this.entityName = entityName;
        this.bhApi = bhApi;
        this.executorService = executorService;
        this.csvItr = csvItr;
    }

    public void runProcess() {
        try {

            // loop through records
            for(Map<String, Object> row : csvItr) {
                JsonService service = new JsonService(entityName, bhApi, row);
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
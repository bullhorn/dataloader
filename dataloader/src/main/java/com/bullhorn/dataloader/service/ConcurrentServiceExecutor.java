package com.bullhorn.dataloader.service;

import java.util.List;
import java.util.concurrent.ExecutorService;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bullhorn.dataloader.util.BullhornAPI;


public class ConcurrentServiceExecutor {

    private final MasterDataService masterDataService;
    private final ExecutorService executorService;
    private final String className;
    private final List<Object> records;
    private final BullhornAPI bhapi;

    private final Log log = LogFactory.getLog(ConcurrentServiceExecutor.class);

    public ConcurrentServiceExecutor(String className, List<Object> records,
                                     MasterDataService masterDataService, BullhornAPI bhapi,
                                     ExecutorService executorService) {
        this.className = className;
        this.records = records;
        this.masterDataService = masterDataService;
        this.bhapi = bhapi;
        this.executorService = executorService;
    }

    public void runProcess() {
        try {

            // loop through records
            for (Object obj : records) {
                // generic concurrent import implementation
                // there is a 1-1 mapping of entity and service
                Class<?> serviceClass = Class.forName(this.getClass().getPackage().getName() + "." + className);
                // instantiate it
                ConcurrentServiceInterface service = (ConcurrentServiceInterface) serviceClass.newInstance();
                // pass the domain object returned from CSV
                service.setObj(obj);
                // pass master data cache
                service.setMasterDataService(masterDataService);
                // pass token
                service.setBhapi(bhapi);
                // execute
                executorService.execute((Runnable) service);
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

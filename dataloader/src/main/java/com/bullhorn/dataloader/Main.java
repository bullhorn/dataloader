package com.bullhorn.dataloader;

import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.lang3.text.WordUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bullhorn.dataloader.service.ConcurrentServiceExecutor;
import com.bullhorn.dataloader.service.MasterDataService;
import com.bullhorn.dataloader.util.BullhornAPI;
import com.bullhorn.dataloader.util.CsvToJson;
import com.bullhorn.dataloader.util.FileUtil;

public class Main {

    private static Log log = LogFactory.getLog(Main.class);

    public static void main(String[] args) {

        try {

            // Entity to be imported and path to the CSV are passed in at runtime
            FileUtil fileUtil = new FileUtil();
            Properties props = fileUtil.getProps("dataloader.properties");

            // Create API object. Pass this object to each import service via ConcurrentServiceExecutor
            // BullhornAPI contains REST session and helper methods to communicate with Bullhorn
            BullhornAPI bhapi = new BullhornAPI(props);


            String entity = args[0];
            String filePath = args[1];
            Integer numThreads = Integer.valueOf(props.getProperty("numThreads"));

            // Cache master data - category, skill, business sector, internal users
            MasterDataService masterDataService = new MasterDataService();
            masterDataService.setBhapi(bhapi);

            CsvToJson csvToJson = new CsvToJson(filePath, bhapi.getMetaDataTypes(entity));
            ExecutorService executorService = Executors.newFixedThreadPool(numThreads);
            // Import to Bullhorn

            ConcurrentServiceExecutor impSvc = new ConcurrentServiceExecutor(
                    WordUtils.capitalize(entity),
                    csvToJson,
                    bhapi,
                    executorService
            );

            // Start import
            impSvc.runProcess();

        } catch (Exception e) {
            log.error(e);
        }
    }
}
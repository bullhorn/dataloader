package com.bullhorn.dataloader;

import java.io.IOException;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.lang3.text.WordUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bullhorn.dataloader.service.query.AssociationCache;
import com.bullhorn.dataloader.service.executor.ConcurrentServiceExecutor;
import com.bullhorn.dataloader.service.query.AssociationQuery;
import com.bullhorn.dataloader.service.api.BullhornAPI;
import com.bullhorn.dataloader.service.csv.CsvToJson;
import com.bullhorn.dataloader.util.FileUtil;
import com.bullhorn.dataloader.util.TemplateUtil;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.LoadingCache;

public class Main {

    private static Log log = LogFactory.getLog(Main.class);

    public static void main(String[] args) throws IOException {

        // Entity to be imported and path to the CSV are passed in at runtime
        FileUtil fileUtil = new FileUtil();
        Properties properties = fileUtil.getProps("dataloader.properties");
        // Cache master data - category, skill, business sector, internal users

        // Create API object. Pass this object to each import service via ConcurrentServiceExecutor
        // BullhornAPI contains REST session and helper methods to communicate with Bullhorn
        BullhornAPI bhapi = new BullhornAPI(properties);
        LoadingCache<AssociationQuery, Optional<Integer>> associationCache = CacheBuilder.newBuilder()
                .maximumSize(10000)
                .build(new AssociationCache(bhapi));

        if ("template".equals(args[0])) {
            TemplateUtil templateUtil = new TemplateUtil(bhapi);
            try {
                templateUtil.writeExampleEntityCsv(args[1]);
            } catch (Exception e) {
                log.error(e);
            }
        } else {
            try {
                String entity = args[0];
                String filePath = args[1];
                Integer numThreads = Integer.valueOf(properties.getProperty("numThreads"));

                CsvToJson csvToJson = new CsvToJson(filePath, bhapi.getMetaDataTypes(entity));
                ExecutorService executorService = Executors.newFixedThreadPool(numThreads);

                // Import to Bullhorn
                ConcurrentServiceExecutor impSvc = new ConcurrentServiceExecutor(
                        WordUtils.capitalize(entity),
                        csvToJson,
                        bhapi,
                        executorService,
                        associationCache
                );

                // Start import
                impSvc.runProcess();

            } catch (Exception e) {
                log.error(e);
            }
        }
    }
}
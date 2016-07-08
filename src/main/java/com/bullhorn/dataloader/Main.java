package com.bullhorn.dataloader;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.lang3.text.WordUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.bullhorn.dataloader.service.api.BullhornAPI;
import com.bullhorn.dataloader.service.api.BullhornApiAssociator;
import com.bullhorn.dataloader.service.csv.CsvFileReader;
import com.bullhorn.dataloader.service.csv.CsvFileWriter;
import com.bullhorn.dataloader.service.csv.Result;
import com.bullhorn.dataloader.service.executor.RowWorkerExecutor;
import com.bullhorn.dataloader.service.query.EntityCache;
import com.bullhorn.dataloader.service.query.EntityQuery;
import com.bullhorn.dataloader.util.PropertyFileUtil;
import com.bullhorn.dataloader.util.TemplateUtil;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.LoadingCache;

public class Main {

    private static Logger log = LogManager.getLogger(Main.class);

    /**
     * Entry point for the application.
     *
     * @param args The user's command line parameters
     */
    public static void main(String[] args) {
        try {
            PropertyFileUtil propertyFileUtil = new PropertyFileUtil("dataloader.properties");
            BullhornAPI bhApi = new BullhornAPI(propertyFileUtil);
            BullhornApiAssociator bullhornApiAssociator = new BullhornApiAssociator(bhApi);

            bhApi.createSession();

            if ("template".equals(args[0])) {
                createTemplate(args[1], bhApi);
            } else {
                String entity = args[0];
                String filePath = args[1];
                loadCsv(entity, filePath, bhApi, bullhornApiAssociator, propertyFileUtil);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            log.error(e);
        }
    }

    static void createTemplate(String entity, BullhornAPI bhapi) {
        TemplateUtil templateUtil = new TemplateUtil(bhapi);
        try {
            templateUtil.writeExampleEntityCsv(entity);
        } catch (Exception e) {
            log.error(e);
        }
    }

    static void loadCsv(String entity, String filePath, BullhornAPI bhApi, BullhornApiAssociator bullhornApiAssociator, PropertyFileUtil propertyFileUtil) {

        final LoadingCache<EntityQuery, Result> associationCache = CacheBuilder.newBuilder()
                .maximumSize(propertyFileUtil.getCacheSize())
                .build(new EntityCache(bhApi));
        try {
            bhApi.frontLoad();
            final CsvFileReader csvFileReader = new CsvFileReader(filePath, bhApi.getRootMetaDataTypes(entity));
            final CsvFileWriter csvFileWriter = new CsvFileWriter(filePath, csvFileReader.getHeaders());

            final ExecutorService executorService = Executors.newFixedThreadPool(propertyFileUtil.getNumThreads());
            final RowWorkerExecutor executor = new RowWorkerExecutor(
                    WordUtils.capitalize(entity),
                    csvFileReader,
                    csvFileWriter,
                    bhApi,
                    bullhornApiAssociator,
                    executorService,
                    associationCache,
                    propertyFileUtil
            );
            executor.runProcess();
        } catch (IOException e) {
            log.error(e);
        }
    }
}

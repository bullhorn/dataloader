package com.bullhorn.dataloader;

import java.io.IOException;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.lang3.text.WordUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.bullhorn.dataloader.service.api.BullhornAPI;
import com.bullhorn.dataloader.service.api.BullhornApiAssociator;
import com.bullhorn.dataloader.service.csv.CsvToJson;
import com.bullhorn.dataloader.service.executor.ConcurrentServiceExecutor;
import com.bullhorn.dataloader.service.query.EntityCache;
import com.bullhorn.dataloader.service.query.EntityQuery;
import com.bullhorn.dataloader.util.PropertyFileUtil;
import com.bullhorn.dataloader.util.TemplateUtil;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.LoadingCache;

public class Main {

    private static Logger log = LogManager.getLogger(Main.class);

    public static void main(String[] args) throws IOException {
        final BullhornAPI bhapi = getBullhornAPI();
        final BullhornApiAssociator bullhornApiAssociator = new BullhornApiAssociator(bhapi);

        bhapi.createSession();

        if ("template".equals(args[0])) {
            createTemplate(args[1], bhapi);
        } else {
            String entity = args[0];
            String filePath = args[1];
            loadCsv(entity, filePath, bhapi, bullhornApiAssociator);
        }
    }

    static BullhornAPI getBullhornAPI() throws IOException {
        final PropertyFileUtil propertyFileUtil = new PropertyFileUtil();
        final Properties properties = propertyFileUtil.getProps("dataloader.properties");
        return new BullhornAPI(properties);
    }

    static void createTemplate(String entity, BullhornAPI bhapi) {
        TemplateUtil templateUtil = new TemplateUtil(bhapi);
        try {
            templateUtil.writeExampleEntityCsv(entity);
        } catch (Exception e) {
            log.error(e);
        }
    }

    static void loadCsv(String entity, String filePath, BullhornAPI bhapi, BullhornApiAssociator bullhornApiAssociator) {

        final LoadingCache<EntityQuery, Optional<Integer>> associationCache = CacheBuilder.newBuilder()
                .maximumSize(bhapi.getCacheSize())
                .build(new EntityCache(bhapi));
        try {
            bhapi.frontLoad();
            final CsvToJson csvToJson = new CsvToJson(filePath, bhapi.getRootMetaDataTypes(entity));
            final ExecutorService executorService = Executors.newFixedThreadPool(bhapi.getThreadSize());
            final ConcurrentServiceExecutor impSvc = new ConcurrentServiceExecutor(
                    WordUtils.capitalize(entity),
                    csvToJson,
                    bhapi,
                    bullhornApiAssociator,
                    executorService,
                    associationCache
            );
            impSvc.runProcess();
        } catch (IOException e) {
            log.error(e);
        }
    }
}

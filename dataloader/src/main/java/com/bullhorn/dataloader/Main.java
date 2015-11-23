package com.bullhorn.dataloader;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.lang3.text.WordUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bullhorn.dataloader.service.api.BullhornAPI;
import com.bullhorn.dataloader.service.api.EntityInstance;
import com.bullhorn.dataloader.service.csv.CsvToJson;
import com.bullhorn.dataloader.service.executor.ConcurrentServiceExecutor;
import com.bullhorn.dataloader.service.query.EntityCache;
import com.bullhorn.dataloader.service.query.EntityQuery;
import com.bullhorn.dataloader.util.FileUtil;
import com.bullhorn.dataloader.util.TemplateUtil;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Sets;

public class Main {

    private static Log log = LogFactory.getLog(Main.class);

    public static void main(String[] args) throws IOException {
        final BullhornAPI bhapi = getBullhornAPI();

        if ("template".equals(args[0])) {
            createTemplate(args[1], bhapi);
        } else {
            String entity = args[0];
            String filePath = args[1];
            loadCsv(entity, filePath, bhapi);
        }
    }

    static BullhornAPI getBullhornAPI() throws IOException {
        final FileUtil fileUtil = new FileUtil();
        final Set<List<EntityInstance>> seenFlag = Sets.newConcurrentHashSet();
        final Properties properties = fileUtil.getProps("dataloader.properties");
        return new BullhornAPI(properties, seenFlag);
    }

    static void createTemplate(String entity, BullhornAPI bhapi) {
        TemplateUtil templateUtil = new TemplateUtil(bhapi);
        try {
            templateUtil.writeExampleEntityCsv(entity);
        } catch (Exception e) {
            log.error(e);
        }
    }

    static void loadCsv(String entity, String filePath, BullhornAPI bhapi) {

        final LoadingCache<EntityQuery, Optional<Integer>> associationCache = CacheBuilder.newBuilder()
                .maximumSize(bhapi.getCacheSize())
                .build(new EntityCache(bhapi));
        try {
            final CsvToJson csvToJson = new CsvToJson(filePath, bhapi.getMetaDataTypes(entity));
            final ExecutorService executorService = Executors.newFixedThreadPool(bhapi.getThreadSize());
            final ConcurrentServiceExecutor impSvc = new ConcurrentServiceExecutor(
                    WordUtils.capitalize(entity),
                    csvToJson,
                    bhapi,
                    executorService,
                    associationCache
            );
            impSvc.runProcess();
        } catch (IOException e) {
            log.error(e);
        }
    }
}
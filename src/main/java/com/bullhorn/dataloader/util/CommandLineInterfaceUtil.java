package com.bullhorn.dataloader.util;

import com.bullhorn.dataloader.service.api.BullhornAPI;
import com.bullhorn.dataloader.service.api.BullhornApiAssociator;
import com.bullhorn.dataloader.service.consts.Method;
import com.bullhorn.dataloader.service.csv.CsvFileWriter;
import com.bullhorn.dataloader.service.csv.CsvFileReader;
import com.bullhorn.dataloader.service.csv.Result;
import com.bullhorn.dataloader.service.executor.EntityAttachmentConcurrencyService;
import com.bullhorn.dataloader.service.executor.EntityConcurrencyService;
import com.bullhorn.dataloader.service.query.EntityCache;
import com.bullhorn.dataloader.service.query.EntityQuery;
import com.bullhornsdk.data.api.BullhornData;
import com.bullhornsdk.data.api.BullhornRestCredentials;
import com.bullhornsdk.data.api.StandardBullhornData;
import com.csvreader.CsvReader;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.LoadingCache;
import org.apache.commons.lang3.text.WordUtils;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CommandLineInterfaceUtil {

    protected BullhornData getBullhornData(PropertyFileUtil propertyFileUtil) throws Exception {
        BullhornData bullhornData = new StandardBullhornData(getBullhornRestCredentials(propertyFileUtil));
        return bullhornData;
    }

    protected BullhornRestCredentials getBullhornRestCredentials(PropertyFileUtil propertyFileUtil) throws Exception {
        BullhornRestCredentials bullhornRestCredentials = new BullhornRestCredentials();
        bullhornRestCredentials.setPassword(propertyFileUtil.getPassword());
        bullhornRestCredentials.setRestAuthorizeUrl(propertyFileUtil.getAuthorizeUrl());
        bullhornRestCredentials.setRestClientId(propertyFileUtil.getClientId());
        bullhornRestCredentials.setRestClientSecret(propertyFileUtil.getClientSecret());
        bullhornRestCredentials.setRestLoginUrl(propertyFileUtil.getLoginUrl());
        bullhornRestCredentials.setRestTokenUrl(propertyFileUtil.getTokenUrl());
        bullhornRestCredentials.setUsername(propertyFileUtil.getUsername());
        bullhornRestCredentials.setRestSessionMinutesToLive("60");
        return bullhornRestCredentials;
    }

    protected PropertyFileUtil getPropertyFileUtil() throws IOException {
        return new PropertyFileUtil("dataloader.properties");
    }

    protected ExecutorService getExecutorService(PropertyFileUtil propertyFileUtil) throws IOException {
        return Executors.newFixedThreadPool(propertyFileUtil.getNumThreads());
    }

    protected void template(String entityName) throws Exception {
        final BullhornAPI bhApi = createSession();
        TemplateUtil templateUtil = new TemplateUtil(bhApi);
        System.out.println("Creating Template for " + entityName);
        templateUtil.writeExampleEntityCsv(entityName);
    }

    protected BullhornAPI createSession() throws Exception {
        final PropertyFileUtil propertyFileUtil = getPropertyFileUtil();
        final BullhornAPI bhApi = new BullhornAPI(propertyFileUtil);
        bhApi.createSession();
        return bhApi;
    }

    protected EntityConcurrencyService createEntityConcurrencyService(Method method, String entity, String filePath) throws Exception {
        final BullhornAPI bhApi = createSession();
        final BullhornApiAssociator bullhornApiAssociator = new BullhornApiAssociator(bhApi);
        final LoadingCache<EntityQuery, Result> associationCache = CacheBuilder.newBuilder()
                .maximumSize(bhApi.getPropertyFileUtil().getCacheSize())
                .build(new EntityCache(bhApi));

        bhApi.frontLoad();
        final CsvFileReader csvFileReader = new CsvFileReader(filePath, bhApi.getRootMetaDataTypes(entity));
        final CsvFileWriter csvFileWriter = new CsvFileWriter(method, filePath, csvFileReader.getHeaders());

        final ExecutorService executorService = getExecutorService(getPropertyFileUtil());
        final EntityConcurrencyService entityConcurrencyService = new EntityConcurrencyService(
                WordUtils.capitalize(entity),
                csvFileReader,
                csvFileWriter,
                bhApi,
                bullhornApiAssociator,
                executorService,
                associationCache,
                bhApi.getPropertyFileUtil()
        );

        return entityConcurrencyService;
    }

    protected EntityAttachmentConcurrencyService createEntityAttachmentConcurrencyService(Method method, String entityName, String filePath) throws Exception {
        final PropertyFileUtil propertyFileUtil = getPropertyFileUtil();

        final BullhornData bullhornData = getBullhornData(propertyFileUtil);
        final ExecutorService executorService = getExecutorService(propertyFileUtil);
        final CsvReader csvReader = new CsvReader(filePath);
        csvReader.readHeaders();
        final CsvFileWriter csvFileWriter = new CsvFileWriter(method, filePath, csvReader.getHeaders());

        EntityAttachmentConcurrencyService entityAttachmentConcurrencyService = new EntityAttachmentConcurrencyService(
                entityName,
                csvReader,
                csvFileWriter,
                executorService,
                propertyFileUtil,
                bullhornData
        );

        return entityAttachmentConcurrencyService;
    }

}

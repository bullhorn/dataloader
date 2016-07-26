package com.bullhorn.dataloader.service;

import java.util.concurrent.ExecutorService;

import com.bullhorn.dataloader.service.executor.EntityAttachmentConcurrencyService;
import com.bullhorn.dataloader.util.CommandLineInterfaceUtil;
import com.bullhornsdk.data.api.BullhornData;
import com.csvreader.CsvReader;
import org.apache.commons.lang3.text.WordUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.bullhorn.dataloader.service.api.BullhornAPI;
import com.bullhorn.dataloader.service.api.BullhornApiAssociator;
import com.bullhorn.dataloader.service.csv.EntityCsvReader;
import com.bullhorn.dataloader.service.csv.CsvFileWriter;
import com.bullhorn.dataloader.service.csv.Result;
import com.bullhorn.dataloader.service.executor.EntityConcurrencyService;
import com.bullhorn.dataloader.service.query.EntityCache;
import com.bullhorn.dataloader.service.query.EntityQuery;
import com.bullhorn.dataloader.util.PrintUtil;
import com.bullhorn.dataloader.util.PropertyFileUtil;
import com.bullhorn.dataloader.util.TemplateUtil;
import com.bullhorn.dataloader.util.ValidationUtil;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.LoadingCache;

public class CommandLineInterface extends CommandLineInterfaceUtil {

    private Logger log = LogManager.getLogger(CommandLineInterface.class);

    final private PrintUtil printUtil;
    final private ValidationUtil validationUtil;

    public CommandLineInterface() {
        printUtil = new PrintUtil();
        validationUtil = new ValidationUtil(printUtil);
    }

    /**
     * Starts the Command Line Interface
     *
     * @param args The user's command line parameters
     */
    public void start(String[] args) {
        try {
            if (validationUtil.isValidParameters(args)) {
                if (args.length == 2) {
                    if (args[0].equalsIgnoreCase("template")) {
                        template(args[0], args[1]);
                    }
                } else if (args.length == 3) {
                    if (args[0].equalsIgnoreCase("load")) {
                        load(args[0], args[1], args[2]);
                    }
                    else if (args[0].equalsIgnoreCase("delete")) {
                        delete(args[0], args[1], args[2]);
                    }
                    else if (args[0].equalsIgnoreCase("loadAttachments")) {
                        loadAttachments(args[0], args[1], args[2]);
                    }
                    else {
                        System.out.println("ERROR: Expected the 'delete' keyword, but was provided: " + args[0]);
                        printUtil.printUsage();
                    }
                }
            }
        } catch (Exception e) {
            System.out.println(e);
            log.error(e);
        }
    }

    private void load(String method, String entityName, String filePath) throws Exception {
        if (validationUtil.isLoadableEntity(entityName) && validationUtil.isValidCsvFile(filePath)) {
            System.out.println("Loading " + entityName + " records from: " + filePath);
            EntityConcurrencyService entityConcurrencyService = createEntityConcurrencyService(method, entityName, filePath);
            entityConcurrencyService.runLoadProcess();
        }
    }

    private void delete(String method, String entityName, String filePath) throws Exception {
        if (validationUtil.isDeletableEntity(entityName) && validationUtil.isValidCsvFile(filePath)) {
            System.out.println("Deleting " + entityName + " records from: " + filePath);
            EntityConcurrencyService entityConcurrencyService = createEntityConcurrencyService(method, entityName, filePath);
            entityConcurrencyService.runDeleteProcess();
        }
    }

    private void loadAttachments(String method, String entityName, String filePath) throws Exception {
        if (validationUtil.isDeletableEntity(entityName) && validationUtil.isValidCsvFile(filePath)) {
            System.out.println("Loading " + entityName + " attachments from: " + filePath);
            EntityAttachmentConcurrencyService entityConcurrencyService = createEntityAttachmentConcurrencyService(method, entityName, filePath);
            entityConcurrencyService.runLoadAttchmentProcess();
        }
    }

    private void template(String method, String entityName) throws Exception {
        final BullhornAPI bhApi = createSession();
        TemplateUtil templateUtil = new TemplateUtil(bhApi);
        System.out.println("Creating Template for " + entityName);
        templateUtil.writeExampleEntityCsv(entityName);
    }

    private BullhornAPI createSession() throws Exception {
        final PropertyFileUtil propertyFileUtil = getPropertyFileUtil();
        final BullhornAPI bhApi = new BullhornAPI(propertyFileUtil);
        bhApi.createSession();
        return bhApi;
    }

    private EntityConcurrencyService createEntityConcurrencyService(String method, String entity, String filePath) throws Exception {
        final BullhornAPI bhApi = createSession();
        final BullhornApiAssociator bullhornApiAssociator = new BullhornApiAssociator(bhApi);
        final LoadingCache<EntityQuery, Result> associationCache = CacheBuilder.newBuilder()
                .maximumSize(bhApi.getPropertyFileUtil().getCacheSize())
                .build(new EntityCache(bhApi));

        bhApi.frontLoad();
        final EntityCsvReader entityCsvReader = new EntityCsvReader(filePath, bhApi.getRootMetaDataTypes(entity));
        final CsvFileWriter csvFileWriter = new CsvFileWriter(method, filePath, entityCsvReader.getHeaders());

        final ExecutorService executorService = getExecutorService(getPropertyFileUtil());
        final EntityConcurrencyService entityConcurrencyService = new EntityConcurrencyService(
                WordUtils.capitalize(entity),
                entityCsvReader,
                csvFileWriter,
                bhApi,
                bullhornApiAssociator,
                executorService,
                associationCache,
                bhApi.getPropertyFileUtil()
        );

        return entityConcurrencyService;
    }

    private EntityAttachmentConcurrencyService createEntityAttachmentConcurrencyService(String method, String entityName, String filePath) throws Exception {
        final PropertyFileUtil propertyFileUtil = getPropertyFileUtil();

        final BullhornData bullhornData = getBullhornData(propertyFileUtil);
        final ExecutorService executorService = getExecutorService(propertyFileUtil);
        final CsvReader csvFileReader = new CsvReader(filePath);
        final CsvFileWriter csvFileWriter = new CsvFileWriter(method, filePath, csvFileReader.getHeaders());

        EntityAttachmentConcurrencyService entityAttachmentConcurrencyService = new EntityAttachmentConcurrencyService(
                entityName,
                csvFileReader,
                csvFileWriter,
                executorService,
                propertyFileUtil,
                bullhornData
        );

        return entityAttachmentConcurrencyService;
    }



}

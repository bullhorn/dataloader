package com.bullhorn.dataloader.service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.lang3.text.WordUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.bullhorn.dataloader.meta.MetaMap;
import com.bullhorn.dataloader.service.api.BullhornAPI;
import com.bullhorn.dataloader.service.api.BullhornApiAssociator;
import com.bullhorn.dataloader.service.api.BullhornApiUpdater;
import com.bullhorn.dataloader.service.csv.CsvFileReader;
import com.bullhorn.dataloader.service.csv.CsvFileWriter;
import com.bullhorn.dataloader.service.csv.Result;
import com.bullhorn.dataloader.service.executor.ConcurrencyService;
import com.bullhorn.dataloader.service.query.EntityCache;
import com.bullhorn.dataloader.service.query.EntityQuery;
import com.bullhorn.dataloader.util.PrintUtil;
import com.bullhorn.dataloader.util.PropertyFileUtil;
import com.bullhorn.dataloader.util.TemplateUtil;
import com.bullhorn.dataloader.util.Timer;
import com.bullhorn.dataloader.util.validation.ValidationUtil;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.LoadingCache;

public class CommandLineInterface {

    private Logger log = LogManager.getLogger(CommandLineInterface.class);

    final private PrintUtil printUtil;
    final private ValidationUtil validationUtil;
    final private Timer timer;

    public CommandLineInterface() {
        printUtil = new PrintUtil();
        validationUtil = new ValidationUtil(printUtil);
        timer = new Timer();
    }

    /**
     * Starts the Command Line Interface
     *
     * @param args The user's command line parameters
     */
    public void start(String[] args) {
        log.info("Args: " + String.join(" ", args));

        try {
            if (validationUtil.isValidParameters(args)) {
                if (args.length == 2) {
                    if (args[0].equalsIgnoreCase("template")) {
                        template(args[1]);
                    } else {
                        load(args[0], args[1]);
                    }
                } else if (args.length == 3) {
                    if (args[0].equalsIgnoreCase("delete")) {
                        delete(args[1], args[2]);
                    } else {
                        printAndLog("ERROR: Expected the 'delete' keyword, but was provided: " + args[0]);
                        printUtil.printUsage();
                    }
                }
            }
        } catch (Exception e) {
            printAndLog(e.toString());
        }
    }

    private void load(String entityName, String filePath) throws Exception {
        if (validationUtil.isLoadableEntity(entityName) && validationUtil.isValidCsvFile(filePath)) {
            printAndLog("Loading " + entityName + " records from: " + filePath + "...");
            final BullhornAPI bhApi = createSession();
            ConcurrencyService concurrencyService = createConcurrencyService(entityName, filePath, bhApi);
            concurrencyService.runLoadProcess();
            printAndLog("Completed setup (establishing connection, retrieving meta, front loading) in " + timer.getDurationStringSec());
        }
    }

    private void delete(String entityName, String filePath) throws Exception {
        if (validationUtil.isDeletableEntity(entityName) && validationUtil.isValidCsvFile(filePath)) {
            printAndLog("Deleting " + entityName + " records from: " + filePath + "...");
            final BullhornAPI bhApi = createSession();
            ConcurrencyService concurrencyService = createConcurrencyService(entityName, filePath, bhApi);
            concurrencyService.runDeleteProcess();
            printAndLog("Completed setup (establishing connection, retrieving meta) in " + timer.getDurationStringSec());
        }
    }

    private void template(String entityName) throws Exception {
        printAndLog("Creating Template for " + entityName + "...");
        final BullhornAPI bhApi = createSession();
        TemplateUtil templateUtil = new TemplateUtil(bhApi);
        templateUtil.writeExampleEntityCsv(entityName);
        printAndLog("Generated template in " + timer.getDurationStringSec());
    }

    private BullhornAPI createSession() throws Exception {
        final PropertyFileUtil propertyFileUtil = new PropertyFileUtil("dataloader.properties");
        final BullhornAPI bhApi = new BullhornAPI(propertyFileUtil);
        bhApi.createSession();
        return bhApi;
    }

    private ConcurrencyService createConcurrencyService(String entity, String filePath, BullhornAPI bhApi) throws Exception {
        final BullhornApiUpdater bhApiUpdater = new BullhornApiUpdater(bhApi);
        final BullhornApiAssociator bhApiAssociator = new BullhornApiAssociator(bhApi);

        final LoadingCache<EntityQuery, Result> associationCache = CacheBuilder.newBuilder()
                .maximumSize(bhApi.getPropertyFileUtil().getCacheSize())
                .build(new EntityCache(bhApiUpdater));

        bhApi.frontLoad();
        MetaMap metaMap = bhApi.getRootMetaDataTypes(entity);
        final CsvFileReader csvFileReader = new CsvFileReader(filePath, metaMap);
        final CsvFileWriter csvFileWriter = new CsvFileWriter(filePath, csvFileReader.getHeaders());

        final ExecutorService executorService = Executors.newFixedThreadPool(bhApi.getPropertyFileUtil().getNumThreads());
        final ConcurrencyService concurrencyService = new ConcurrencyService(
                WordUtils.capitalize(entity),
                csvFileReader,
                csvFileWriter,
                bhApi,
                bhApiAssociator,
                executorService,
                associationCache,
                bhApi.getPropertyFileUtil()
        );

        return concurrencyService;
    }

    private void printAndLog(String line) {
        System.out.println(line);
        log.info(line);
    }
}

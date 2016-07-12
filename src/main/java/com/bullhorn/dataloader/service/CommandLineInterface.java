package com.bullhorn.dataloader.service;

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
import com.bullhorn.dataloader.service.executor.ConcurrencyService;
import com.bullhorn.dataloader.service.query.EntityCache;
import com.bullhorn.dataloader.service.query.EntityQuery;
import com.bullhorn.dataloader.util.PrintUtil;
import com.bullhorn.dataloader.util.PropertyFileUtil;
import com.bullhorn.dataloader.util.TemplateUtil;
import com.bullhorn.dataloader.util.ValidationUtil;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.LoadingCache;

public class CommandLineInterface {

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
                        template(args[1]);
                    } else {
                        load(args[0], args[1]);
                    }
                } else if (args.length == 3) {
                    if (args[0].equalsIgnoreCase("delete")) {
                        delete(args[1], args[2]);
                    } else {
                        System.out.println("ERROR: Expected the 'delete' keyword, but was provided: " + args[0]);
                        printUtil.printUsage();
                    }
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            log.error(e);
        }
    }

    private void load(String entityName, String filePath) throws Exception {
        if (validationUtil.isLoadableEntity(entityName) && validationUtil.isValidCsvFile(filePath)) {
            final BullhornAPI bhApi = createSession();
            System.out.println("Loading " + entityName + " records from: " + filePath);
            ConcurrencyService concurrencyService = createConcurrencyService(entityName, filePath, bhApi);
            concurrencyService.runLoadProcess();
        }
    }

    private void delete(String entityName, String filePath) throws Exception {
        if (validationUtil.isDeletableEntity(entityName) && validationUtil.isValidCsvFile(filePath)) {
            final BullhornAPI bhApi = createSession();
            System.out.println("Deleting " + entityName + " records from: " + filePath);
            ConcurrencyService concurrencyService = createConcurrencyService(entityName, filePath, bhApi);
            concurrencyService.runDeleteProcess();
        }
    }

    private void template(String entityName) throws Exception {
        final BullhornAPI bhApi = createSession();
        TemplateUtil templateUtil = new TemplateUtil(bhApi);
        System.out.println("Creating Template for " + entityName);
        templateUtil.writeExampleEntityCsv(entityName);
    }

    private BullhornAPI createSession() throws Exception {
        final PropertyFileUtil propertyFileUtil = new PropertyFileUtil("dataloader.properties");
        final BullhornAPI bhApi = new BullhornAPI(propertyFileUtil);
        bhApi.createSession();
        return bhApi;
    }

    private ConcurrencyService createConcurrencyService(String entity, String filePath, BullhornAPI bhApi) throws Exception {
        final BullhornApiAssociator bullhornApiAssociator = new BullhornApiAssociator(bhApi);
        final LoadingCache<EntityQuery, Result> associationCache = CacheBuilder.newBuilder()
                .maximumSize(bhApi.getPropertyFileUtil().getCacheSize())
                .build(new EntityCache(bhApi));

        bhApi.frontLoad();
        final CsvFileReader csvFileReader = new CsvFileReader(filePath, bhApi.getRootMetaDataTypes(entity));
        final CsvFileWriter csvFileWriter = new CsvFileWriter(filePath, csvFileReader.getHeaders());

        final ExecutorService executorService = Executors.newFixedThreadPool(bhApi.getPropertyFileUtil().getNumThreads());
        final ConcurrencyService concurrencyService = new ConcurrencyService(
                WordUtils.capitalize(entity),
                csvFileReader,
                csvFileWriter,
                bhApi,
                bullhornApiAssociator,
                executorService,
                associationCache,
                bhApi.getPropertyFileUtil()
        );

        return concurrencyService;
    }
}

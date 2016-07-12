package com.bullhorn.dataloader;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.io.FilenameUtils;
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
import com.bullhorn.dataloader.util.PropertyFileUtil;
import com.bullhorn.dataloader.util.StringConsts;
import com.bullhorn.dataloader.util.TemplateUtil;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.LoadingCache;

public class Main {

    private static Logger log = LogManager.getLogger(Main.class);

    private static void printUsage() {
        System.out.println("");
        System.out.println("Usage:");
        System.out.println("    Insert/Update: dataloader <EntityName> path/to/data.csv");
        System.out.println("           Delete: dataloader delete <EntityName> path/to/data.csv");
        System.out.println("  Create Template: dataloader template <EntityName>");
        System.out.println("");
        System.out.println("where <EntityName> is one of the supported entities listed at:");
        System.out.println("                   https://github.com/bullhorn/dataloader/wiki/Supported-Entities");
        System.out.println("");
    }

    private static void printEntityError(String entityName, String warningText) {
        System.out.println("");
        System.out.println("ERROR: " + warningText + " entity: \"" + entityName + "\"");
        System.out.println("       The entity is " + warningText + " in REST and cannot be changed by DataLoader.\"");
        System.out.println("       See the full list of DataLoader supported entities at:");
        System.out.println("       https://github.com/bullhorn/dataloader/wiki/Supported-Entities.");
        System.out.println("");
    }

    private static void printUnknownEntityError(String entityName) {
        System.out.println("");
        System.out.println("ERROR: Unknown entity: \"" + entityName + "\"");
        System.out.println("       This entity is not known to DataLoader and cannot be used.");
        System.out.println("       Check your spelling and see the full list of DataLoader supported entities at:");
        System.out.println("       https://github.com/bullhorn/dataloader/wiki/Supported-Entities.");
        System.out.println("");
    }

    /**
     * Main Entry Point
     * <p>
     * This method should function only as a gatekeeper for command line args.
     * No business logic should go here, only calls to other methods to do the work.
     *
     * @param args The user's command line parameters
     */
    public static void main(String[] args) {
        try {
            if (args.length < 2) {
                System.out.println("ERROR: Not enough arguments provided.");
                printUsage();
            } else if (args.length == 2) {
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
                    printUsage();
                }
            } else {
                System.out.println("ERROR: Too many arguments provided.");
                printUsage();
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            log.error(e);
        }
    }

    /**
     * Second level validation of the given file before passing it off for processing.
     */
    static private boolean isValidCsvFile(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            System.out.println("ERROR: Cannot access: " + filePath);
            System.out.println("       Ensure path is correct.");
            printUsage();
            return false;
        } else if (file.isDirectory()) {
            System.out.println("ERROR: Expected a file, but a directory was provided.");
            printUsage();
            return false;
        } else if (!FilenameUtils.getExtension(filePath).equalsIgnoreCase("csv")) {
            System.out.println("ERROR: Expected a '*.csv' file, but was provided: " + filePath);
            System.out.println("       Provide a csv file to load/update");
            printUsage();
            return false;
        }

        return true;
    }

    /**
     * Validates that an entity can be loaded, based on the name.
     */
    static private boolean isLoadableEntity(String entityName) {
        if (StringConsts.isLoadable(entityName)) {
            return true;
        } else if (StringConsts.isReadOnly(entityName)) {
            printEntityError(entityName, "read only");
            return false;
        }

        printUnknownEntityError(entityName);
        return false;
    }

    /**
     * Validates that an entity can be deleted, based on the name.
     */
    static private boolean isDeletableEntity(String entityName) {
        if (StringConsts.isDeletable(entityName)) {
            return true;
        } else if (StringConsts.isNotDeletable(entityName) || StringConsts.isReadOnly(entityName)) {
            printEntityError(entityName, "not deletable");
            return false;
        }

        printUnknownEntityError(entityName);
        return false;
    }

    static private void load(String entityName, String filePath) throws Exception {
        if (isLoadableEntity(entityName) && isValidCsvFile(filePath)) {
            final BullhornAPI bhApi = createSession();
            System.out.println("Loading " + entityName + " records from: " + filePath);
            loadCsv(entityName, filePath, bhApi);
        }
    }

    static private void delete(String entityName, String filePath) throws Exception {
        if (isDeletableEntity(entityName) && isValidCsvFile(filePath)) {
            final BullhornAPI bhApi = createSession();
            System.out.println("Deleting " + entityName + " records from: " + filePath);
            deleteCsv(entityName, filePath, bhApi);
        }
    }

    static private void template(String entityName) throws Exception {
        final BullhornAPI bhApi = createSession();
        TemplateUtil templateUtil = new TemplateUtil(bhApi);
        System.out.println("Creating Template for " + entityName);
        templateUtil.writeExampleEntityCsv(entityName);
    }

    static private BullhornAPI createSession() throws Exception {
        final PropertyFileUtil propertyFileUtil = new PropertyFileUtil("dataloader.properties");
        final BullhornAPI bhApi = new BullhornAPI(propertyFileUtil);
        bhApi.createSession();
        return bhApi;
    }

    static private void loadCsv(String entity, String filePath, BullhornAPI bhApi) throws Exception {
        final BullhornApiAssociator bullhornApiAssociator = new BullhornApiAssociator(bhApi);
        final LoadingCache<EntityQuery, Result> associationCache = CacheBuilder.newBuilder()
                .maximumSize(bhApi.getPropertyFileUtil().getCacheSize())
                .build(new EntityCache(bhApi));

        bhApi.frontLoad();
        final CsvFileReader csvFileReader = new CsvFileReader(filePath, bhApi.getRootMetaDataTypes(entity));
        final CsvFileWriter csvFileWriter = new CsvFileWriter(filePath, csvFileReader.getHeaders());

        final ExecutorService executorService = Executors.newFixedThreadPool(bhApi.getPropertyFileUtil().getNumThreads());
        final ConcurrencyService executor = new ConcurrencyService(
                WordUtils.capitalize(entity),
                csvFileReader,
                csvFileWriter,
                bhApi,
                bullhornApiAssociator,
                executorService,
                associationCache,
                bhApi.getPropertyFileUtil()
        );
        executor.runLoadProcess();
    }

    static private void deleteCsv(String entity, String filePath, BullhornAPI bhApi) throws Exception {
        final BullhornApiAssociator bullhornApiAssociator = new BullhornApiAssociator(bhApi);
        final LoadingCache<EntityQuery, Result> associationCache = CacheBuilder.newBuilder()
                .maximumSize(bhApi.getPropertyFileUtil().getCacheSize())
                .build(new EntityCache(bhApi));

        final CsvFileReader csvFileReader = new CsvFileReader(filePath, bhApi.getRootMetaDataTypes(entity));
        final CsvFileWriter csvFileWriter = new CsvFileWriter(filePath, csvFileReader.getHeaders());

        final ExecutorService executorService = Executors.newFixedThreadPool(bhApi.getPropertyFileUtil().getNumThreads());
        final ConcurrencyService executor = new ConcurrencyService(
                WordUtils.capitalize(entity),
                csvFileReader,
                csvFileWriter,
                bhApi,
                bullhornApiAssociator,
                executorService,
                associationCache,
                bhApi.getPropertyFileUtil()
        );
        executor.runDeleteProcess();
    }
}

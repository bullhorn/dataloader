package com.bullhorn.dataloader.service;

import com.bullhorn.dataloader.enums.Command;
import com.bullhorn.dataloader.enums.EntityInfo;
import com.bullhorn.dataloader.service.csv.CsvFileReader;
import com.bullhorn.dataloader.service.csv.CsvFileWriter;
import com.bullhorn.dataloader.service.executor.BullhornRestApi;
import com.bullhorn.dataloader.service.executor.ConcurrencyService;
import com.bullhorn.dataloader.util.ActionTotals;
import com.bullhorn.dataloader.util.CompleteUtil;
import com.bullhorn.dataloader.util.ConnectionUtil;
import com.bullhorn.dataloader.util.PrintUtil;
import com.bullhorn.dataloader.util.PropertyFileUtil;
import com.bullhorn.dataloader.util.Timer;
import com.bullhorn.dataloader.util.validation.ValidationUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.SortedMap;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Base class for all command line actions.
 * <p>
 * Contains common functionality.
 */
public abstract class AbstractService {

    final protected ConnectionUtil connectionUtil;
    final protected PrintUtil printUtil;
    final protected PropertyFileUtil propertyFileUtil;
    final protected ValidationUtil validationUtil;
    final protected CompleteUtil completeUtil;
    final protected InputStream inputStream;
    final protected Timer timer;

    public AbstractService(PrintUtil printUtil,
                           PropertyFileUtil propertyFileUtil,
                           ValidationUtil validationUtil,
                           CompleteUtil completeUtil,
                           ConnectionUtil connectionUtil,
                           InputStream inputStream,
                           Timer timer) throws IOException {
        this.printUtil = printUtil;
        this.propertyFileUtil = propertyFileUtil;
        this.validationUtil = validationUtil;
        this.completeUtil = completeUtil;
        this.connectionUtil = connectionUtil;
        this.inputStream = inputStream;
        this.timer = timer;
    }

    // TODO: Move out to utility class
    /**
     * Create a thread pool executor service for processing entities
     *
     * @param propertyFileUtil - properties for the thread pool
     * @return java.util.concurrent.ExecutorService
     */
    protected ExecutorService getExecutorService(PropertyFileUtil propertyFileUtil) {
        final BlockingQueue taskPoolSize = new ArrayBlockingQueue(getTaskPoolSize());
        final int timeToLive = 10;

        return new ThreadPoolExecutor(propertyFileUtil.getNumThreads(), propertyFileUtil.getNumThreads(), timeToLive, TimeUnit.SECONDS, taskPoolSize, new ThreadPoolExecutor.CallerRunsPolicy());
    }

    // TODO: Move out to utility class
    /**
     * Gets task pool size limit on basis of system memory
     *
     * @return task pool size limit
     */
    protected int getTaskPoolSize() {
        final long sixteenGigabyte = 16456252;
        final long memorySize = ((com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean()).getTotalPhysicalMemorySize() / 1024;

        if (memorySize < sixteenGigabyte) {
            return 1000;
        }
        return 10000;
    }

    /**
     * Create thread pool for processing entityClass attachment changes
     *
     * @param command    - command line action to perform
     * @param entityInfo - enum representing the entity
     * @param filePath   - CSV file with attachment data
     * @return ConcurrencyService thread pool service
     * @throws Exception if error when opening session, loading entity data, or reading CSV
     */
    protected ConcurrencyService createConcurrencyService(Command command, EntityInfo entityInfo, String filePath) throws Exception {
        final BullhornRestApi bullhornRestApi = connectionUtil.connect();
        final ExecutorService executorService = getExecutorService(propertyFileUtil);
        final CsvFileReader csvFileReader = new CsvFileReader(filePath);
        final CsvFileWriter csvFileWriter = new CsvFileWriter(command, filePath, csvFileReader.getHeaders());
        ActionTotals actionTotals = new ActionTotals();

        ConcurrencyService concurrencyService = new ConcurrencyService(
            command,
            entityInfo,
            csvFileReader,
            csvFileWriter,
            executorService,
            propertyFileUtil,
            bullhornRestApi,
            printUtil,
            actionTotals
        );

        return concurrencyService;
    }

    /**
     * When loading from directory, give the user a chance to hit ENTER or CTRL+C once they see all the files about
     * to be processed. Handles the case where there are multiple entities with multiple files or one entity with
     * multiple files.
     *
     * @param filePath            The user provided directory where these files came from
     * @param entityToFileListMap The list of files that will be loaded
     * @return true if the user has responded with yes, false if no
     */
    protected Boolean promptUserForMultipleFiles(String filePath, SortedMap<EntityInfo, List<String>> entityToFileListMap) {
        if (entityToFileListMap.size() > 1 ||
            (!entityToFileListMap.isEmpty() &&
                entityToFileListMap.get(entityToFileListMap.firstKey()).size() > 1)) {
            printUtil.printAndLog("Ready to process the following CSV files from the " + filePath + " directory in the following order:");

            Integer count = 1;
            for (Map.Entry<EntityInfo, List<String>> entityFileEntry : entityToFileListMap.entrySet()) {
                String entityName = entityFileEntry.getKey().getEntityName();
                for (String fileName : entityFileEntry.getValue()) {
                    File file = new File(fileName);
                    printUtil.printAndLog("   " + count++ + ". " + entityName + " records from " + file.getName());
                }
            }

            printUtil.print("Do you want to continue? [Y/N]");
            Scanner scanner = new Scanner(inputStream);
            Boolean yesOrNoResponse = false;
            while (!yesOrNoResponse) {
                String input = scanner.nextLine();
                if (input.startsWith("y") || input.startsWith("Y")) {
                    yesOrNoResponse = true;
                } else if (input.startsWith("n") || input.startsWith("N")) {
                    return false;
                }
            }
        }

        return true;
    }
}

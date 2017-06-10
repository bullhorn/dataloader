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
import com.bullhorn.dataloader.util.ThreadPoolUtil;
import com.bullhorn.dataloader.util.Timer;
import com.bullhorn.dataloader.util.validation.ValidationUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.SortedMap;
import java.util.concurrent.ExecutorService;

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
        final ExecutorService executorService = ThreadPoolUtil.getExecutorService(propertyFileUtil.getNumThreads());
        final CsvFileReader csvFileReader = new CsvFileReader(filePath);
        final CsvFileWriter csvFileWriter = new CsvFileWriter(command, filePath, csvFileReader.getHeaders());
        ActionTotals actionTotals = new ActionTotals();

        return new ConcurrencyService(command, entityInfo, csvFileReader, csvFileWriter, executorService, propertyFileUtil, bullhornRestApi, printUtil, actionTotals);
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

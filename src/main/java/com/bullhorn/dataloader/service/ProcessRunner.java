package com.bullhorn.dataloader.service;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import com.bullhorn.dataloader.data.ActionTotals;
import com.bullhorn.dataloader.data.CsvFileReader;
import com.bullhorn.dataloader.data.CsvFileWriter;
import com.bullhorn.dataloader.data.Row;
import com.bullhorn.dataloader.enums.Command;
import com.bullhorn.dataloader.enums.EntityInfo;
import com.bullhorn.dataloader.rest.Cache;
import com.bullhorn.dataloader.rest.CompleteUtil;
import com.bullhorn.dataloader.rest.Preloader;
import com.bullhorn.dataloader.rest.RestApi;
import com.bullhorn.dataloader.rest.RestSession;
import com.bullhorn.dataloader.task.AbstractTask;
import com.bullhorn.dataloader.task.TaskFactory;
import com.bullhorn.dataloader.util.ArrayUtil;
import com.bullhorn.dataloader.util.PrintUtil;
import com.bullhorn.dataloader.util.PropertyFileUtil;
import com.bullhorn.dataloader.util.StringConsts;
import com.bullhorn.dataloader.util.ThreadPoolUtil;

/**
 * Dependency injected utility for running a process, such as load or delete.
 *
 * Contains the logic for running all complex processes in DataLoader. Services can call these methods to create and
 * execute tasks as part of a larger process without having to know all of the details involved.
 */
@SuppressWarnings("StatementWithEmptyBody")
public class ProcessRunner {

    private final RestSession restSession;
    private final Preloader preloader;
    private final PrintUtil printUtil;
    private final PropertyFileUtil propertyFileUtil;
    private final ThreadPoolUtil threadPoolUtil;
    private final Cache cache;
    private final CompleteUtil completeUtil;

    public ProcessRunner(RestSession restSession,
                         Preloader preloader,
                         PrintUtil printUtil,
                         PropertyFileUtil propertyFileUtil,
                         ThreadPoolUtil threadPoolUtil,
                         Cache cache,
                         CompleteUtil completeUtil) {
        this.restSession = restSession;
        this.preloader = preloader;
        this.printUtil = printUtil;
        this.propertyFileUtil = propertyFileUtil;
        this.threadPoolUtil = threadPoolUtil;
        this.cache = cache;
        this.completeUtil = completeUtil;
    }

    ActionTotals run(Command command, EntityInfo entityInfo, String filePath) throws IOException, InterruptedException {
        RestApi restApi = restSession.getRestApi();
        ExecutorService executorService = threadPoolUtil.getExecutorService();
        CsvFileReader csvFileReader = new CsvFileReader(filePath, propertyFileUtil, printUtil);
        CsvFileWriter csvFileWriter = new CsvFileWriter(command, filePath, csvFileReader.getHeaders());
        ActionTotals actionTotals = new ActionTotals();
        TaskFactory taskFactory = new TaskFactory(entityInfo, csvFileWriter, propertyFileUtil, restApi, printUtil, actionTotals, cache, completeUtil);

        // Warning when id column is present but will be ignored
        if (command.equals(Command.LOAD)) {
            List<String> headers = new ArrayList<>(Arrays.asList(csvFileReader.getHeaders()));
            if (ArrayUtil.containsIgnoreCase(headers, StringConsts.ID)
                && !ArrayUtil.containsIgnoreCase(propertyFileUtil.getEntityExistFields(entityInfo), StringConsts.ID)) {
                printUtil.printAndLog("WARNING: The '" + StringConsts.ID + "' column is not being used for "
                    + "duplicate checking. The " + StringConsts.ID + " value will be ignored.");
            }
        }

        // Loop over each row in the file
        while (csvFileReader.readRecord()) {
            // Run preloader before loading only
            Row row = command == Command.LOAD ? preloader.convertRow(csvFileReader.getRow()) : csvFileReader.getRow();

            // Create an individual task runner (thread) for the row
            AbstractTask task = taskFactory.getTask(command, row);

            // Put the task in the thread pool so that it can be processed when a thread is available
            executorService.execute(task);
        }
        // Use Shutdown and AwaitTermination Wait to allow all current threads to complete and then print totals
        executorService.shutdown();
        while (!executorService.awaitTermination(1, TimeUnit.MINUTES)) {
        }
        printUtil.printActionTotals(command, actionTotals);
        return actionTotals;
    }
}

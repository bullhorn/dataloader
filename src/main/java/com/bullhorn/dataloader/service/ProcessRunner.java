package com.bullhorn.dataloader.service;


import com.bullhorn.dataloader.data.ActionTotals;
import com.bullhorn.dataloader.data.CsvFileReader;
import com.bullhorn.dataloader.data.CsvFileWriter;
import com.bullhorn.dataloader.data.Row;
import com.bullhorn.dataloader.enums.Command;
import com.bullhorn.dataloader.enums.EntityInfo;
import com.bullhorn.dataloader.rest.Preloader;
import com.bullhorn.dataloader.rest.RestApi;
import com.bullhorn.dataloader.rest.RestSession;
import com.bullhorn.dataloader.task.AbstractTask;
import com.bullhorn.dataloader.task.ConvertAttachmentTask;
import com.bullhorn.dataloader.task.DeleteAttachmentTask;
import com.bullhorn.dataloader.task.DeleteCustomObjectTask;
import com.bullhorn.dataloader.task.DeleteTask;
import com.bullhorn.dataloader.task.LoadAttachmentTask;
import com.bullhorn.dataloader.task.LoadCustomObjectTask;
import com.bullhorn.dataloader.task.LoadTask;
import com.bullhorn.dataloader.util.PrintUtil;
import com.bullhorn.dataloader.util.PropertyFileUtil;
import com.bullhorn.dataloader.util.ThreadPoolUtil;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

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

    public ProcessRunner(RestSession restSession, Preloader preloader, PrintUtil printUtil, PropertyFileUtil propertyFileUtil, ThreadPoolUtil threadPoolUtil) {
        this.restSession = restSession;
        this.preloader = preloader;
        this.printUtil = printUtil;
        this.propertyFileUtil = propertyFileUtil;
        this.threadPoolUtil = threadPoolUtil;
    }

    ActionTotals runLoadProcess(EntityInfo entityInfo, String filePath) throws IOException, InterruptedException {
        RestApi restApi = restSession.getRestApi();
        ExecutorService executorService = threadPoolUtil.getExecutorService();
        CsvFileReader csvFileReader = new CsvFileReader(filePath);
        CsvFileWriter csvFileWriter = new CsvFileWriter(Command.LOAD, filePath, csvFileReader.getHeaders());
        ActionTotals actionTotals = new ActionTotals();

        // Loop over each row in the file
        while (csvFileReader.readRecord()) {
            // Create an individual task runner (thread) for the row
            Row row = preloader.convertRow(csvFileReader.getRow());
            AbstractTask task;
            if (entityInfo.isCustomObject()) {
                task = new LoadCustomObjectTask(entityInfo, row, preloader, csvFileWriter, propertyFileUtil, restApi, printUtil, actionTotals);
            } else {
                task = new LoadTask(entityInfo, row, preloader, csvFileWriter, propertyFileUtil, restApi, printUtil, actionTotals);
            }
            // Put the task in the thread pool so that it can be processed when a thread is available
            executorService.execute(task);
        }
        // Use Shutdown and AwaitTermination Wait to allow all current threads to complete and then print totals
        executorService.shutdown();
        while (!executorService.awaitTermination(1, TimeUnit.MINUTES)) {
        }
        printUtil.printActionTotals(Command.LOAD, actionTotals);
        return actionTotals;
    }

    ActionTotals runDeleteProcess(EntityInfo entityInfo, String filePath) throws IOException, InterruptedException {
        RestApi restApi = restSession.getRestApi();
        ExecutorService executorService = threadPoolUtil.getExecutorService();
        CsvFileReader csvFileReader = new CsvFileReader(filePath);
        CsvFileWriter csvFileWriter = new CsvFileWriter(Command.DELETE, filePath, csvFileReader.getHeaders());
        ActionTotals actionTotals = new ActionTotals();

        // Loop over each row in the file
        while (csvFileReader.readRecord()) {
            // Create an individual task runner (thread) for the row
            Row row = csvFileReader.getRow();
            AbstractTask task;
            if (entityInfo.isCustomObject()) {
                task = new DeleteCustomObjectTask(entityInfo, row, preloader, csvFileWriter, propertyFileUtil, restApi, printUtil, actionTotals);
            } else {
                task = new DeleteTask(entityInfo, row, csvFileWriter, propertyFileUtil, restApi, printUtil, actionTotals);
            }
            // Put the task in the thread pool so that it can be processed when a thread is available
            executorService.execute(task);
        }
        // Use Shutdown and AwaitTermination Wait to allow all current threads to complete and then print totals
        executorService.shutdown();
        while (!executorService.awaitTermination(1, TimeUnit.MINUTES)) {
        }
        printUtil.printActionTotals(Command.DELETE, actionTotals);
        return actionTotals;
    }

    ActionTotals runLoadAttachmentsProcess(EntityInfo entityInfo, String filePath) throws IOException, InterruptedException {
        RestApi restApi = restSession.getRestApi();
        ExecutorService executorService = threadPoolUtil.getExecutorService();
        CsvFileReader csvFileReader = new CsvFileReader(filePath);
        CsvFileWriter csvFileWriter = new CsvFileWriter(Command.LOAD_ATTACHMENTS, filePath, csvFileReader.getHeaders());
        ActionTotals actionTotals = new ActionTotals();

        // Loop over each row in the file
        while (csvFileReader.readRecord()) {
            // Create an individual task runner (thread) for the row
            Row row = csvFileReader.getRow();
            LoadAttachmentTask task = new LoadAttachmentTask(entityInfo, row, csvFileWriter, propertyFileUtil, restApi, printUtil, actionTotals);

            // Put the task in the thread pool so that it can be processed when a thread is available
            executorService.execute(task);
        }
        // Use Shutdown and AwaitTermination Wait to allow all current threads to complete and then print totals
        executorService.shutdown();
        while (!executorService.awaitTermination(1, TimeUnit.MINUTES)) {
        }
        printUtil.printActionTotals(Command.LOAD_ATTACHMENTS, actionTotals);
        return actionTotals;
    }

    ActionTotals runConvertAttachmentsProcess(EntityInfo entityInfo, String filePath) throws IOException, InterruptedException {
        RestApi restApi = restSession.getRestApi();
        ExecutorService executorService = threadPoolUtil.getExecutorService();
        CsvFileReader csvFileReader = new CsvFileReader(filePath);
        CsvFileWriter csvFileWriter = new CsvFileWriter(Command.CONVERT_ATTACHMENTS, filePath, csvFileReader.getHeaders());
        ActionTotals actionTotals = new ActionTotals();

        // Loop over each row in the file
        while (csvFileReader.readRecord()) {
            // Create an individual task runner (thread) for the row
            Row row = csvFileReader.getRow();
            ConvertAttachmentTask task = new ConvertAttachmentTask(entityInfo, row, csvFileWriter, propertyFileUtil, restApi, printUtil, actionTotals);

            // Put the task in the thread pool so that it can be processed when a thread is available
            executorService.execute(task);
        }
        // Use Shutdown and AwaitTermination Wait to allow all current threads to complete and then print totals
        executorService.shutdown();
        while (!executorService.awaitTermination(1, TimeUnit.MINUTES)) {
        }
        printUtil.printActionTotals(Command.CONVERT_ATTACHMENTS, actionTotals);
        return actionTotals;
    }

    ActionTotals runDeleteAttachmentsProcess(EntityInfo entityInfo, String filePath) throws IOException, InterruptedException {
        RestApi restApi = restSession.getRestApi();
        ExecutorService executorService = threadPoolUtil.getExecutorService();
        CsvFileReader csvFileReader = new CsvFileReader(filePath);
        CsvFileWriter csvFileWriter = new CsvFileWriter(Command.CONVERT_ATTACHMENTS, filePath, csvFileReader.getHeaders());
        ActionTotals actionTotals = new ActionTotals();

        // Loop over each row in the file
        while (csvFileReader.readRecord()) {
            // Create an individual task runner (thread) for the row
            Row row = csvFileReader.getRow();
            DeleteAttachmentTask task = new DeleteAttachmentTask(entityInfo, row, csvFileWriter, propertyFileUtil, restApi, printUtil, actionTotals);

            // Put the task in the thread pool so that it can be processed when a thread is available
            executorService.execute(task);
        }
        // Use Shutdown and AwaitTermination Wait to allow all current threads to complete and then print totals
        executorService.shutdown();
        while (!executorService.awaitTermination(1, TimeUnit.MINUTES)) {
        }
        printUtil.printActionTotals(Command.DELETE_ATTACHMENTS, actionTotals);
        return actionTotals;
    }
}

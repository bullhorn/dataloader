package com.bullhorn.dataloader.util;


import com.bullhorn.dataloader.csv.CsvFileReader;
import com.bullhorn.dataloader.csv.CsvFileWriter;
import com.bullhorn.dataloader.enums.Command;
import com.bullhorn.dataloader.enums.EntityInfo;
import com.bullhorn.dataloader.rest.BullhornRestApi;
import com.bullhorn.dataloader.task.AbstractTask;
import com.bullhorn.dataloader.task.ConvertAttachmentTask;
import com.bullhorn.dataloader.task.DeleteAttachmentTask;
import com.bullhorn.dataloader.task.DeleteCustomObjectTask;
import com.bullhorn.dataloader.task.DeleteTask;
import com.bullhorn.dataloader.task.LoadAttachmentTask;
import com.bullhorn.dataloader.task.LoadCustomObjectTask;
import com.bullhorn.dataloader.task.LoadTask;
import com.bullhornsdk.data.model.file.FileMeta;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Dependency injected utility for running a process, such as load or delete.
 * <p>
 * Contains the logic for running all complex processes in DataLoader. Services can call these methods to create and
 * execute tasks as part of a larger process without having to know all of the details involved.
 */
public class ProcessRunnerUtil {

    final protected ConnectionUtil connectionUtil;
    final protected PreloadUtil preloadUtil;
    final protected PrintUtil printUtil;
    final protected PropertyFileUtil propertyFileUtil;
    final protected ThreadPoolUtil threadPoolUtil;

    public ProcessRunnerUtil(ConnectionUtil connectionUtil, PreloadUtil preloadUtil, PrintUtil printUtil, PropertyFileUtil propertyFileUtil, ThreadPoolUtil threadPoolUtil) {
        this.connectionUtil = connectionUtil;
        this.preloadUtil = preloadUtil;
        this.printUtil = printUtil;
        this.propertyFileUtil = propertyFileUtil;
        this.threadPoolUtil = threadPoolUtil;
    }

    public ActionTotals runLoadProcess(EntityInfo entityInfo, String filePath) throws IOException, InterruptedException {
        BullhornRestApi bullhornRestApi = connectionUtil.getSession();
        ExecutorService executorService = threadPoolUtil.getExecutorService();
        CsvFileReader csvFileReader = new CsvFileReader(filePath);
        CsvFileWriter csvFileWriter = new CsvFileWriter(Command.LOAD, filePath, csvFileReader.getHeaders());
        ActionTotals actionTotals = new ActionTotals();

        // Preload any necessary data prior to loading this entity type
        preloadUtil.preload(entityInfo);

        // Loop over each row in the file
        Integer rowNumber = 1;
        while (csvFileReader.readRecord()) {
            // Get the data for the row
            Map<String, String> dataMap = csvFileReader.getRecordDataMap();

            // Create an individual task for the row
            AbstractTask task;
            if (entityInfo.isCustomObject()) {
                // TODO: Remove the need for this once CustomObjects PUT calls work
                task = new LoadCustomObjectTask(rowNumber++, entityInfo, dataMap, preloadUtil, csvFileWriter, propertyFileUtil, bullhornRestApi, printUtil, actionTotals);
            } else {
                // TODO: Use the methodMap from the entityInfo object and stop passing it
                // TODO: Combine the rowNumber and dataMap into the row object
                // TODO: Move the countryNameToIdMap into a PreloadUtil and DI it
                task = new LoadTask(rowNumber++, entityInfo, dataMap, preloadUtil, csvFileWriter, propertyFileUtil, bullhornRestApi, printUtil, actionTotals);
            }
            // Put the task in the thread pool so that it can be processed when a thread is available
            executorService.execute(task);
        }
        // Use Shutdown and AwaitTermination Wait to allow all current threads to complete and then print totals
        executorService.shutdown();
        while (!executorService.awaitTermination(1, TimeUnit.MINUTES)) ;
        printUtil.printActionTotals(Command.LOAD, actionTotals);
        return actionTotals;
    }

    public ActionTotals runDeleteProcess(EntityInfo entityInfo, String filePath) throws IOException, InterruptedException {
        BullhornRestApi bullhornRestApi = connectionUtil.getSession();
        ExecutorService executorService = threadPoolUtil.getExecutorService();
        CsvFileReader csvFileReader = new CsvFileReader(filePath);
        CsvFileWriter csvFileWriter = new CsvFileWriter(Command.DELETE, filePath, csvFileReader.getHeaders());
        ActionTotals actionTotals = new ActionTotals();

        // Loop over each row in the file
        Integer rowNumber = 1;
        while (csvFileReader.readRecord()) {
            // Get the data for the row
            Map<String, String> dataMap = csvFileReader.getRecordDataMap();

            // Create an individual task for the row
            AbstractTask task;
            if (entityInfo.isCustomObject()) {
                // TODO: Remove the need for this once CustomObjects DELETE calls work
                task = new DeleteCustomObjectTask(rowNumber++, entityInfo, dataMap, preloadUtil, csvFileWriter, propertyFileUtil, bullhornRestApi, printUtil, actionTotals);
            } else {
                task = new DeleteTask(rowNumber++, entityInfo, dataMap, csvFileWriter, propertyFileUtil, bullhornRestApi, printUtil, actionTotals);
            }
            // Put the task in the thread pool so that it can be processed when a thread is available
            executorService.execute(task);
        }
        // Use Shutdown and AwaitTermination Wait to allow all current threads to complete and then print totals
        executorService.shutdown();
        while (!executorService.awaitTermination(1, TimeUnit.MINUTES)) ;
        printUtil.printActionTotals(Command.DELETE, actionTotals);
        return actionTotals;
    }

    public ActionTotals runLoadAttachmentsProcess(EntityInfo entityInfo, String filePath) throws IOException, InterruptedException {
        BullhornRestApi bullhornRestApi = connectionUtil.getSession();
        ExecutorService executorService = threadPoolUtil.getExecutorService();
        CsvFileReader csvFileReader = new CsvFileReader(filePath);
        CsvFileWriter csvFileWriter = new CsvFileWriter(Command.LOAD_ATTACHMENTS, filePath, csvFileReader.getHeaders());
        ActionTotals actionTotals = new ActionTotals();

        // Use the FileMeta setters, not the setters for the entity that is being loaded
        Map<String, Method> methodMap = MethodUtil.getSetterMethodMap(FileMeta.class);

        // Loop over each row in the file
        Integer rowNumber = 1;
        while (csvFileReader.readRecord()) {
            // Get the data for the row
            Map<String, String> dataMap = csvFileReader.getRecordDataMap();

            // Create an individual task for the row
            LoadAttachmentTask task = new LoadAttachmentTask(rowNumber++, entityInfo, dataMap, methodMap, csvFileWriter, propertyFileUtil, bullhornRestApi, printUtil, actionTotals);

            // Put the task in the thread pool so that it can be processed when a thread is available
            executorService.execute(task);
        }
        // Use Shutdown and AwaitTermination Wait to allow all current threads to complete and then print totals
        executorService.shutdown();
        while (!executorService.awaitTermination(1, TimeUnit.MINUTES)) ;
        printUtil.printActionTotals(Command.LOAD_ATTACHMENTS, actionTotals);
        return actionTotals;
    }

    public ActionTotals runConvertAttachmentsProcess(EntityInfo entityInfo, String filePath) throws IOException, InterruptedException {
        BullhornRestApi bullhornRestApi = connectionUtil.getSession();
        ExecutorService executorService = threadPoolUtil.getExecutorService();
        CsvFileReader csvFileReader = new CsvFileReader(filePath);
        CsvFileWriter csvFileWriter = new CsvFileWriter(Command.CONVERT_ATTACHMENTS, filePath, csvFileReader.getHeaders());
        ActionTotals actionTotals = new ActionTotals();

        // Loop over each row in the file
        Integer rowNumber = 1;
        while (csvFileReader.readRecord()) {
            // Get the data for the row
            Map<String, String> dataMap = csvFileReader.getRecordDataMap();

            // Create an individual task for the row
            ConvertAttachmentTask task = new ConvertAttachmentTask(rowNumber++, entityInfo, dataMap, csvFileWriter, propertyFileUtil, bullhornRestApi, printUtil, actionTotals);

            // Put the task in the thread pool so that it can be processed when a thread is available
            executorService.execute(task);
        }
        // Use Shutdown and AwaitTermination Wait to allow all current threads to complete and then print totals
        executorService.shutdown();
        while (!executorService.awaitTermination(1, TimeUnit.MINUTES)) ;
        printUtil.printActionTotals(Command.CONVERT_ATTACHMENTS, actionTotals);
        return actionTotals;
    }

    public ActionTotals runDeleteAttachmentsProcess(EntityInfo entityInfo, String filePath) throws IOException, InterruptedException {
        BullhornRestApi bullhornRestApi = connectionUtil.getSession();
        ExecutorService executorService = threadPoolUtil.getExecutorService();
        CsvFileReader csvFileReader = new CsvFileReader(filePath);
        CsvFileWriter csvFileWriter = new CsvFileWriter(Command.CONVERT_ATTACHMENTS, filePath, csvFileReader.getHeaders());
        ActionTotals actionTotals = new ActionTotals();

        // Loop over each row in the file
        Integer rowNumber = 1;
        while (csvFileReader.readRecord()) {
            // Get the data for the row
            Map<String, String> dataMap = csvFileReader.getRecordDataMap();

            // Create an individual task for the row
            DeleteAttachmentTask task = new DeleteAttachmentTask(rowNumber++, entityInfo, dataMap, csvFileWriter, propertyFileUtil, bullhornRestApi, printUtil, actionTotals);

            // Put the task in the thread pool so that it can be processed when a thread is available
            executorService.execute(task);
        }
        // Use Shutdown and AwaitTermination Wait to allow all current threads to complete and then print totals
        executorService.shutdown();
        while (!executorService.awaitTermination(1, TimeUnit.MINUTES)) ;
        printUtil.printActionTotals(Command.DELETE_ATTACHMENTS, actionTotals);
        return actionTotals;
    }
}

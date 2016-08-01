package com.bullhorn.dataloader.service.executor;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import com.bullhorn.dataloader.service.Command;
import com.bullhorn.dataloader.service.csv.CsvFileWriter;
import com.bullhorn.dataloader.task.DeleteAttachmentTask;
import com.bullhorn.dataloader.task.LoadAttachmentTask;
import com.bullhorn.dataloader.util.ActionTotals;
import com.bullhorn.dataloader.util.PrintUtil;
import com.bullhorn.dataloader.util.PropertyFileUtil;
import com.bullhornsdk.data.api.BullhornData;
import com.csvreader.CsvReader;

/**
 * Responsible for executing tasks to process rows in a CSV input file.
 */
public class EntityAttachmentsConcurrencyService {

    private final ExecutorService executorService;
    private final String entityName;
    private final CsvReader csvReader;
    private final CsvFileWriter csvWriter;
    private final PropertyFileUtil propertyFileUtil;
    private final BullhornData bullhornData;
    private final Command command;
    private final PrintUtil printUtil;
    private final ActionTotals actionTotals;

    public EntityAttachmentsConcurrencyService(Command command,
                                               String entityName,
                                               CsvReader csvReader,
                                               CsvFileWriter csvWriter,
                                               ExecutorService executorService,
                                               PropertyFileUtil propertyFileUtil,
                                               BullhornData bullhornData,
                                               PrintUtil printUtil,
                                               ActionTotals actionTotals) {
        this.command = command;
        this.entityName = entityName;
        this.csvReader = csvReader;
        this.csvWriter = csvWriter;
        this.executorService = executorService;
        this.propertyFileUtil = propertyFileUtil;
        this.bullhornData = bullhornData;
        this.printUtil = printUtil;
        this.actionTotals = actionTotals;
    }

    public void runLoadAttachmentsProcess() throws IOException, InterruptedException {
        while (csvReader.readRecord()) {
            LinkedHashMap<String, String> dataMap = getCsvDataMap();
            LoadAttachmentTask loadAttachmentTask = new LoadAttachmentTask(command, entityName, dataMap, csvWriter, propertyFileUtil, bullhornData, printUtil, actionTotals);
            executorService.execute(loadAttachmentTask );
        }
        executorService.shutdown();
        while(!executorService.awaitTermination(1, TimeUnit.MINUTES));
        printUtil.printActionTotals(actionTotals);
    }

    public void runDeleteAttachmentsProcess() throws IOException, InterruptedException {
        while (csvReader.readRecord()) {
            LinkedHashMap<String, String> dataMap = getCsvDataMap();
            DeleteAttachmentTask deleteAttachmentTask = new DeleteAttachmentTask(command, entityName, dataMap, csvWriter, propertyFileUtil, bullhornData, printUtil, actionTotals);
            executorService.execute(deleteAttachmentTask);
        }
        executorService.shutdown();
        while(!executorService.awaitTermination(1, TimeUnit.MINUTES));
        printUtil.printActionTotals(actionTotals);
    }

    /**
     * creates is a mapping of name to value pairs for a single row in the CSV file
     */
    protected LinkedHashMap<String, String> getCsvDataMap() throws IOException {
        LinkedHashMap<String, String> dataMap = new LinkedHashMap<>();
        for (int i = 0; i < csvReader.getHeaderCount(); i++){
            dataMap.put(csvReader.getHeader(i), csvReader.getValues()[i]);
        }
        return dataMap;
    }
}

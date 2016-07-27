package com.bullhorn.dataloader.service.executor;

import com.bullhorn.dataloader.service.consts.Method;
import com.bullhorn.dataloader.service.csv.CsvFileWriter;
import com.bullhorn.dataloader.task.LoadAttachmentTask;
import com.bullhorn.dataloader.util.ActionTotals;
import com.bullhorn.dataloader.util.PrintUtil;
import com.bullhorn.dataloader.util.PropertyFileUtil;
import com.bullhornsdk.data.api.BullhornData;
import com.csvreader.CsvReader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Responsible for executing tasks to process rows in a CSV input file.
 */
public class EntityAttachmentConcurrencyService {

    private final ExecutorService executorService;
    private final String entityName;
    private final CsvReader csvReader;
    private final CsvFileWriter csvWriter;
    private final PropertyFileUtil propertyFileUtil;
    private final BullhornData bullhornData;
    private final Method method;
    private final PrintUtil printUtil;
    private final ActionTotals actionTotals;

    private final Logger log = LogManager.getLogger(EntityAttachmentConcurrencyService.class);

    public EntityAttachmentConcurrencyService(Method method,
                                              String entityName,
                                              CsvReader csvReader,
                                              CsvFileWriter csvWriter,
                                              ExecutorService executorService,
                                              PropertyFileUtil propertyFileUtil,
                                              BullhornData bullhornData,
                                              PrintUtil printUtil,
                                              ActionTotals actionTotals) {
        this.method = method;
        this.entityName = entityName;
        this.csvReader = csvReader;
        this.csvWriter = csvWriter;
        this.executorService = executorService;
        this.propertyFileUtil = propertyFileUtil;
        this.bullhornData = bullhornData;
        this.printUtil = printUtil;
        this.actionTotals = actionTotals;
    }

    public void runLoadAttachmentProcess() throws IOException, InterruptedException {
        while (csvReader.readRecord()) {
            LinkedHashMap<String, String> dataMap = getCsvDataMap();
            LoadAttachmentTask loadAttachmentTask = new LoadAttachmentTask(method, entityName, dataMap, csvWriter, propertyFileUtil, bullhornData, printUtil, actionTotals);
            executorService.execute(loadAttachmentTask );
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

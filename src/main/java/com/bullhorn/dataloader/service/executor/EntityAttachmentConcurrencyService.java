package com.bullhorn.dataloader.service.executor;

import com.bullhorn.dataloader.service.csv.CsvFileWriter;
import com.bullhorn.dataloader.task.LoadAttachmentTask;
import com.bullhorn.dataloader.util.PropertyFileUtil;
import com.bullhornsdk.data.api.BullhornData;
import com.csvreader.CsvReader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.concurrent.ExecutorService;

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

    private final Logger log = LogManager.getLogger(EntityAttachmentConcurrencyService.class);

    public EntityAttachmentConcurrencyService(String entityName,
                                              CsvReader csvReader,
                                              CsvFileWriter csvWriter,
                                              ExecutorService executorService,
                                              PropertyFileUtil propertyFileUtil,
                                              BullhornData bullhornData) {
        this.entityName = entityName;
        this.csvReader = csvReader;
        this.csvWriter = csvWriter;
        this.executorService = executorService;
        this.propertyFileUtil = propertyFileUtil;
        this.bullhornData = bullhornData;
    }

    public void runLoadAttchmentProcess() throws IOException {
        while (csvReader.readRecord()) {
            LinkedHashMap<String, String> dataMap = getDataMap();
            LoadAttachmentTask loadAttachmentTask = new LoadAttachmentTask(entityName, dataMap, csvWriter, propertyFileUtil, bullhornData);
            executorService.execute(loadAttachmentTask );
        }
        executorService.shutdown();
    }

    private LinkedHashMap<String, String> getDataMap() throws IOException {
        LinkedHashMap<String, String> dataMap = new LinkedHashMap<>();
        for (int i = 0; i < csvReader.getHeaderCount(); i++){
            dataMap.put(csvReader.getHeader(i), csvReader.getValues()[i]);
        }
        return dataMap;
    }

}

package com.bullhorn.dataloader.service.executor;

import com.bullhorn.dataloader.service.Command;
import com.bullhorn.dataloader.service.csv.CsvFileWriter;
import com.bullhorn.dataloader.task.DeleteAttachmentTask;
import com.bullhorn.dataloader.task.DeleteTask;
import com.bullhorn.dataloader.task.LoadAttachmentTask;
import com.bullhorn.dataloader.task.LoadTask;
import com.bullhorn.dataloader.util.ActionTotals;
import com.bullhorn.dataloader.util.PrintUtil;
import com.bullhorn.dataloader.util.PropertyFileUtil;
import com.bullhornsdk.data.api.BullhornData;
import com.bullhornsdk.data.model.entity.core.type.BullhornEntity;
import com.bullhornsdk.data.model.enums.BullhornEntityInfo;
import com.csvreader.CsvReader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Responsible for executing tasks to process rows in a CSV input file.
 */
public class ConcurrencyService<B extends BullhornEntity> {

    private final ExecutorService executorService;
    private final String entityName;
    private final CsvReader csvReader;
    private final CsvFileWriter csvWriter;
    private final PropertyFileUtil propertyFileUtil;
    private final BullhornData bullhornData;
    private final Command method;
    private final PrintUtil printUtil;
    private final ActionTotals actionTotals;
    private Integer rowNumber = 1;

    private final Logger log = LogManager.getLogger(ConcurrencyService.class);

    public ConcurrencyService(Command method,
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

    public void runLoadProcess() throws IOException, InterruptedException {
        Class<B> entity = getAndSetBullhornEntityInfo();
        Map<String, Method> methodMap = createMethodMap(entity);
        while (csvReader.readRecord()) {
            LinkedHashMap<String, String> dataMap = getCsvDataMap();
            LoadTask loadTask = new LoadTask(method, rowNumber++, entity, dataMap, methodMap, csvWriter, propertyFileUtil, bullhornData, printUtil, actionTotals);
            executorService.execute(loadTask );
        }
        executorService.shutdown();
        while(!executorService.awaitTermination(1, TimeUnit.MINUTES));
        printUtil.printActionTotals(actionTotals);
    }

    public void runDeleteProcess() throws IOException, InterruptedException {
        Class<B> entity = getAndSetBullhornEntityInfo();
        while (csvReader.readRecord()) {
            LinkedHashMap<String, String> dataMap = getCsvDataMap();
            DeleteTask deleteTask = new DeleteTask(method, rowNumber++, entity, dataMap, csvWriter, propertyFileUtil, bullhornData, printUtil, actionTotals);
            executorService.execute(deleteTask );
        }
        executorService.shutdown();
        while(!executorService.awaitTermination(1, TimeUnit.MINUTES));
        printUtil.printActionTotals(actionTotals);
    }

    public void runLoadAttachmentsProcess() throws IOException, InterruptedException {
        Class<B> entity = getAndSetBullhornEntityInfo();
        while (csvReader.readRecord()) {
            LinkedHashMap<String, String> dataMap = getCsvDataMap();
            LoadAttachmentTask loadAttachmentTask = new LoadAttachmentTask(method, rowNumber++, entity, dataMap, csvWriter, propertyFileUtil, bullhornData, printUtil, actionTotals);
            executorService.execute(loadAttachmentTask );
        }
        executorService.shutdown();
        while(!executorService.awaitTermination(1, TimeUnit.MINUTES));
        printUtil.printActionTotals(actionTotals);
    }

    public void runDeleteAttachmentsProcess() throws IOException, InterruptedException {
        Class<B> entity = getAndSetBullhornEntityInfo();
        while (csvReader.readRecord()) {
            LinkedHashMap<String, String> dataMap = getCsvDataMap();
            DeleteAttachmentTask deleteAttachmentTask = new DeleteAttachmentTask(method, rowNumber++, entity, dataMap, csvWriter, propertyFileUtil, bullhornData, printUtil, actionTotals);
            executorService.execute(deleteAttachmentTask);
        }
        executorService.shutdown();
        while (!executorService.awaitTermination(1, TimeUnit.MINUTES)) ;
        printUtil.printActionTotals(actionTotals);
    }

    protected Map<String, Method> createMethodMap(Class<B> entity) {
        Map<String, Method> methodMap = new HashMap();
        for (Method method : Arrays.asList(entity.getMethods())){
            if ("set".equalsIgnoreCase(method.getName().substring(0, 3))) {
                methodMap.put(method.getName().substring(3).toLowerCase(), method);
            }
        }
        return methodMap;
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

    protected Class<B> getAndSetBullhornEntityInfo() {
        Class<B> entity = BullhornEntityInfo.getTypeFromName(entityName).getType();
        return entity;
    }

}

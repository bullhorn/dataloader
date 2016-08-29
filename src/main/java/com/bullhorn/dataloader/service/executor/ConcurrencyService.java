package com.bullhorn.dataloader.service.executor;

import com.bullhorn.dataloader.meta.EntityInfo;
import com.bullhorn.dataloader.service.Command;
import com.bullhorn.dataloader.service.csv.CsvFileWriter;
import com.bullhorn.dataloader.task.AbstractTask;
import com.bullhorn.dataloader.task.ConvertAttachmentTask;
import com.bullhorn.dataloader.task.DeleteAttachmentTask;
import com.bullhorn.dataloader.task.DeleteTask;
import com.bullhorn.dataloader.task.LoadAttachmentTask;
import com.bullhorn.dataloader.task.LoadCustomObjectTask;
import com.bullhorn.dataloader.task.LoadTask;
import com.bullhorn.dataloader.util.ActionTotals;
import com.bullhorn.dataloader.util.PrintUtil;
import com.bullhorn.dataloader.util.PropertyFileUtil;
import com.bullhorn.dataloader.util.validation.EntityValidation;
import com.bullhornsdk.data.api.BullhornData;
import com.bullhornsdk.data.model.entity.core.standard.Country;
import com.bullhornsdk.data.model.entity.core.type.BullhornEntity;
import com.bullhornsdk.data.model.entity.embedded.Address;
import com.bullhornsdk.data.model.file.FileMeta;
import com.bullhornsdk.data.model.parameter.standard.ParamFactory;
import com.csvreader.CsvReader;
import com.google.common.collect.Sets;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Responsible for executing tasks to process rows in a CSV input file.
 */
public class ConcurrencyService<B extends BullhornEntity> {

    private final ExecutorService executorService;
    private final EntityInfo entityInfo;
    private final CsvReader csvReader;
    private final CsvFileWriter csvWriter;
    private final PropertyFileUtil propertyFileUtil;
    private final BullhornData bullhornData;
    private final Command command;
    private final PrintUtil printUtil;
    private final ActionTotals actionTotals;
    private Integer rowNumber = 1;

    public ConcurrencyService(Command command,
                              EntityInfo entityInfo,
                              CsvReader csvReader,
                              CsvFileWriter csvWriter,
                              ExecutorService executorService,
                              PropertyFileUtil propertyFileUtil,
                              BullhornData bullhornData,
                              PrintUtil printUtil,
                              ActionTotals actionTotals) {
        this.command = command;
        this.entityInfo = entityInfo;
        this.csvReader = csvReader;
        this.csvWriter = csvWriter;
        this.executorService = executorService;
        this.propertyFileUtil = propertyFileUtil;
        this.bullhornData = bullhornData;
        this.printUtil = printUtil;
        this.actionTotals = actionTotals;
    }

    public void runConvertAttachmentsProcess() throws IOException, InterruptedException {
        while (csvReader.readRecord()) {
            LinkedHashMap<String, String> dataMap = getCsvDataMap();
            ConvertAttachmentTask task = new ConvertAttachmentTask(command, rowNumber++, entityInfo, dataMap, csvWriter, propertyFileUtil, bullhornData, printUtil, actionTotals);
            executorService.execute(task);
        }
        executorService.shutdown();
        while (!executorService.awaitTermination(1, TimeUnit.MINUTES)) ;
        printUtil.printActionTotals(command, actionTotals);
    }

    public void runLoadProcess() throws IOException, InterruptedException {
        Map<String, Method> methodMap = createMethodMap(entityInfo.getBullhornEntityInfo().getType());
        Map<String, Integer> countryNameToIdMap = createCountryNameToIdMap(methodMap);
        while (csvReader.readRecord()) {
            LinkedHashMap<String, String> dataMap = getCsvDataMap();
            AbstractTask task = getLoadTask(methodMap, countryNameToIdMap, dataMap);
            executorService.execute(task);
        }
        executorService.shutdown();
        while (!executorService.awaitTermination(1, TimeUnit.MINUTES)) ;
        printUtil.printActionTotals(command, actionTotals);
    }

    private AbstractTask getLoadTask(Map<String, Method> methodMap, Map<String, Integer> countryNameToIdMap, LinkedHashMap<String, String> dataMap) {
        if (EntityValidation.isCustomObject(this.entityInfo.getEntityName())){
            return new LoadCustomObjectTask(command, rowNumber++, entityInfo, dataMap, methodMap, countryNameToIdMap, csvWriter, propertyFileUtil, bullhornData, printUtil, actionTotals);
        } else {
            return new LoadTask(command, rowNumber++, entityInfo, dataMap, methodMap, countryNameToIdMap, csvWriter, propertyFileUtil, bullhornData, printUtil, actionTotals);
        }
    }

    public void runDeleteProcess() throws IOException, InterruptedException {
        while (csvReader.readRecord()) {
            LinkedHashMap<String, String> dataMap = getCsvDataMap();
            DeleteTask task = new DeleteTask(command, rowNumber++, entityInfo, dataMap, csvWriter, propertyFileUtil, bullhornData, printUtil, actionTotals);
            executorService.execute(task);
        }
        executorService.shutdown();
        while (!executorService.awaitTermination(1, TimeUnit.MINUTES)) ;
        printUtil.printActionTotals(command, actionTotals);
    }

    public void runLoadAttachmentsProcess() throws IOException, InterruptedException {
        Map<String, Method> methodMap = createMethodMap(FileMeta.class);
        while (csvReader.readRecord()) {
            LinkedHashMap<String, String> dataMap = getCsvDataMap();
            LoadAttachmentTask task = new LoadAttachmentTask(command, rowNumber++, entityInfo, dataMap, methodMap, csvWriter, propertyFileUtil, bullhornData, printUtil, actionTotals);
            executorService.execute(task);
        }
        executorService.shutdown();
        while (!executorService.awaitTermination(1, TimeUnit.MINUTES)) ;
        printUtil.printActionTotals(command, actionTotals);
    }

    public void runDeleteAttachmentsProcess() throws IOException, InterruptedException {
        while (csvReader.readRecord()) {
            LinkedHashMap<String, String> dataMap = getCsvDataMap();
            DeleteAttachmentTask task = new DeleteAttachmentTask(command, rowNumber++, entityInfo, dataMap, csvWriter, propertyFileUtil, bullhornData, printUtil, actionTotals);
            executorService.execute(task);
        }
        executorService.shutdown();
        while (!executorService.awaitTermination(1, TimeUnit.MINUTES)) ;
        printUtil.printActionTotals(command, actionTotals);
    }

    protected Map<String, Integer> createCountryNameToIdMap(Map<String, Method> methodMap) {
        if (methodMap.containsKey("countryid")) {
            Map<String, Integer> countryNameToIdMap = new HashMap<>();
            List<Country> countryList = bullhornData.queryForAllRecords(Country.class, "id IS NOT null", Sets.newHashSet("id", "name"), ParamFactory.queryParams()).getData();
            countryList.stream().forEach(n -> countryNameToIdMap.put(n.getName().trim(), n.getId()));
            return countryNameToIdMap;
        }
        return null;
    }

    public Map<String, Method> createMethodMap(Class entity) {
        Map<String, Method> methodMap = new HashMap();
        for (Method method : Arrays.asList(entity.getMethods())) {
            if ("set".equalsIgnoreCase(method.getName().substring(0, 3))) {
                methodMap.put(method.getName().substring(3).toLowerCase(), method);
            }
        }
        addAddressMethodsIfNeeded(methodMap);
        return methodMap;
    }

    private void addAddressMethodsIfNeeded(Map<String, Method> methodMap) {
        if (methodMap.containsKey("address")) {
            for (Method method : Arrays.asList(Address.class.getMethods())) {
                if ("set".equalsIgnoreCase(method.getName().substring(0, 3))) {
                    methodMap.put(method.getName().substring(3).toLowerCase(), method);
                }
            }
        }
    }

    /**
     * creates is a mapping of name to value pairs for a single row in the CSV file
     */
    protected LinkedHashMap<String, String> getCsvDataMap() throws IOException {
        LinkedHashMap<String, String> dataMap = new LinkedHashMap<>();
        for (int i = 0; i < csvReader.getHeaderCount(); i++) {
            dataMap.put(csvReader.getHeader(i), csvReader.getValues()[i]);
        }
        return dataMap;
    }

}

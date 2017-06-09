package com.bullhorn.dataloader.service.executor;

import com.bullhorn.dataloader.enums.EntityInfo;
import com.bullhorn.dataloader.enums.Command;
import com.bullhorn.dataloader.service.csv.CsvFileReader;
import com.bullhorn.dataloader.service.csv.CsvFileWriter;
import com.bullhorn.dataloader.task.AbstractTask;
import com.bullhorn.dataloader.task.ConvertAttachmentTask;
import com.bullhorn.dataloader.task.DeleteAttachmentTask;
import com.bullhorn.dataloader.task.DeleteCustomObjectTask;
import com.bullhorn.dataloader.task.DeleteTask;
import com.bullhorn.dataloader.task.LoadAttachmentTask;
import com.bullhorn.dataloader.task.LoadCustomObjectTask;
import com.bullhorn.dataloader.task.LoadTask;
import com.bullhorn.dataloader.util.ActionTotals;
import com.bullhorn.dataloader.util.PrintUtil;
import com.bullhorn.dataloader.util.PropertyFileUtil;
import com.bullhornsdk.data.model.entity.core.standard.Country;
import com.bullhornsdk.data.model.entity.core.type.BullhornEntity;
import com.bullhornsdk.data.model.entity.embedded.Address;
import com.bullhornsdk.data.model.file.FileMeta;
import com.bullhornsdk.data.model.parameter.standard.ParamFactory;
import com.google.common.collect.Sets;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
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
    private final CsvFileReader csvFileReader;
    private final CsvFileWriter csvWriter;
    private final PropertyFileUtil propertyFileUtil;
    private final BullhornRestApi bullhornRestApi;
    private final Command command;
    private final PrintUtil printUtil;
    private final ActionTotals actionTotals;
    private Integer rowNumber = 1;

    public ConcurrencyService(Command command,
                              EntityInfo entityInfo,
                              CsvFileReader csvFileReader,
                              CsvFileWriter csvFileWriter,
                              ExecutorService executorService,
                              PropertyFileUtil propertyFileUtil,
                              BullhornRestApi bullhornRestApi,
                              PrintUtil printUtil,
                              ActionTotals actionTotals) {
        this.command = command;
        this.entityInfo = entityInfo;
        this.csvFileReader = csvFileReader;
        this.csvWriter = csvFileWriter;
        this.executorService = executorService;
        this.propertyFileUtil = propertyFileUtil;
        this.bullhornRestApi = bullhornRestApi;
        this.printUtil = printUtil;
        this.actionTotals = actionTotals;
    }

    public void runConvertAttachmentsProcess() throws IOException, InterruptedException {
        while (csvFileReader.readRecord()) {
            Map<String, String> dataMap = csvFileReader.getRecordDataMap();
            ConvertAttachmentTask task = new ConvertAttachmentTask(command, rowNumber++, entityInfo, dataMap, csvWriter, propertyFileUtil, bullhornRestApi, printUtil, actionTotals);
            executorService.execute(task);
        }
        executorService.shutdown();
        while (!executorService.awaitTermination(1, TimeUnit.MINUTES)) ;
        printUtil.printActionTotals(command, actionTotals);
    }

    public void runLoadProcess() throws IOException, InterruptedException {
        Map<String, Method> methodMap = createMethodMap(entityInfo.getBullhornEntityInfo().getType());
        Map<String, Integer> countryNameToIdMap = createCountryNameToIdMap(methodMap);
        while (csvFileReader.readRecord()) {
            Map<String, String> dataMap = csvFileReader.getRecordDataMap();
            AbstractTask task = getLoadTask(methodMap, countryNameToIdMap, dataMap);
            executorService.execute(task);
        }
        executorService.shutdown();
        while (!executorService.awaitTermination(1, TimeUnit.MINUTES)) ;
        printUtil.printActionTotals(command, actionTotals);
    }

    private AbstractTask getLoadTask(Map<String, Method> methodMap, Map<String, Integer> countryNameToIdMap, Map<String, String> dataMap) {
        if (entityInfo.isCustomObject()){
            return new LoadCustomObjectTask(command, rowNumber++, entityInfo, dataMap, methodMap, countryNameToIdMap, csvWriter, propertyFileUtil, bullhornRestApi, printUtil, actionTotals);
        } else {
            return new LoadTask(command, rowNumber++, entityInfo, dataMap, methodMap, countryNameToIdMap, csvWriter, propertyFileUtil, bullhornRestApi, printUtil, actionTotals);
        }
    }

    public void runDeleteProcess() throws IOException, InterruptedException {
        while (csvFileReader.readRecord()) {
            Map<String, String> dataMap = csvFileReader.getRecordDataMap();
            AbstractTask task = getDeleteTask(dataMap);
            executorService.execute(task);
        }
        executorService.shutdown();
        while (!executorService.awaitTermination(1, TimeUnit.MINUTES)) ;
        printUtil.printActionTotals(command, actionTotals);
    }

    private AbstractTask getDeleteTask(Map<String, String> dataMap) {
        if (entityInfo.isCustomObject()){
            return new DeleteCustomObjectTask(command, rowNumber++, entityInfo, dataMap, csvWriter, propertyFileUtil, bullhornRestApi, printUtil, actionTotals);
        } else {
            return new DeleteTask(command, rowNumber++, entityInfo, dataMap, csvWriter, propertyFileUtil, bullhornRestApi, printUtil, actionTotals);
        }
    }

    public void runLoadAttachmentsProcess() throws IOException, InterruptedException {
        Map<String, Method> methodMap = createMethodMap(FileMeta.class);
        while (csvFileReader.readRecord()) {
            Map<String, String> dataMap = csvFileReader.getRecordDataMap();
            LoadAttachmentTask task = new LoadAttachmentTask(command, rowNumber++, entityInfo, dataMap, methodMap, csvWriter, propertyFileUtil, bullhornRestApi, printUtil, actionTotals);
            executorService.execute(task);
        }
        executorService.shutdown();
        while (!executorService.awaitTermination(1, TimeUnit.MINUTES)) ;
        printUtil.printActionTotals(command, actionTotals);
    }

    public void runDeleteAttachmentsProcess() throws IOException, InterruptedException {
        while (csvFileReader.readRecord()) {
            Map<String, String> dataMap = csvFileReader.getRecordDataMap();
            DeleteAttachmentTask task = new DeleteAttachmentTask(command, rowNumber++, entityInfo, dataMap, csvWriter, propertyFileUtil, bullhornRestApi, printUtil, actionTotals);
            executorService.execute(task);
        }
        executorService.shutdown();
        while (!executorService.awaitTermination(1, TimeUnit.MINUTES)) ;
        printUtil.printActionTotals(command, actionTotals);
    }

    // TODO: Pull out into Utility
    protected Map<String, Integer> createCountryNameToIdMap(Map<String, Method> methodMap) {
        if (methodMap.containsKey("countryid")) {
            Map<String, Integer> countryNameToIdMap = new HashMap<>();
            List<Country> countryList = bullhornRestApi.queryForAllRecords(Country.class, "id IS NOT null", Sets.newHashSet("id", "name"), ParamFactory.queryParams()).getData();
            countryList.stream().forEach(n -> countryNameToIdMap.put(n.getName().trim(), n.getId()));
            return countryNameToIdMap;
        }
        return null;
    }

    // TODO: Pull out into Utility
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

    // TODO: Pull out into Utility
    private void addAddressMethodsIfNeeded(Map<String, Method> methodMap) {
        if (methodMap.containsKey("address")) {
            for (Method method : Arrays.asList(Address.class.getMethods())) {
                if ("set".equalsIgnoreCase(method.getName().substring(0, 3))) {
                    methodMap.put(method.getName().substring(3).toLowerCase(), method);
                }
            }
        }
    }

    public ActionTotals getActionTotals() {
        return actionTotals;
    }

    public BullhornRestApi getBullhornRestApi() {
        return bullhornRestApi;
    }
}

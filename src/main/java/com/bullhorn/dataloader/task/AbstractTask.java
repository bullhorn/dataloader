package com.bullhorn.dataloader.task;

import java.io.IOException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;

import com.bullhorn.dataloader.consts.TaskConsts;
import com.bullhorn.dataloader.service.Command;
import com.bullhorn.dataloader.service.csv.CsvFileWriter;
import com.bullhorn.dataloader.service.csv.Result;
import com.bullhorn.dataloader.util.ActionTotals;
import com.bullhorn.dataloader.util.PrintUtil;
import com.bullhorn.dataloader.util.PropertyFileUtil;
import com.bullhornsdk.data.api.BullhornData;
import com.bullhornsdk.data.model.entity.core.type.BullhornEntity;
import com.bullhornsdk.data.model.entity.core.type.QueryEntity;
import com.bullhornsdk.data.model.entity.core.type.SearchEntity;
import com.bullhornsdk.data.model.parameter.standard.ParamFactory;
import com.google.common.collect.Sets;

public abstract class AbstractTask<B extends BullhornEntity> implements Runnable, TaskConsts {

    protected Command command;
    protected Integer rowNumber;
    protected Class<B> entityClass;
    protected Integer bullhornParentId;
    protected Map<String, String> dataMap;
    protected CsvFileWriter csvWriter;
    protected PropertyFileUtil propertyFileUtil;
    protected BullhornData bullhornData;
    protected PrintUtil printUtil;
    protected ActionTotals actionTotals;
    private static AtomicInteger rowProcessedCount = new AtomicInteger(0);

    public AbstractTask(Command command,
                        Integer rowNumber,
                        Class<B> entityClass,
                        LinkedHashMap<String, String> dataMap,
                        CsvFileWriter csvWriter,
                        PropertyFileUtil propertyFileUtil,
                        BullhornData bullhornData,
                        PrintUtil printUtil,
                        ActionTotals actionTotals) {
        this.command = command;
        this.rowNumber = rowNumber;
        this.entityClass = entityClass;
        this.dataMap = dataMap;
        this.csvWriter = csvWriter;
        this.propertyFileUtil = propertyFileUtil;
        this.bullhornData = bullhornData;
        this.printUtil = printUtil;
        this.actionTotals = actionTotals;
    }

    public <S extends SearchEntity> void getAndSetBullhornID() throws Exception {
        String query = externalID + ":" + dataMap.get(externalID);
        List<S> searchList = bullhornData.search((Class<S>) entityClass, query, Sets.newHashSet("id"), ParamFactory.searchParams()).getData();
        if (!searchList.isEmpty()){
            bullhornParentId = searchList.get(0).getId();
        }
        else {
            throw new Exception("Parent Entity not found.");
        }
    }

    protected  void addParentEntityIDtoDataMap() {
        dataMap.put(TaskConsts.parentEntityID, bullhornParentId.toString());
    }

    protected void writeToResultCSV(Result result) {
        int attempts = 0;
        while (attempts < 3) {
            try {
                csvWriter.writeRow(dataMap.values().toArray(new String[0]), result);
                updateActionTotals(result);
                updateRowProcessedCounts();
                break;
            } catch (IOException e) {
                e.printStackTrace();
                attempts++;
            }
        }
    }

    private void updateRowProcessedCounts() {
        rowProcessedCount.incrementAndGet();
        if(rowProcessedCount.intValue() % 111 == 0) {
            printUtil.printAndLog("Processed: " + NumberFormat.getNumberInstance(Locale.US).format(rowProcessedCount) + " records.");
        }
    }

    private void updateActionTotals(Result result) {
        if(result.getAction().equals(Result.Action.INSERT)) {
            actionTotals.incrementTotalInsert();
        } else if(result.getAction().equals(Result.Action.UPDATE)){
            actionTotals.incrementTotalUpdate();
        } else if(result.getAction().equals(Result.Action.DELETE)){
            actionTotals.incrementTotalDelete();
        } else if(result.getStatus().equals(Result.Status.FAILURE)) {
            actionTotals.incrementTotalError();
        }
    }

    protected Result handleFailure(Exception e) {
        printUtil.printAndLog("ERROR: " + e.toString());
        return Result.Failure(e.toString());
    }

    protected <S extends SearchEntity> List<B> searchForEntity(String field, String value, Class<B> entityClass) {
        String query = field + ":" + value;
        return (List<B>) bullhornData.search((Class<S>) entityClass, query, Sets.newHashSet("id"), ParamFactory.searchParams()).getData();
    }

    protected <S extends SearchEntity> List<B> searchForEntity() {
        Map<String, String> entityExistFieldsMap = getEntityExistFieldsMap();
        String query = entityExistFieldsMap.keySet().stream().map(n -> n + ":" + entityExistFieldsMap.get(n)).collect(Collectors.joining(" AND "));
        return (List<B>) bullhornData.search((Class<S>) entityClass, query, Sets.newHashSet("id"), ParamFactory.searchParams()).getData();
    }

    protected <Q extends QueryEntity> List<B> queryForEntity(String field, String value, Class<B> entityClass) {
        String where = field + "='" + value + "'";
        return (List<B>) bullhornData.query((Class<Q>) entityClass, where, Sets.newHashSet("id"), ParamFactory.queryParams()).getData();
    }

    protected Map<String, String> getEntityExistFieldsMap() {
        Map<String, String> valueMap = new HashMap<>();
        List<String> entityExistFieldList = propertyFileUtil.getEntityExistFields(entityClass.getSimpleName()).get();
        for (String entityExistField : entityExistFieldList){
            valueMap.put(entityExistField, dataMap.get(entityExistField));
        }
        return valueMap;
    }

    protected final Object convertStringToClass(Method method, String value) throws ParseException {
        if (StringUtils.isEmpty(value)){
            return null;
        }
        Class convertToClass = method.getParameterTypes()[0];
        value = value.trim();
        if (String.class.equals(convertToClass)) {
            return value;
        } else if (Integer.class.equals(convertToClass)) {
            return Integer.parseInt(value);
        } else if (Double.class.equals(convertToClass)) {
            return Double.parseDouble(value);
        } else if (Boolean.class.equals(convertToClass)) {
            return Boolean.getBoolean(value);
        } else if (DateTime.class.equals(convertToClass)) {
            DateTimeFormatter formatter = propertyFileUtil.getDateParser();
            return formatter.parseDateTime(value);
        } else if (BigDecimal.class.equals(convertToClass)) {
            DecimalFormat decimalFormat = new DecimalFormat();
            decimalFormat.setParseBigDecimal(true);
            return (BigDecimal) decimalFormat.parse(value);
        }
        return null;
    }

}

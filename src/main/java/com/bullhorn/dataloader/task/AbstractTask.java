package com.bullhorn.dataloader.task;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.bullhorn.dataloader.consts.TaskConsts;
import com.bullhorn.dataloader.service.Command;
import com.bullhorn.dataloader.service.csv.CsvFileWriter;
import com.bullhorn.dataloader.service.csv.Result;
import com.bullhorn.dataloader.util.ActionTotals;
import com.bullhorn.dataloader.util.PrintUtil;
import com.bullhorn.dataloader.util.PropertyFileUtil;
import com.bullhornsdk.data.api.BullhornData;
import com.bullhornsdk.data.model.entity.core.type.BullhornEntity;
import com.bullhornsdk.data.model.entity.core.type.SearchEntity;
import com.bullhornsdk.data.model.enums.BullhornEntityInfo;
import com.bullhornsdk.data.model.parameter.standard.ParamFactory;
import com.google.common.collect.Sets;

public abstract class AbstractTask<B extends BullhornEntity> implements Runnable, TaskConsts {
    private static final Logger log = LogManager.getLogger(AbstractTask.class);

    protected Command command;
    protected String entityName;
    protected Integer bullhornParentId;
    protected Map<String, String> dataMap;
    protected CsvFileWriter csvWriter;
    protected PropertyFileUtil propertyFileUtil;
    protected BullhornData bullhornData;
    protected Class<B> entity;
    protected PrintUtil printUtil;
    protected ActionTotals actionTotals;
    private static AtomicInteger rowProcessedCount = new AtomicInteger(0);

    public AbstractTask(Command command,
                        String entityName,
                        LinkedHashMap<String, String> dataMap,
                        CsvFileWriter csvWriter,
                        PropertyFileUtil propertyFileUtil,
                        BullhornData bullhornData,
                        PrintUtil printUtil,
                        ActionTotals actionTotals) {
        this.command = command;
        this.entityName = entityName;
        this.dataMap = dataMap;
        this.csvWriter = csvWriter;
        this.propertyFileUtil = propertyFileUtil;
        this.bullhornData = bullhornData;
        this.printUtil = printUtil;
        this.actionTotals = actionTotals;
    }

    protected void getAndSetBullhornEntityInfo() {
        entity = BullhornEntityInfo.getTypeFromName(entityName).getType();
    }

    public <S extends SearchEntity> void getAndSetBullhornID() throws Exception {
        String query = externalID + ":" + dataMap.get(externalID);
        List<S> searchList = bullhornData.search((Class<S>) entity, query, Sets.newHashSet("id"), ParamFactory.searchParams()).getData();
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
        try {
            csvWriter.writeRow(dataMap.values().toArray(new String[0]), result);
            updateActionTotals(result);
            updateRowProcessedCounts();
        } catch (IOException e) {
            printUtil.printAndLog(e.toString());
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

}

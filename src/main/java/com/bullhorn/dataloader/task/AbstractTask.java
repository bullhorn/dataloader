package com.bullhorn.dataloader.task;

import com.bullhorn.dataloader.consts.TaskConsts;
import com.bullhorn.dataloader.service.csv.CsvFileWriter;
import com.bullhorn.dataloader.service.csv.Result;
import com.bullhorn.dataloader.util.PropertyFileUtil;
import com.bullhornsdk.data.api.BullhornData;
import com.bullhornsdk.data.model.entity.core.type.BullhornEntity;
import com.bullhornsdk.data.model.entity.core.type.SearchEntity;
import com.bullhornsdk.data.model.enums.BullhornEntityInfo;
import com.bullhornsdk.data.model.parameter.standard.ParamFactory;
import com.bullhornsdk.data.model.response.file.FileWrapper;
import com.google.common.collect.Sets;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractTask<B extends BullhornEntity> implements Runnable, TaskConsts {
    private static final Logger log = LogManager.getLogger(AbstractTask.class);

    protected String entityName;
    protected Integer bullhornParentId;
    protected Map<String, String> dataMap;
    protected CsvFileWriter csvWriter;
    protected PropertyFileUtil propertyFileUtil;
    protected BullhornData bullhornData;
    protected Class<B> entity;

    public AbstractTask(String entityName,
                        LinkedHashMap<String, String> dataMap,
                        CsvFileWriter csvWriter,
                        PropertyFileUtil propertyFileUtil,
                        BullhornData bullhornData) {
        this.entityName = entityName;
        this.dataMap = dataMap;
        this.csvWriter = csvWriter;
        this.propertyFileUtil = propertyFileUtil;
        this.bullhornData = bullhornData;
    }

    protected Result getFailureResult(Exception e) {
        Result result = new Result(Result.Status.FAILURE, null, null, e.getMessage());
        return result;
    }

    protected Result getSuccessResult(FileWrapper fileWrapper) {
        Result result = new Result(Result.Status.SUCCESS, null, fileWrapper.getId(), "");
        return result;
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

}

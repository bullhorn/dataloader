package com.bullhorn.dataloader.task;

import com.bullhorn.dataloader.data.ActionTotals;
import com.bullhorn.dataloader.data.CsvFileWriter;
import com.bullhorn.dataloader.data.Result;
import com.bullhorn.dataloader.data.Row;
import com.bullhorn.dataloader.enums.EntityInfo;
import com.bullhorn.dataloader.rest.RestApi;
import com.bullhorn.dataloader.util.PrintUtil;
import com.bullhorn.dataloader.util.PropertyFileUtil;
import com.bullhorn.dataloader.util.StringConsts;
import com.bullhornsdk.data.exception.RestApiException;
import com.bullhornsdk.data.model.entity.core.type.BullhornEntity;
import com.bullhornsdk.data.model.entity.core.type.DeleteEntity;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Responsible for deleting a single row from a CSV input file.
 */
public class DeleteTask<B extends BullhornEntity> extends AbstractTask<B> {
    private Integer bullhornId;

    public DeleteTask(EntityInfo entityInfo,
                      Row row,
                      CsvFileWriter csvFileWriter,
                      PropertyFileUtil propertyFileUtil,
                      RestApi restApi,
                      PrintUtil printUtil,
                      ActionTotals actionTotals) {
        super(entityInfo, row, csvFileWriter, propertyFileUtil, restApi, printUtil, actionTotals);
    }

    /**
     * Run method on this runnable object called by the thread manager.
     *
     * At this point, we should have an entity type that we know we can delete (soft or hard).
     */
    @Override
    public void run() {
        Result result;
        try {
            result = handle();
        } catch (Exception e) {
            result = handleFailure(e, bullhornId);
        }
        writeToResultCsv(result);
    }

    private <D extends DeleteEntity> Result handle() throws IOException {
        if (!row.hasValue(StringConsts.ID)) {
            throw new IllegalArgumentException("Cannot Perform Delete: missing '" + StringConsts.ID + "' column.");
        }

        bullhornId = Integer.parseInt(row.getValue(StringConsts.ID));

        if (!isEntityDeletable(bullhornId)) {
            throw new RestApiException("Cannot Perform Delete: " + entityInfo.getEntityName()
                + " record with ID: " + bullhornId + " does not exist or has already been soft-deleted.");
        }

        restApi.deleteEntity((Class<D>) entityInfo.getEntityClass(), bullhornId);
        return Result.delete(bullhornId);
    }

    /**
     * Returns true if the given internal ID corresponds to a record that can be deleted
     *
     * @param bullhornId The internal ID
     * @return True if deletable, false otherwise
     */
    private Boolean isEntityDeletable(Integer bullhornId) throws IOException {
        Map<String, String> existFieldsMap = new HashMap<>();
        existFieldsMap.put(StringConsts.ID, bullhornId.toString());

        if (entityInfo.isSoftDeletable()) {
            existFieldsMap.putAll(getIsDeletedField());
            List<B> existingEntityList = findEntityList(existFieldsMap);
            return !existingEntityList.isEmpty();
        } else if (entityInfo.isHardDeletable()) {
            List<B> existingEntityList = findEntityList(existFieldsMap);
            return !existingEntityList.isEmpty();
        } else {
            throw new RestApiException("Cannot Perform Delete: " + entityInfo.getEntityName()
                + " records are not deletable.");
        }
    }

    private Map<String, String> getIsDeletedField() {
        Map<String, String> existFieldsMap = new HashMap<>();
        if (entityInfo == EntityInfo.NOTE) {
            existFieldsMap.put(StringConsts.IS_DELETED, "false");
        } else {
            existFieldsMap.put(StringConsts.IS_DELETED, "0");
        }
        return existFieldsMap;
    }
}

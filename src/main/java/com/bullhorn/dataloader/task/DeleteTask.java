package com.bullhorn.dataloader.task;

import com.bullhorn.dataloader.data.ActionTotals;
import com.bullhorn.dataloader.data.CsvFileWriter;
import com.bullhorn.dataloader.data.Result;
import com.bullhorn.dataloader.enums.EntityInfo;
import com.bullhorn.dataloader.rest.RestApi;
import com.bullhorn.dataloader.util.PrintUtil;
import com.bullhorn.dataloader.util.PropertyFileUtil;
import com.bullhorn.dataloader.util.StringConsts;
import com.bullhornsdk.data.exception.RestApiException;
import com.bullhornsdk.data.model.entity.association.EntityAssociations;
import com.bullhornsdk.data.model.entity.core.type.AssociationEntity;
import com.bullhornsdk.data.model.entity.core.type.BullhornEntity;
import com.bullhornsdk.data.model.entity.core.type.DeleteEntity;
import com.bullhornsdk.data.model.response.crud.CrudResponse;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Responsible for deleting a single row from a CSV input file.
 */
public class DeleteTask<A extends AssociationEntity, E extends EntityAssociations, B extends BullhornEntity> extends AbstractTask<A, E, B> {
    private Integer bullhornID;

    public DeleteTask(Integer rowNumber,
                      EntityInfo entityInfo,
                      Map<String, String> dataMap,
                      CsvFileWriter csvFileWriter,
                      PropertyFileUtil propertyFileUtil,
                      RestApi restApi,
                      PrintUtil printUtil,
                      ActionTotals actionTotals) {
        super(rowNumber, entityInfo, dataMap, csvFileWriter, propertyFileUtil, restApi, printUtil, actionTotals);
    }

    /**
     * Run method on this runnable object called by the thread manager.
     * <p>
     * At this point, we should have an entity type that we know we can delete (soft or hard).
     */
    @Override
    public void run() {
        Result result;
        try {
            result = handle();
        } catch (Exception e) {
            result = handleFailure(e, bullhornID);
        }
        writeToResultCSV(result);
    }

    private <D extends DeleteEntity> Result handle() throws IOException {
        if (!dataMap.containsKey(StringConsts.ID)) {
            throw new IllegalArgumentException("Row " + rowNumber + ": Cannot Perform Delete: missing '" + StringConsts.ID + "' column.");
        }

        bullhornID = Integer.parseInt(dataMap.get(StringConsts.ID));

        if (!isEntityDeletable(bullhornID)) {
            throw new RestApiException("Row " + rowNumber + ": Cannot Perform Delete: " + entityInfo.getEntityName() +
                " record with ID: " + bullhornID + " does not exist or has already been soft-deleted.");
        }

        CrudResponse response = restApi.deleteEntity((Class<D>) entityInfo.getEntityClass(), bullhornID);
        checkForRestSdkErrorMessages(response);
        return Result.Delete(bullhornID);
    }

    /**
     * Returns true if the given internal ID corresponds to a record that can be deleted
     *
     * @param bullhornID The internal ID
     * @return True if deletable, false otherwise
     */
    private Boolean isEntityDeletable(Integer bullhornID) throws IOException {
        Map<String, String> existFieldsMap = new HashMap<>();
        existFieldsMap.put(StringConsts.ID, bullhornID.toString());

        if (entityInfo.isSoftDeletable()) {
            existFieldsMap.putAll(getIsDeletedField());
            List<B> existingEntityList = findEntityList(existFieldsMap);
            return !existingEntityList.isEmpty();
        } else if (entityInfo.isHardDeletable()) {
            List<B> existingEntityList = findEntityList(existFieldsMap);
            return !existingEntityList.isEmpty();
        } else {
            throw new RestApiException("Row " + rowNumber + ": Cannot Perform Delete: " + entityInfo.getEntityName() +
                " records are not deletable.");
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

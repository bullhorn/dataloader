package com.bullhorn.dataloader.task;

import com.bullhorn.dataloader.meta.EntityInfo;
import com.bullhorn.dataloader.service.Command;
import com.bullhorn.dataloader.service.csv.CsvFileWriter;
import com.bullhorn.dataloader.service.csv.Result;
import com.bullhorn.dataloader.util.ActionTotals;
import com.bullhorn.dataloader.util.PrintUtil;
import com.bullhorn.dataloader.util.PropertyFileUtil;
import com.bullhornsdk.data.api.BullhornData;
import com.bullhornsdk.data.exception.RestApiException;
import com.bullhornsdk.data.model.entity.association.EntityAssociations;
import com.bullhornsdk.data.model.entity.core.type.AssociationEntity;
import com.bullhornsdk.data.model.entity.core.type.BullhornEntity;
import com.bullhornsdk.data.model.entity.core.type.DeleteEntity;
import com.bullhornsdk.data.model.response.crud.CrudResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Responsible for deleting a single row from a CSV input file.
 */
public class DeleteTask<A extends AssociationEntity, E extends EntityAssociations, B extends BullhornEntity> extends AbstractTask<A, E, B> {
    private static final Logger log = LogManager.getLogger(DeleteTask.class);
    private Integer bullhornID;

    public DeleteTask(Command command,
                      Integer rowNumber,
                      EntityInfo entityInfo,
                      LinkedHashMap<String, String> dataMap,
                      CsvFileWriter csvWriter,
                      PropertyFileUtil propertyFileUtil,
                      BullhornData bullhornData,
                      PrintUtil printUtil,
                      ActionTotals actionTotals) {
        super(command, rowNumber, entityInfo, dataMap, csvWriter, propertyFileUtil, bullhornData, printUtil, actionTotals);
    }

    /**
     * Run method on this runnable object called by the thread manager.
     * <p>
     * At this point, we should have an entityClass type that we know we can delete (soft or hard).
     */
    @Override
    public void run() {
        init();
        Result result;
        try {
            result = handle();
        } catch (Exception e) {
            result = handleFailure(e, bullhornID);
        }
        writeToResultCSV(result);
    }

    private <D extends DeleteEntity> Result handle() throws IOException {
        // TODO: Throw error from DeleteService if ID column is not present
        bullhornID = Integer.parseInt(dataMap.get(ID));

        if (isEntitySoftDeleted(bullhornID)) {
            throw new RestApiException("Row " + rowNumber + ": Cannot Perform Delete: " + entityClass.getSimpleName() +
                " record with ID: " + bullhornID + " has already been soft deleted.");
        }

        CrudResponse response = bullhornData.deleteEntity((Class<D>) entityClass, bullhornID);
        checkForRestSdkErrorMessages(response);
        return Result.Delete(bullhornID);
    }

    private Boolean isEntitySoftDeleted(Integer bullhornID) throws IOException {
        Map<String, String> softDeletedExistFieldsMap = new HashMap<>();
        softDeletedExistFieldsMap.put(ID, bullhornID.toString());
        softDeletedExistFieldsMap.put("isDeleted", "1");

        List<B> existingEntityList = findEntityList(softDeletedExistFieldsMap);
        return !existingEntityList.isEmpty();
    }
}

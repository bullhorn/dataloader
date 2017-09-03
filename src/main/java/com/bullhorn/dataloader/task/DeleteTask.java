package com.bullhorn.dataloader.task;

import com.bullhorn.dataloader.data.ActionTotals;
import com.bullhorn.dataloader.data.Cell;
import com.bullhorn.dataloader.data.CsvFileWriter;
import com.bullhorn.dataloader.data.Result;
import com.bullhorn.dataloader.data.Row;
import com.bullhorn.dataloader.enums.EntityInfo;
import com.bullhorn.dataloader.rest.Field;
import com.bullhorn.dataloader.rest.RestApi;
import com.bullhorn.dataloader.util.PrintUtil;
import com.bullhorn.dataloader.util.PropertyFileUtil;
import com.bullhorn.dataloader.util.StringConsts;
import com.bullhornsdk.data.exception.RestApiException;
import com.bullhornsdk.data.model.entity.core.type.BullhornEntity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Responsible for deleting a single row from a CSV input file.
 */
public class DeleteTask<B extends BullhornEntity> extends AbstractTask<B> {

    public DeleteTask(EntityInfo entityInfo,
                      Row row,
                      CsvFileWriter csvFileWriter,
                      PropertyFileUtil propertyFileUtil,
                      RestApi restApi,
                      PrintUtil printUtil,
                      ActionTotals actionTotals) {
        super(entityInfo, row, csvFileWriter, propertyFileUtil, restApi, printUtil, actionTotals);
    }

    @SuppressWarnings("unchecked")
    protected Result handle() throws IOException, IllegalAccessException, InstantiationException {
        if (!row.hasValue(StringConsts.ID)) {
            throw new IllegalArgumentException("Cannot Perform Delete: missing '" + StringConsts.ID + "' column.");
        }

        entityId = Integer.parseInt(row.getValue(StringConsts.ID));
        if (!isEntityDeletable(entityId)) {
            throw new RestApiException("Cannot Perform Delete: " + entityInfo.getEntityName()
                + " record with ID: " + entityId + " does not exist or has already been soft-deleted.");
        }

        restApi.deleteEntity(entityInfo.getEntityClass(), entityId);
        return Result.delete(entityId);
    }

    /**
     * Returns true if the given internal ID corresponds to a record that can be deleted
     *
     * @param bullhornId The internal ID
     * @return True if deletable, false otherwise
     */
    private Boolean isEntityDeletable(Integer bullhornId) throws IOException {
        List<Field> entityExistFields = new ArrayList<>();
        Cell idCell = new Cell(StringConsts.ID, bullhornId.toString());
        Field idField = new Field(entityInfo, idCell, true, propertyFileUtil.getDateParser());
        entityExistFields.add(idField);

        if (entityInfo.isSoftDeletable()) {
            Cell isDeletedCell = new Cell(StringConsts.IS_DELETED, entityInfo.getSearchIsDeletedValue(false));
            Field isDeletedField = new Field(entityInfo, isDeletedCell, true, propertyFileUtil.getDateParser());
            entityExistFields.add(isDeletedField);
            List<B> existingEntityList = findEntityList(entityExistFields);
            return !existingEntityList.isEmpty();
        } else if (entityInfo.isHardDeletable()) {
            List<B> existingEntityList = findEntityList(entityExistFields);
            return !existingEntityList.isEmpty();
        } else {
            throw new RestApiException("Cannot Perform Delete: " + entityInfo.getEntityName()
                + " records are not deletable.");
        }
    }
}

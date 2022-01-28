package com.bullhorn.dataloader.task;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import com.bullhorn.dataloader.data.ActionTotals;
import com.bullhorn.dataloader.data.Cell;
import com.bullhorn.dataloader.data.CsvFileWriter;
import com.bullhorn.dataloader.data.Result;
import com.bullhorn.dataloader.data.Row;
import com.bullhorn.dataloader.enums.EntityInfo;
import com.bullhorn.dataloader.enums.ErrorInfo;
import com.bullhorn.dataloader.rest.Cache;
import com.bullhorn.dataloader.rest.CompleteUtil;
import com.bullhorn.dataloader.rest.Field;
import com.bullhorn.dataloader.rest.RestApi;
import com.bullhorn.dataloader.util.DataLoaderException;
import com.bullhorn.dataloader.util.PrintUtil;
import com.bullhorn.dataloader.util.PropertyFileUtil;
import com.bullhorn.dataloader.util.StringConsts;
import com.bullhornsdk.data.model.entity.core.type.BullhornEntity;
import com.google.common.collect.Sets;

/**
 * Responsible for deleting a single row from a CSV input file.
 */
public class DeleteTask extends AbstractTask {

    DeleteTask(EntityInfo entityInfo,
               Row row,
               CsvFileWriter csvFileWriter,
               PropertyFileUtil propertyFileUtil,
               RestApi restApi,
               PrintUtil printUtil,
               ActionTotals actionTotals,
               Cache cache, CompleteUtil completeUtil) {
        super(entityInfo, row, csvFileWriter, propertyFileUtil, restApi, printUtil, actionTotals, cache, completeUtil);
    }

    @SuppressWarnings("unchecked")
    protected Result handle() throws InvocationTargetException, IllegalAccessException {
        if (!row.hasValue(StringConsts.ID)) {
            throw new DataLoaderException(ErrorInfo.MISSING_REQUIRED_COLUMN, "Missing the '" + StringConsts.ID + "' column.");
        }

        if (!entityInfo.isSoftDeletable() && !entityInfo.isHardDeletable()) {
            throw new DataLoaderException(ErrorInfo.CANNOT_PERFORM_DELETE, entityInfo.getEntityName() + " records are not deletable.");
        }

        entityId = Integer.parseInt(row.getValue(StringConsts.ID));
        Cell idCell = new Cell(StringConsts.ID, entityId.toString());
        Field idField = new Field(entityInfo, idCell, true, propertyFileUtil.getDateParser());
        List<Field> entityExistFields = new ArrayList<>();
        entityExistFields.add(idField);

        List<BullhornEntity> existingEntities = findActiveEntities(entityExistFields, Sets.newHashSet(StringConsts.ID), true);
        if (existingEntities.isEmpty()) {
            throw new DataLoaderException(ErrorInfo.MISSING_OR_DELETED_RECORD, entityInfo.getEntityName()
                + " record with ID: " + entityId + " does not exist or has already been soft-deleted.");
        }

        restApi.deleteEntity(entityInfo.getEntityClass(), entityId);
        return Result.delete(entityId);
    }
}

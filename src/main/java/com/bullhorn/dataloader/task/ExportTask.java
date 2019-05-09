package com.bullhorn.dataloader.task;

import com.bullhorn.dataloader.data.ActionTotals;
import com.bullhorn.dataloader.data.Cell;
import com.bullhorn.dataloader.data.CsvFileWriter;
import com.bullhorn.dataloader.data.Result;
import com.bullhorn.dataloader.data.Row;
import com.bullhorn.dataloader.enums.EntityInfo;
import com.bullhorn.dataloader.rest.Cache;
import com.bullhorn.dataloader.rest.CompleteUtil;
import com.bullhorn.dataloader.rest.Field;
import com.bullhorn.dataloader.rest.Record;
import com.bullhorn.dataloader.rest.RestApi;
import com.bullhorn.dataloader.util.FindUtil;
import com.bullhorn.dataloader.util.PrintUtil;
import com.bullhorn.dataloader.util.PropertyFileUtil;
import com.bullhornsdk.data.exception.RestApiException;
import com.bullhornsdk.data.model.entity.core.type.BullhornEntity;

import java.util.List;

/**
 * Finds current values for existing entity in Rest and overwrites the value in the row with values from Rest.
 */
public class ExportTask extends AbstractTask {

    ExportTask(EntityInfo entityInfo,
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
    protected Result handle() throws Exception {
        Record record = new Record(entityInfo, row, propertyFileUtil);
        List<Field> entityExistFields = record.getEntityExistFields();
        if (entityExistFields.isEmpty()) {
            throw new RestApiException("Cannot perform export because exist field is not specified for entity: " + entityInfo.getEntityName());
        }

        // Lookup existing entities
        Boolean isPrimaryEntity = true;
        List<BullhornEntity> foundEntityList = findEntities(entityExistFields, record.getFieldsParameter(isPrimaryEntity), isPrimaryEntity);
        if (foundEntityList.isEmpty()) {
            throw new RestApiException(FindUtil.getNoMatchingRecordsExistMessage(entityInfo, record.getEntityExistFields()));
        }

        // TODO: Write out all rows found
        for (BullhornEntity bullhornEntity : foundEntityList) {
            Row newRow = new Row(row.getFilePath(), row.getNumber());
            for (Field field : record.getFields()) {
                String value = field.getStringValueFromEntity(bullhornEntity, propertyFileUtil.getListDelimiter());
                Cell updatedCell = new Cell(field.getCell().getName(), value);
                newRow.addCell(updatedCell);
            }
            csvFileWriter.writeRow(newRow, Result.export(bullhornEntity.getId()));
        }

        // Create new rows of data
        BullhornEntity entity = foundEntityList.get(0);
        entityId = entity.getId();

        // Replace row with current data from Rest
        Row updatedRow = new Row(row.getFilePath(), row.getNumber());
        for (Field field : record.getFields()) {
            String value = field.getStringValueFromEntity(entity, propertyFileUtil.getListDelimiter());
            Cell updatedCell = new Cell(field.getCell().getName(), value);
            updatedRow.addCell(updatedCell);
        }
        row = updatedRow;

        return Result.export(entityId);
    }
}

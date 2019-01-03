package com.bullhorn.dataloader.task;

import com.bullhorn.dataloader.data.ActionTotals;
import com.bullhorn.dataloader.data.Cell;
import com.bullhorn.dataloader.data.CsvFileWriter;
import com.bullhorn.dataloader.data.Result;
import com.bullhorn.dataloader.data.Row;
import com.bullhorn.dataloader.enums.EntityInfo;
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

    public ExportTask(EntityInfo entityInfo,
                      Row row,
                      CsvFileWriter csvFileWriter,
                      PropertyFileUtil propertyFileUtil,
                      RestApi restApi,
                      PrintUtil printUtil,
                      ActionTotals actionTotals,
                      CompleteUtil completeUtil) {
        super(entityInfo, row, csvFileWriter, propertyFileUtil, restApi, printUtil, actionTotals, completeUtil);
    }

    protected Result handle() throws Exception {
        Record record = new Record(entityInfo, row, propertyFileUtil);

        List<Field> entityExistFields = record.getEntityExistFields();
        if (entityExistFields.isEmpty()) {
            throw new RestApiException("Cannot perform export because exist field is not specified for entity: " + entityInfo.getEntityName());
        }

        List<BullhornEntity> foundEntityList = findEntities(entityExistFields, record.getFieldsParameter(), true);
        if (foundEntityList.isEmpty()) {
            throw new RestApiException(FindUtil.getNoMatchingRecordsExistMessage(entityInfo, record.getEntityExistFields()));
        } else if (foundEntityList.size() > 1) {
            throw new RestApiException(FindUtil.getMultipleRecordsExistMessage(entityInfo, record.getEntityExistFields(), foundEntityList.size()));
        }
        BullhornEntity entity = foundEntityList.get(0);
        entityId = entity.getId();

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

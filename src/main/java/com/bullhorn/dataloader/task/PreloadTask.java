package com.bullhorn.dataloader.task;

import java.util.List;

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
import com.bullhorn.dataloader.util.AssociationUtil;
import com.bullhorn.dataloader.util.FindUtil;
import com.bullhorn.dataloader.util.PrintUtil;
import com.bullhorn.dataloader.util.PropertyFileUtil;
import com.bullhornsdk.data.exception.RestApiException;
import com.bullhornsdk.data.model.entity.core.type.AssociationEntity;
import com.bullhornsdk.data.model.entity.core.type.BullhornEntity;
import com.bullhornsdk.data.model.entity.embedded.OneToMany;
import com.bullhornsdk.data.model.parameter.standard.ParamFactory;
import com.google.common.collect.Sets;

/**
 * Finds current values for existing entity in Rest and overwrites the value in the row with values from Rest.
 */
public class PreloadTask extends AbstractTask {

    private final PrintUtil printUtil;

    public PreloadTask(EntityInfo entityInfo,
                       Row row,
                       CsvFileWriter csvFileWriter,
                       PropertyFileUtil propertyFileUtil,
                       RestApi restApi,
                       PrintUtil printUtil,
                       ActionTotals actionTotals,
                       Cache cache, CompleteUtil completeUtil) {
        super(entityInfo, row, csvFileWriter, propertyFileUtil, restApi, printUtil, actionTotals, cache, completeUtil);
        this.printUtil = printUtil;
    }

    /**
     * Overrides the run method to work across multiple rows
     */
    public void run() {
        try {
            handle();
        } catch (Exception e) {
            printUtil.printAndLog("Error during preloading: " + e);
        }
    }

    @SuppressWarnings("unchecked")
    protected Result handle() throws Exception {
        Record record = new Record(entityInfo, row, propertyFileUtil);
        List<Field> entityExistFields = record.getEntityExistFields();
        if (entityExistFields.isEmpty()) {
            throw new RestApiException("Cannot perform export because exist field is not specified for entity: " + entityInfo.getEntityName());
        }

        // Lookup existing entities
        List<BullhornEntity> foundEntityList = findEntities(entityExistFields, record.getFieldsParameter(), true);
        if (foundEntityList.isEmpty()) {
            throw new RestApiException(FindUtil.getNoMatchingRecordsExistMessage(entityInfo, record.getEntityExistFields()));
        } else if (foundEntityList.size() > 1) {
            throw new RestApiException(FindUtil.getMultipleRecordsExistMessage(entityInfo, record.getEntityExistFields(), foundEntityList.size()));
        }
        BullhornEntity entity = foundEntityList.get(0);
        entityId = entity.getId();

        // Follow on query for associated entities that have not returned the full number of records
        for (Field field : record.getToManyFields()) {
            OneToMany existingToMany = field.getOneToManyFromEntity(entity);
            if (existingToMany != null && existingToMany.getTotal() > existingToMany.getData().size()) {
                List<BullhornEntity> associations = restApi.getAllAssociationsList((Class<AssociationEntity>) entityInfo.getEntityClass(),
                    Sets.newHashSet(entityId), AssociationUtil.getToManyField(field), Sets.newHashSet(field.getName()),
                    ParamFactory.associationParams());
                OneToMany newToMany = new OneToMany();
                newToMany.setTotal(existingToMany.getTotal());
                newToMany.setData(associations);
                field.populateOneToManyOnEntity(entity, newToMany);
            }
        }

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

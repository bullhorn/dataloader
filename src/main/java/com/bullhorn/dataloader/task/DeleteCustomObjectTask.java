package com.bullhorn.dataloader.task;

import com.bullhorn.dataloader.data.ActionTotals;
import com.bullhorn.dataloader.data.CsvFileWriter;
import com.bullhorn.dataloader.data.Result;
import com.bullhorn.dataloader.data.Row;
import com.bullhorn.dataloader.enums.EntityInfo;
import com.bullhorn.dataloader.rest.Preloader;
import com.bullhorn.dataloader.rest.RestApi;
import com.bullhorn.dataloader.util.AssociationUtil;
import com.bullhorn.dataloader.util.PrintUtil;
import com.bullhorn.dataloader.util.PropertyFileUtil;
import com.bullhorn.dataloader.util.StringConsts;
import com.bullhornsdk.data.model.entity.association.AssociationField;
import com.bullhornsdk.data.model.entity.association.EntityAssociations;
import com.bullhornsdk.data.model.entity.core.type.AssociationEntity;
import com.bullhornsdk.data.model.entity.core.type.BullhornEntity;
import com.google.common.collect.Sets;

import java.io.IOException;

/**
 * Responsible for deleting a single row from a CSV input file.
 */
public class DeleteCustomObjectTask<A extends AssociationEntity, E extends EntityAssociations,
    B extends BullhornEntity> extends LoadCustomObjectTask<A, E, B> {

    public DeleteCustomObjectTask(EntityInfo entityInfo,
                                  Row row,
                                  Preloader preloader,
                                  CsvFileWriter csvFileWriter,
                                  PropertyFileUtil propertyFileUtil,
                                  RestApi restApi,
                                  PrintUtil printUtil,
                                  ActionTotals actionTotals) {
        super(entityInfo, row, preloader, csvFileWriter, propertyFileUtil, restApi, printUtil, actionTotals);
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
            result = handleFailure(e, entityId);
        }
        writeToResultCsv(result);
    }

    protected Result handle() throws Exception {
        if (!row.hasValue(StringConsts.ID)) {
            throw new IllegalArgumentException("Cannot Perform Delete: missing '" + StringConsts.ID + "' column.");
        }

        entityId = Integer.parseInt(row.getValue("id"));
        String parentEntityField = getParentEntityField();
        getParentEntity(parentEntityField);
        deleteCustomObject(parentEntity.getId());
        return Result.delete(entityId);
    }

    /**
     * This peforms a disassociate call, which will not hard delete the custom object, only remove
     * it from it's association to the parent entity.
     *
     * @param parentEntityId The id of the parentEntity
     */
    private void deleteCustomObject(Integer parentEntityId) {
        AssociationField associationField = AssociationUtil.getCustomObjectField(entityInfo,
            EntityInfo.fromString(parentEntityClass.getSimpleName()));
        restApi.disassociateWithEntity((Class<A>) parentEntityClass, parentEntityId, associationField,
            Sets.newHashSet(entityId));
    }

    private String getParentEntityField() throws IOException {
        String parentField = "";
        for (String field : row.getNames()) {
            if (field.contains(".") && !field.contains("_")) {
                parentField = field;
            }
        }
        if (parentField.equals("")) {
            throw new IOException("No association entities found in csv for " + entityInfo.getEntityName()
                + ". CustomObjectInstances require a parent entity in the csv.");
        }
        return parentField;
    }
}

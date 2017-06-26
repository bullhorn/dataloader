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
import com.bullhornsdk.data.exception.RestApiException;
import com.bullhornsdk.data.model.entity.association.AssociationField;
import com.bullhornsdk.data.model.entity.association.EntityAssociations;
import com.bullhornsdk.data.model.entity.core.type.AssociationEntity;
import com.bullhornsdk.data.model.entity.core.type.BullhornEntity;
import com.bullhornsdk.data.model.response.crud.CrudResponse;
import com.google.common.collect.Sets;

import java.io.IOException;
import java.util.List;

/**
 * Responsible for deleting a single row from a CSV input file.
 */
public class DeleteCustomObjectTask<A extends AssociationEntity, E extends EntityAssociations, B extends BullhornEntity> extends LoadCustomObjectTask<A, E, B> {

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
     * <p>
     * At this point, we should have an entity type that we know we can delete (soft or hard).
     */
    @Override
    public void run() {
        Result result;
        try {
            result = handle();
        } catch (Exception e) {
            result = handleFailure(e, entityID);
        }
        writeToResultCSV(result);
    }

    protected Result handle() throws Exception {
        if (!dataMap.containsKey(StringConsts.ID)) {
            throw new IllegalArgumentException("Row " + rowNumber + ": Cannot Perform Delete: missing '" + StringConsts.ID + "' column.");
        }

        entityID = Integer.parseInt(dataMap.get("id"));
        String parentEntityField = getParentEntityField();
        getParentEntity(parentEntityField);
        deleteCustomObject(parentEntity.getId());
        return Result.Delete(entityID);
    }

    /**
     * This peforms a disassociate call, which will not hard delete the custom object, only remove
     * it from it's association to the parent entity.
     *
     * @param parentEntityID The id of the parentEntity
     */
    private void deleteCustomObject(Integer parentEntityID) {
        AssociationField associationField = getAssociationField();
        CrudResponse response = restApi.disassociateWithEntity((Class<A>) parentEntityClass, parentEntityID, associationField, Sets.newHashSet(entityID));
        checkForRestSdkErrorMessages(response);
    }

    private String getParentEntityField() throws IOException {
        String parentField = "";
        for (String field : dataMap.keySet()) {
            if (field.contains(".") && !field.contains("_")) {
                parentField = field;
            }
        }
        if (parentField.equals("")) {
            throw new IOException("No association entities found in csv for " + entityInfo.getEntityName() + ". CustomObjectInstances require a parent entity in the csv.");
        }
        return parentField;
    }

    // TODO: Move to Association Util
    private AssociationField getAssociationField() {
        String associationName = getAssociationName();
        List<AssociationField<AssociationEntity, BullhornEntity>> associationFieldList = AssociationUtil.getAssociationFields((Class<AssociationEntity>) parentEntityClass);
        for (AssociationField associationField : associationFieldList) {
            if (associationField.getAssociationFieldName().equalsIgnoreCase(associationName)) {
                return associationField;
            }
        }
        throw new RestApiException("Cannot find association field for association " + associationName);
    }

    private String getAssociationName() {
        String entityName = entityInfo.getEntityName();
        String instanceNumber = entityName.substring(entityName.length() - 1, entityName.length());
        return "customObject" + instanceNumber + "s";
    }
}

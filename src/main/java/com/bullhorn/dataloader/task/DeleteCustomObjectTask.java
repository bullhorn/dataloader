package com.bullhorn.dataloader.task;

import com.bullhorn.dataloader.enums.Command;
import com.bullhorn.dataloader.enums.EntityInfo;
import com.bullhorn.dataloader.service.csv.CsvFileWriter;
import com.bullhorn.dataloader.service.csv.Result;
import com.bullhorn.dataloader.service.executor.BullhornRestApi;
import com.bullhorn.dataloader.util.ActionTotals;
import com.bullhorn.dataloader.util.AssociationUtil;
import com.bullhorn.dataloader.util.PrintUtil;
import com.bullhorn.dataloader.util.PropertyFileUtil;
import com.bullhornsdk.data.exception.RestApiException;
import com.bullhornsdk.data.model.entity.association.AssociationField;
import com.bullhornsdk.data.model.entity.association.EntityAssociations;
import com.bullhornsdk.data.model.entity.core.type.AssociationEntity;
import com.bullhornsdk.data.model.entity.core.type.BullhornEntity;
import com.bullhornsdk.data.model.response.crud.CrudResponse;
import com.google.common.collect.Sets;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Responsible for deleting a single row from a CSV input file.
 */
public class DeleteCustomObjectTask<A extends AssociationEntity, E extends EntityAssociations, B extends BullhornEntity> extends LoadCustomObjectTask<A, E, B> {

    public DeleteCustomObjectTask(Command command,
                                Integer rowNumber,
                                EntityInfo entityInfo,
                                Map<String, String> dataMap,
                                CsvFileWriter csvWriter,
                                PropertyFileUtil propertyFileUtil,
                                BullhornRestApi bullhornRestApi,
                                PrintUtil printUtil,
                                ActionTotals actionTotals) {
        super(command, rowNumber, entityInfo, dataMap, null, null, csvWriter, propertyFileUtil, bullhornRestApi, printUtil, actionTotals);
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
        entityID = Integer.parseInt(dataMap.get("id"));
        String parentEntityField = getParentEntityField();
        Integer parentEntityID = Integer.parseInt(dataMap.get(parentEntityField));

        getParentEntity(parentEntityField);
        deleteCustomObject(parentEntityID);
        return Result.Delete(entityID);
    }

    private void deleteCustomObject(Integer parentEntityID) {
        AssociationField associationField = getAssociationField();
        CrudResponse response = bullhornRestApi.disassociateWithEntity((Class<A>) parentEntityClass, parentEntityID, associationField, Sets.newHashSet(entityID));
        checkForRestSdkErrorMessages(response);
    }

    private String getParentEntityField() throws IOException {
        String parentField = "";
        for (String field : dataMap.keySet()){
            if (field.contains(".") && !field.contains("_")){
                parentField = field;
            }
        }
        if (parentField ==""){
            throw new IOException("No association entities found in csv for " + entityInfo.getEntityName() + ". CustomObjectInstances require a parent entity in the csv.");
        }
        return parentField;
    }

    private AssociationField getAssociationField() {
        String associationName = getAssociationName();
        List<AssociationField<AssociationEntity, BullhornEntity>> associationFieldList = AssociationUtil.getAssociationFields((Class<AssociationEntity>) parentEntityClass);
        for (AssociationField associationField : associationFieldList) {
            if (associationField.getAssociationFieldName().equalsIgnoreCase(associationName)){
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

    @Override
    protected void getParentEntity(String field) throws Exception {
        String entityName = entityInfo.getEntityName();

        if (entityName.toLowerCase().contains("person")) {
            getPersonCustomObjectParentEntityClass(entityName);
        } else {
            getParentEntityClass(field);
        }
    }
}

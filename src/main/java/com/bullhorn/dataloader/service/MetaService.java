package com.bullhorn.dataloader.service;

import com.bullhorn.dataloader.enums.EntityInfo;
import com.bullhorn.dataloader.rest.RestApi;
import com.bullhorn.dataloader.rest.RestSession;
import com.bullhorn.dataloader.util.FileUtil;
import com.bullhorn.dataloader.util.MethodUtil;
import com.bullhorn.dataloader.util.PrintUtil;
import com.bullhorn.dataloader.util.StringConsts;
import com.bullhorn.dataloader.util.ValidationUtil;
import com.bullhornsdk.data.model.entity.meta.Field;
import com.bullhornsdk.data.model.entity.meta.MetaData;
import com.bullhornsdk.data.model.enums.MetaParameter;
import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Output meta data for a given entity
 */
public class MetaService implements Action {

    private final RestSession restSession;
    private final PrintUtil printUtil;

    MetaService(RestSession restSession, PrintUtil printUtil) {
        this.restSession = restSession;
        this.printUtil = printUtil;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void run(String[] args) {
        EntityInfo entityInfo = FileUtil.extractEntityFromFileName(args[1]);
        RestApi restApi = restSession.getRestApi();

        try {
            printUtil.log("Getting meta for " + Objects.requireNonNull(entityInfo).getEntityName() + "...");
            MetaData metaData = restApi.getMetaData(entityInfo.getEntityClass(), MetaParameter.FULL, null);
            enrichMeta(metaData);
            JSONObject jsonMeta = metaToJson(metaData);
            printUtil.print(jsonMeta.toString());
            printUtil.log("Done generating meta for " + Objects.requireNonNull(entityInfo).getEntityName());
        } catch (Exception e) {
            printUtil.printAndLog("ERROR: Failed to get Meta for " + Objects.requireNonNull(entityInfo).getEntityName());
            printUtil.printAndLog(e);
        }
    }

    @Override
    public boolean isValidArguments(String[] args) {
        return ValidationUtil.validateNumArgs(args, 2, printUtil)
            && ValidationUtil.validateEntityName(args[1], printUtil);
    }

    /**
     * One layer deep enrichment of meta data to match SDK-REST.
     */
    @SuppressWarnings("unchecked")
    private void enrichMeta(MetaData metaData) {
        enrichMetaForEntity(metaData);
        List<Field> fields = metaData.getFields();
        for (Field field : fields) {
            if (field.getAssociatedEntity() != null) {
                enrichMetaForEntity(field.getAssociatedEntity());
            }
        }
    }

    /**
     * Given the meta for an entity or associated entity, use the SDK to enrich that meta.
     */
    @SuppressWarnings("unchecked")
    private void enrichMetaForEntity(MetaData metaData) {
        EntityInfo entityInfo = EntityInfo.fromString(metaData.getEntity());
        if (entityInfo != null) {
            List<Field> fields = metaData.getFields();
            Map<String, Method> setterMethodMap = MethodUtil.getSetterMethodMap(entityInfo.getEntityClass());

            // Throw out fields in meta that are not in the SDK
            List<Field> fieldsToRemove = new ArrayList();
            for (Field field : fields) {
                if (!setterMethodMap.containsKey(field.getName().toLowerCase())) {
                    fieldsToRemove.add(field);
                    printUtil.log("Removed " + entityInfo.getEntityName() + " field: "
                        + field.getName() + " that does not exist in SDK-REST.");
                }
            }
            fields.removeAll(fieldsToRemove);

            // Add additional fields from SDK-REST that are not in meta
            if (setterMethodMap.containsKey(StringConsts.EXTERNAL_ID.toLowerCase())
                && fields.stream().noneMatch(field -> field.getName().equals(StringConsts.EXTERNAL_ID))) {
                Field externalIdField = new Field();
                externalIdField.setName(StringConsts.EXTERNAL_ID);
                externalIdField.setType("SCALAR");
                externalIdField.setDataType("String");
                fields.add(externalIdField);
                printUtil.log("Added " + entityInfo.getEntityName() + " field: "
                    + StringConsts.EXTERNAL_ID + " that was not in Meta.");
            }
        }
    }

    @SuppressWarnings("unchecked")
    private JSONObject metaToJson(MetaData metaData) {
        JSONArray jsonFields = new JSONArray();
        List<Field> fields = metaData.getFields();
        for (Field field : fields) {
            JSONObject jsonField = new JSONObject();
            jsonField.put("name", field.getName());
            jsonField.put("type", field.getType());
            jsonField.put("dataType", field.getDataType());
            jsonField.put("dataSpecialization", field.getDataSpecialization());
            jsonField.put("confidential", field.getConfidential());
            jsonField.put("optional", field.getOptional());
            jsonField.put("label", field.getLabel());
            jsonField.put("required", field.getRequired());
            jsonField.put("readOnly", field.getReadOnly());
            jsonField.put("multiValue", field.getMultiValue());
            jsonField.put("sortOrder", field.getSortOrder());
            jsonField.put("hint", field.getHint());
            jsonField.put("description", field.getDescription());
            if (field.getAssociatedEntity() != null) {
                jsonField.put("associatedEntity", metaToJson(field.getAssociatedEntity()));
            }
            jsonFields.put(jsonField);
        }

        JSONObject jsonMeta = new JSONObject();
        jsonMeta.put("entity", metaData.getEntity());
        jsonMeta.put("label", metaData.getLabel());
        jsonMeta.put("fields", jsonFields);
        return jsonMeta;
    }
}

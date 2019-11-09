package com.bullhorn.dataloader.service;

import com.bullhorn.dataloader.enums.EntityInfo;
import com.bullhorn.dataloader.rest.RestApi;
import com.bullhorn.dataloader.rest.RestSession;
import com.bullhorn.dataloader.util.FileUtil;
import com.bullhorn.dataloader.util.PrintUtil;
import com.bullhorn.dataloader.util.ValidationUtil;
import com.bullhornsdk.data.model.entity.meta.Field;
import com.bullhornsdk.data.model.entity.meta.MetaData;
import com.bullhornsdk.data.model.enums.MetaParameter;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;
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
            MetaData meta = restApi.getMetaData(entityInfo.getEntityClass(), MetaParameter.FULL, null);
            JSONObject jsonMeta = metaToJson(meta);
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

    @SuppressWarnings("unchecked")
    private JSONObject metaToJson(MetaData metaData) {
        JSONArray jsonFields = new JSONArray();
        List<Field> fields = metaData.getFields();
        for (Field field : fields) {
            if (field.getDescription() != null) {
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
        }

        JSONObject jsonMeta = new JSONObject();
        jsonMeta.put("entity", metaData.getEntity());
        jsonMeta.put("label", metaData.getLabel());
        jsonMeta.put("fields", jsonFields);
        return jsonMeta;
    }
}

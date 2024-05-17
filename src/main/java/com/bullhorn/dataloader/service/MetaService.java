package com.bullhorn.dataloader.service;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.json.JSONArray;
import org.json.JSONObject;

import com.bullhorn.dataloader.enums.EntityInfo;
import com.bullhorn.dataloader.rest.RestApi;
import com.bullhorn.dataloader.rest.RestSession;
import com.bullhorn.dataloader.util.FileUtil;
import com.bullhorn.dataloader.util.MethodUtil;
import com.bullhorn.dataloader.util.PrintUtil;
import com.bullhorn.dataloader.util.StringConsts;
import com.bullhorn.dataloader.util.ValidationUtil;
import com.bullhornsdk.data.model.entity.core.paybill.optionslookup.SimplifiedOptionsLookup;
import com.bullhornsdk.data.model.entity.core.standard.PrivateLabel;
import com.bullhornsdk.data.model.entity.meta.Field;
import com.bullhornsdk.data.model.entity.meta.MetaData;
import com.bullhornsdk.data.model.entity.meta.StandardMetaData;
import com.bullhornsdk.data.model.enums.MetaParameter;
import com.google.common.collect.Sets;

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
        String entityName = Objects.requireNonNull(entityInfo).getEntityName();
        RestApi restApi = restSession.getRestApi();

        try {
            printUtil.log("Getting meta for " + entityName + "...");
            MetaData<?> metaData = restApi.getMetaData(entityInfo.getEntityClass(), MetaParameter.FULL, Sets.newHashSet(StringConsts.ALL_FIELDS));
            enrichMeta(metaData);
            String jsonString = metaToJson(metaData).toString();
            printUtil.print(jsonString);
            FileUtil.writeStringToFileAndLogException("meta.json", jsonString, printUtil);
            printUtil.log("Done generating meta for " + entityName);
        } catch (Exception e) {
            printUtil.printAndLog("ERROR: Failed to get Meta for " + entityName);
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
    private void enrichMeta(MetaData<?> metaData) {
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
    private void enrichMetaForEntity(MetaData<?> metaData) {
        EntityInfo entityInfo = EntityInfo.fromString(metaData.getEntity());
        if (entityInfo != null) {
            List<Field> fields = metaData.getFields();
            Map<String, Method> setterMethodMap = MethodUtil.getSetterMethodMap(entityInfo.getEntityClass());

            List<Field> fieldsToRemove = new ArrayList<>();
            for (Field field : fields) {
                // Throw out fields in meta that are not in the SDK
                if (!setterMethodMap.containsKey(field.getName().toLowerCase())) {
                    fieldsToRemove.add(field);
                    printUtil.log("Removed " + entityInfo.getEntityName() + " field: "
                        + field.getName() + " that does not exist in SDK-REST.");
                }

                // SimplifiedOptionsLookup should be treated as a direct field on the entity, since it is sent over as an id within an object
                if (SimplifiedOptionsLookup.class.getSimpleName().equals(field.getDataType())) {
                    field.setAssociatedEntity(null);
                    field.setType(StringConsts.SCALAR);
                }
            }
            fields.removeAll(fieldsToRemove);

            // Add additional fields from SDK-REST that are not in meta
            if (setterMethodMap.containsKey(StringConsts.EXTERNAL_ID.toLowerCase())
                && fields.stream().noneMatch(field -> field.getName().equals(StringConsts.EXTERNAL_ID))) {
                fields.add(createField(StringConsts.EXTERNAL_ID, StringConsts.EXTERNAL_ID, "SCALAR", "String"));
                printUtil.log("Added " + entityInfo.getEntityName() + " field: "
                    + StringConsts.EXTERNAL_ID + " that was not in Meta.");
            }
            if (entityInfo.equals(EntityInfo.WORKERS_COMPENSATION_RATE)
                && fields.stream().noneMatch(field -> field.getName().equals(StringConsts.PRIVATE_LABEL))) {
                StandardMetaData<PrivateLabel> associatedEntityMeta = new StandardMetaData<>();
                associatedEntityMeta.setEntity("PrivateLabel");
                associatedEntityMeta.setLabel("Private Label");
                associatedEntityMeta.setFields(new ArrayList<>(Collections.singletonList(
                    createField(StringConsts.ID, "ID", "ID", "Integer"))));
                Field privateLabelField = createField(StringConsts.PRIVATE_LABEL, "Private Label", "TO_ONE", null);
                privateLabelField.setAssociatedEntity(associatedEntityMeta);
                fields.add(privateLabelField);
                printUtil.log("Added " + entityInfo.getEntityName() + " field: "
                    + StringConsts.PRIVATE_LABEL + " that was not in Meta.");
            }
        }
    }

    private JSONObject metaToJson(MetaData<?> metaData) {
        JSONObject jsonMeta = new JSONObject();
        jsonMeta.put("entity", metaData.getEntity());
        jsonMeta.put("label", metaData.getLabel());
        jsonMeta.put("fields", fieldsToJson(metaData.getFields()));
        return jsonMeta;
    }

    private JSONArray fieldsToJson(List<Field> fields) {
        JSONArray jsonFields = new JSONArray();
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
            } else if (field.getFields().size() > 0) {
                jsonField.put("fields", fieldsToJson(field.getFields()));
            }
            jsonFields.put(jsonField);
        }
        return jsonFields;
    }

    /**
     * Convenience constructor that builds up a small Field object.
     */
    private static Field createField(String name, String label, String type, String dataType) {
        Field field = new Field();
        field.setName(name);
        field.setLabel(label);
        field.setType(type);
        field.setDataType(dataType);
        return field;
    }
}

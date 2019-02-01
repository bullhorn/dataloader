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
import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.List;
import java.util.Objects;

/**
 * Output meta data for a given entity
 */
public class MetaService implements Action {

    private final RestSession restSession;
    private final ValidationUtil validationUtil;
    private final PrintUtil printUtil;

    MetaService(RestSession restSession, ValidationUtil validationUtil, PrintUtil printUtil) {
        this.restSession = restSession;
        this.printUtil = printUtil;
        this.validationUtil = validationUtil;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void run(String[] args) {
        if (!isValidArguments(args)) {
            throw new IllegalArgumentException("invalid command line arguments");
        }

        EntityInfo entityInfo = FileUtil.extractEntityFromFileName(args[1]);
        RestApi restApi = restSession.getRestApi();

        try {
            printUtil.printAndLog("Getting Meta for " + Objects.requireNonNull(entityInfo).getEntityName() + "...");
            MetaData metaData = restApi.getMetaData(entityInfo.getEntityClass(), MetaParameter.FULL, null);
            List<Field> metaFields = metaData.getFields();
            JSONArray jsonArray = new JSONArray();
            metaFields.forEach(field -> {
                if (field.getDescription() != null) {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("name", field.getName());
                    jsonObject.put("label", field.getLabel());
                    jsonObject.put("description", field.getDescription());
                    jsonObject.put("hint", field.getHint());
                    jsonArray.put(jsonObject);
                }
            });
            String metaString = jsonArray.toString(2);
            File file = new File(entityInfo.getEntityName() + "Meta.json");
            FileUtils.writeStringToFile(file, metaString, "UTF-8");
            printUtil.printAndLog("Generated file: " + file.getName());
        } catch (Exception e) {
            printUtil.printAndLog("ERROR: Failed to get Meta for " + Objects.requireNonNull(entityInfo).getEntityName());
            printUtil.printAndLog(e);
        }
    }

    @Override
    public boolean isValidArguments(String[] args) {
        if (!validationUtil.isNumParametersValid(args, 2)) {
            return false;
        }

        EntityInfo entityInfo = FileUtil.extractEntityFromFileName(args[1]);
        if (entityInfo == null) {
            printUtil.printAndLog("ERROR: Meta requested is not valid: \"" + args[1] + "\" is not a valid entity.");
            return false;
        }

        return true;
    }
}

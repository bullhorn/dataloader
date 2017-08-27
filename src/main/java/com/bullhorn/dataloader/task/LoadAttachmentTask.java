package com.bullhorn.dataloader.task;

import com.bullhorn.dataloader.data.ActionTotals;
import com.bullhorn.dataloader.data.Cell;
import com.bullhorn.dataloader.data.CsvFileWriter;
import com.bullhorn.dataloader.data.Result;
import com.bullhorn.dataloader.data.Row;
import com.bullhorn.dataloader.enums.EntityInfo;
import com.bullhorn.dataloader.rest.RestApi;
import com.bullhorn.dataloader.util.MethodUtil;
import com.bullhorn.dataloader.util.PrintUtil;
import com.bullhorn.dataloader.util.PropertyFileUtil;
import com.bullhorn.dataloader.util.StringConsts;
import com.bullhornsdk.data.exception.RestApiException;
import com.bullhornsdk.data.model.entity.core.type.BullhornEntity;
import com.bullhornsdk.data.model.entity.core.type.FileEntity;
import com.bullhornsdk.data.model.entity.core.type.SearchEntity;
import com.bullhornsdk.data.model.file.FileMeta;
import com.bullhornsdk.data.model.file.standard.StandardFileMeta;
import com.bullhornsdk.data.model.parameter.standard.ParamFactory;
import com.bullhornsdk.data.model.response.file.FileContent;
import com.bullhornsdk.data.model.response.file.FileWrapper;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.commons.codec.binary.StringUtils;
import org.apache.commons.lang.WordUtils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

/**
 * Responsible for attaching a single row from a CSV input file.
 */
public class LoadAttachmentTask<B extends BullhornEntity> extends AbstractTask<B> {

    private FileMeta fileMeta;
    private boolean isNewEntity = true;
    private Map<String, Method> methodMap;
    private Integer bullhornParentId;

    public LoadAttachmentTask(EntityInfo entityInfo,
                              Row row,
                              CsvFileWriter csvFileWriter,
                              PropertyFileUtil propertyFileUtil,
                              RestApi restApi,
                              PrintUtil printUtil,
                              ActionTotals actionTotals) {
        super(entityInfo, row, csvFileWriter, propertyFileUtil, restApi, printUtil, actionTotals);
        this.methodMap = MethodUtil.getSetterMethodMap(FileMeta.class);
    }

    @Override
    public void run() {
        Result result;
        try {
            result = handle();
        } catch (Exception e) {
            result = handleFailure(e);
        }
        writeToResultCsv(result);
    }

    private Result handle() throws Exception {
        List<String> entityExistFields = propertyFileUtil.getEntityExistFields(entityInfo);
        if (entityExistFields.isEmpty()) {
            throw new IllegalArgumentException("Properties file is missing the '"
                + WordUtils.uncapitalize(entityInfo.getEntityName())
                + "ExistField' property required to lookup the parent entity.");
        }

        getAndSetBullhornId(entityExistFields);
        addParentEntityIDtoRow();
        createFileMeta();
        populateFileMeta();
        Result result = addOrUpdateFile();
        return result;
    }

    private void addParentEntityIDtoRow() {
        row.addCell(new Cell(StringConsts.PARENT_ENTITY_ID, bullhornParentId.toString()));
    }

    // attachments are keyed off of the <entity>ExistField property, NOT <entity>AttachmentExistField
    private <S extends SearchEntity> void getAndSetBullhornId(List<String> properties) throws Exception {
        if (properties.contains(getEntityAssociatedPropertyName(StringConsts.ID))) {
            bullhornParentId = Integer.parseInt(row.getValue(getEntityAssociatedPropertyName(StringConsts.ID)));
        } else {
            List<String> propertiesWithValues = Lists.newArrayList();
            for (String property : properties) {
                String propertyValue = row.getValue(getEntityAssociatedPropertyName(property));
                Class fieldType = getFieldType(entityInfo.getEntityClass(), WordUtils.uncapitalize(entityInfo.getEntityClass().getSimpleName()) + "ExistField", property);
                propertiesWithValues.add(getQueryStatement(property, propertyValue, fieldType, entityInfo.getEntityClass()));
            }
            String query = Joiner.on(" AND ").join(propertiesWithValues);
            List<S> searchList = restApi.searchForList((Class<S>) entityInfo.getEntityClass(), query, Sets.newHashSet("id"), ParamFactory.searchParams());
            if (!searchList.isEmpty()) {
                bullhornParentId = searchList.get(0).getId();
            } else {
                throw new RestApiException("Parent Entity not found.");
            }
        }
    }

    private String getEntityAssociatedPropertyName(String property) {
        return WordUtils.uncapitalize(entityInfo.getEntityName()) + "." + property;
    }

    private <F extends FileEntity> void createFileMeta() {
        fileMeta = new StandardFileMeta();

        List<FileMeta> allFileMetas = restApi.getFileMetaData((Class<F>) entityInfo.getEntityClass(), bullhornParentId);

        for (FileMeta curFileMeta : allFileMetas) {
            if (curFileMeta.getId().toString().equalsIgnoreCase(row.getValue(StringConsts.ID))
                || curFileMeta.getExternalID().equalsIgnoreCase(row.getValue(StringConsts.EXTERNAL_ID))
                ) {
                isNewEntity = false;
                fileMeta = curFileMeta;
                break;
            }
        }
    }

    private <F extends FileEntity> void populateFileMeta() throws Exception {
        File attachmentFile;

        try {
            attachmentFile = new File(row.getValue(StringConsts.RELATIVE_FILE_PATH));
        } catch (NullPointerException e) {
            throw new IOException("Missing the '" + StringConsts.RELATIVE_FILE_PATH + "' column required for loadAttachments");
        }

        if (isNewEntity || row.hasValue(StringConsts.RELATIVE_FILE_PATH)) {
            try {
                byte[] encoded = Files.readAllBytes(Paths.get(row.getValue(StringConsts.RELATIVE_FILE_PATH)));
                String fileStr = StringUtils.newStringUtf8(org.apache.commons.codec.binary.Base64.encodeBase64(encoded));
                fileMeta.setFileContent(fileStr);
                fileMeta.setName(attachmentFile.getName());
            } catch (IOException e) {
                throw new RestApiException("Unable to set fileContent on insert for: " + row.getValue(StringConsts.RELATIVE_FILE_PATH));
            }
        } else {
            // for update, grab original fileContent because it is required for an update
            FileContent fileContent = restApi.getFileContent((Class<F>) entityInfo.getEntityClass(), bullhornParentId, fileMeta.getId());
            fileMeta.setFileContent(fileContent.getFileContent());
        }

        // set values from FileParams
        Map<String, String> paramsMap = ParamFactory.fileParams().getParameterMap();
        for (String field : paramsMap.keySet()) {
            populateFieldOnEntity(field, paramsMap.get(field), fileMeta, methodMap);
        }

        // set values from csv file
        for (String field : row.getNames()) {
            if (validField(field)) {
                populateFieldOnEntity(field, row.getValue(field), fileMeta, methodMap);
            }
        }

        // external id cannot be null
        if (fileMeta.getExternalID() == null) {
            fileMeta.setExternalID(attachmentFile.getName());
        }
    }

    private <F extends FileEntity> Result addOrUpdateFile() {
        if (isNewEntity) {
            FileWrapper fileWrapper = restApi.addFile((Class<F>) entityInfo.getEntityClass(), bullhornParentId, fileMeta);
            return Result.insert(fileWrapper.getId());
        } else {
            FileWrapper fileWrapper = restApi.updateFile((Class<F>) entityInfo.getEntityClass(), bullhornParentId, fileMeta);
            return Result.update(fileWrapper.getId());
        }
    }

    private boolean validField(String field) {
        return !(field.contains(".")
            || field.equals(StringConsts.PARENT_ENTITY_ID)
            || field.equals(StringConsts.RELATIVE_FILE_PATH)
            || field.equals(StringConsts.IS_RESUME));
    }
}

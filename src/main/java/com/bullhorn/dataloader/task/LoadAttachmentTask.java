package com.bullhorn.dataloader.task;

import com.bullhorn.dataloader.meta.EntityInfo;
import com.bullhorn.dataloader.service.Command;
import com.bullhorn.dataloader.service.csv.CsvFileWriter;
import com.bullhorn.dataloader.service.csv.Result;
import com.bullhorn.dataloader.util.ActionTotals;
import com.bullhorn.dataloader.util.PrintUtil;
import com.bullhorn.dataloader.util.PropertyFileUtil;
import com.bullhorn.dataloader.util.StringConsts;
import com.bullhornsdk.data.api.BullhornData;
import com.bullhornsdk.data.exception.RestApiException;
import com.bullhornsdk.data.model.entity.association.EntityAssociations;
import com.bullhornsdk.data.model.entity.core.type.AssociationEntity;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Responsible for attaching a single row from a CSV input file.
 */
public class LoadAttachmentTask<A extends AssociationEntity, E extends EntityAssociations, B extends BullhornEntity> extends AbstractTask<A, E, B> {

    private FileMeta fileMeta;
    private boolean isNewEntity = true;
    private Map<String, Method> methodMap;

    public LoadAttachmentTask(Command command,
                              Integer rowNumber,
                              EntityInfo entityInfo,
                              LinkedHashMap<String, String> dataMap,
                              Map<String, Method> methodMap,
                              CsvFileWriter csvWriter,
                              PropertyFileUtil propertyFileUtil,
                              BullhornData bullhornData,
                              PrintUtil printUtil,
                              ActionTotals actionTotals) {
        super(command, rowNumber, entityInfo, dataMap, csvWriter, propertyFileUtil, bullhornData, printUtil, actionTotals);
        this.methodMap = methodMap;
    }

    @Override
    public void run() {
        init();
        Result result;
        try {
            result = handle();
        } catch (Exception e) {
            result = handleFailure(e);
        }
        writeToResultCSV(result);
    }

    private Result handle() throws Exception {
        Optional<List<String>> entityExistFields = propertyFileUtil.getEntityExistFields(entityClass.getSimpleName());
        if (!entityExistFields.isPresent()) {
            throw new IllegalArgumentException("Row " + rowNumber + ": Properties file is missing the '" +
                WordUtils.uncapitalize(entityClass.getSimpleName()) + "ExistField' property required to lookup " +
                "the parent entity.");
        }

        getAndSetBullhornID(entityExistFields.get());
        addParentEntityIDtoDataMap();
        createFileMeta();
        populateFileMeta();
        Result result = addOrUpdateFile();
        return result;
    }

    // attachments are keyed off of the <entity>ExistField property, NOT <entity>AttachmentExistField
    private <S extends SearchEntity> void getAndSetBullhornID(List<String> properties) throws Exception {
        if (properties.contains(getEntityAssociatedPropertyName(StringConsts.ID))) {
             bullhornParentId = Integer.parseInt(dataMap.get(getEntityAssociatedPropertyName(StringConsts.ID)));
        } else {
            List<String> propertiesWithValues = Lists.newArrayList();
            for (String property : properties) {
                String propertyValue = dataMap.get(getEntityAssociatedPropertyName(property));
                Class fieldType = getFieldType(entityClass, WordUtils.uncapitalize(entityClass.getSimpleName()) + "ExistField", property);
                propertiesWithValues.add(getQueryStatement(property, propertyValue, fieldType, entityClass));
            }
            String query = Joiner.on(" AND ").join(propertiesWithValues);
            List<S> searchList = bullhornData.search((Class<S>) entityClass, query, Sets.newHashSet("id"), ParamFactory.searchParams()).getData();
            if (!searchList.isEmpty()) {
                bullhornParentId = searchList.get(0).getId();
            } else {
                throw new RestApiException("Row " + rowNumber + ": Parent Entity not found.");
            }
        }
    }

    private String getEntityAssociatedPropertyName(String property) {
        return getCamelCasedClassToString() + "." + property;
    }

    private <F extends FileEntity> void createFileMeta() {
        fileMeta = new StandardFileMeta();

        List<FileMeta> allFileMetas = bullhornData.getFileMetaData((Class<F>) entityClass, bullhornParentId);

        for (FileMeta curFileMeta : allFileMetas) {
            if (curFileMeta.getId().toString().equalsIgnoreCase(dataMap.get(StringConsts.ID))
                || curFileMeta.getExternalID().equalsIgnoreCase(dataMap.get(StringConsts.EXTERNAL_ID))
                ) {
                isNewEntity = false;
                fileMeta = curFileMeta;
                break;
            }
        }
    }

    private <F extends FileEntity> void populateFileMeta() throws Exception {
        File attachmentFile = new File(dataMap.get(StringConsts.RELATIVE_FILE_PATH));

        if (isNewEntity) {
            try {
                byte[] encoded = Files.readAllBytes(Paths.get(dataMap.get(StringConsts.RELATIVE_FILE_PATH)));
                String fileStr = StringUtils.newStringUtf8(org.apache.commons.codec.binary.Base64.encodeBase64(encoded));
                fileMeta.setFileContent(fileStr);
                fileMeta.setName(attachmentFile.getName());
            } catch (IOException e) {
                throw new RestApiException("Row " + rowNumber + ": Unable to set fileContent on insert for: " + dataMap.get(StringConsts.RELATIVE_FILE_PATH));
            }
        } else {
            // for update, grab original fileContent because it is required for an update
            FileContent fileContent = bullhornData.getFileContent((Class<F>) entityClass, bullhornParentId, fileMeta.getId());
            fileMeta.setFileContent(fileContent.getFileContent());
        }

        // set values from FileParams
        Map<String, String> paramsMap = ParamFactory.fileParams().getParameterMap();
        for (String field : paramsMap.keySet()) {
            populateFieldOnEntity(field, paramsMap.get(field), fileMeta, methodMap);
        }

        // set values from csv file
        for (String field : dataMap.keySet()) {
            if (validField(field)) {
                populateFieldOnEntity(field, dataMap.get(field), fileMeta, methodMap);
            }
        }

        // external id cannot be null
        if (fileMeta.getExternalID() == null) {
            fileMeta.setExternalID(attachmentFile.getName());
        }
    }

    private <F extends FileEntity> Result addOrUpdateFile() {
        if (isNewEntity) {
            FileWrapper fileWrapper = bullhornData.addFile((Class<F>) entityClass, bullhornParentId, fileMeta);
            return Result.Insert(fileWrapper.getId());
        } else {
            FileWrapper fileWrapper = bullhornData.updateFile((Class<F>) entityClass, bullhornParentId, fileMeta);
            return Result.Update(fileWrapper.getId());
        }
    }

    private boolean validField(String field) {
        return !(field.contains(".")
            || field.equals(StringConsts.PARENT_ENTITY_ID)
            || field.equals(StringConsts.RELATIVE_FILE_PATH)
            || field.equals(StringConsts.IS_RESUME));
    }

}

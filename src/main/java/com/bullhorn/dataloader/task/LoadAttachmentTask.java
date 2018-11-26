package com.bullhorn.dataloader.task;

import com.bullhorn.dataloader.data.ActionTotals;
import com.bullhorn.dataloader.data.Cell;
import com.bullhorn.dataloader.data.CsvFileWriter;
import com.bullhorn.dataloader.data.Result;
import com.bullhorn.dataloader.data.Row;
import com.bullhorn.dataloader.enums.EntityInfo;
import com.bullhorn.dataloader.rest.CompleteUtil;
import com.bullhorn.dataloader.rest.Field;
import com.bullhorn.dataloader.rest.Record;
import com.bullhorn.dataloader.rest.RestApi;
import com.bullhorn.dataloader.util.FindUtil;
import com.bullhorn.dataloader.util.PrintUtil;
import com.bullhorn.dataloader.util.PropertyFileUtil;
import com.bullhorn.dataloader.util.StringConsts;
import com.bullhornsdk.data.exception.RestApiException;
import com.bullhornsdk.data.model.entity.core.type.BullhornEntity;
import com.bullhornsdk.data.model.entity.core.type.FileEntity;
import com.bullhornsdk.data.model.file.FileMeta;
import com.bullhornsdk.data.model.file.standard.StandardFileMeta;
import com.bullhornsdk.data.model.parameter.standard.ParamFactory;
import com.bullhornsdk.data.model.response.file.FileContent;
import com.bullhornsdk.data.model.response.file.FileWrapper;
import com.google.common.collect.Sets;
import org.apache.commons.codec.binary.StringUtils;
import org.apache.commons.lang.WordUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Responsible for attaching a single row from a CSV input file.
 */
public class LoadAttachmentTask extends AbstractTask {

    public LoadAttachmentTask(EntityInfo entityInfo,
                              Row row,
                              CsvFileWriter csvFileWriter,
                              PropertyFileUtil propertyFileUtil,
                              RestApi restApi,
                              PrintUtil printUtil,
                              ActionTotals actionTotals,
                              CompleteUtil completeUtil) {
        super(entityInfo, row, csvFileWriter, propertyFileUtil, restApi, printUtil, actionTotals, completeUtil);
    }

    @SuppressWarnings("unchecked")
    protected Result handle() throws Exception {
        // Verify that the parent entity exist field is set, for example: candidateExistField
        // In the column header, the parent entity exist field needs to be: "parentEntityName.field", for example: "candidate.externalID"
        List<String> parentEntityExistFieldNames = propertyFileUtil.getEntityExistFields(entityInfo);
        if (parentEntityExistFieldNames.isEmpty()) {
            throw new IllegalArgumentException("Properties file is missing the '"
                + WordUtils.uncapitalize(entityInfo.getEntityName()) + StringConsts.EXIST_FIELD_SUFFIX
                + "' property required to lookup the parent entity.");
        }

        // Verify that the parent entity exists and is active in REST
        List<Field> parentEntityExistFields = parentEntityExistFieldNames.stream().map(fieldName -> new Field(entityInfo,
            new Cell(fieldName, row.getValue(WordUtils.uncapitalize(entityInfo.getEntityName()) + "." + fieldName)),
            true, propertyFileUtil.getDateParser())).collect(Collectors.toList());
        List<BullhornEntity> foundParentEntityList = findActiveEntities(parentEntityExistFields, Sets.newHashSet(StringConsts.ID), false);
        if (foundParentEntityList.isEmpty()) {
            throw new RestApiException("Parent Entity not found.");
        } else if (foundParentEntityList.size() > 1) {
            throw new RestApiException(FindUtil.getMultipleRecordsExistMessage(entityInfo, parentEntityExistFields, foundParentEntityList.size()));
        } else {
            entityId = foundParentEntityList.get(0).getId();
        }

        // Get all file metas for files on the parent entity, and perform exist check on id and externalID
        boolean isNewEntity = true;
        FileMeta fileMeta = new StandardFileMeta();
        List<FileMeta> existingFileMetaList = restApi.getFileMetaData((Class<FileEntity>) entityInfo.getEntityClass(), entityId);
        List<FileMeta> matchingFileMeta = existingFileMetaList.stream()
            .filter(fm -> fm.getId().toString().equalsIgnoreCase(row.getValue(StringConsts.ID))
                || fm.getExternalID().equalsIgnoreCase(row.getValue(StringConsts.EXTERNAL_ID))).collect(Collectors.toList());
        if (!matchingFileMeta.isEmpty()) {
            fileMeta = matchingFileMeta.get(0);
            isNewEntity = false;
        }

        // Create row made up of cell values that apply to the file meta, starting with the standard file parameters
        Row fileRow = new Row(row.getNumber());
        Map<String, String> parameterMap = ParamFactory.fileParams().getParameterMap();
        for (String parameterName : parameterMap.keySet()) {
            fileRow.addCell(new Cell(parameterName, parameterMap.get(parameterName)));
        }

        // Read the file from disk if this is a new file upload or an update to an existing file that updates the file content
        if (isNewEntity || row.hasValue(StringConsts.RELATIVE_FILE_PATH)) {
            File attachmentFile;
            try {
                attachmentFile = new File(row.getValue(StringConsts.RELATIVE_FILE_PATH));
            } catch (NullPointerException e) {
                throw new IOException("Missing the '" + StringConsts.RELATIVE_FILE_PATH + "' column required for loadAttachments");
            }

            // If the externalID is not already being set, set it to the file name
            if (!fileRow.hasValue(StringConsts.EXTERNAL_ID)) {
                fileRow.addCell(new Cell(StringConsts.EXTERNAL_ID, attachmentFile.getName()));
            }

            try {
                byte[] encoded = Files.readAllBytes(Paths.get(row.getValue(StringConsts.RELATIVE_FILE_PATH)));
                String fileStr = StringUtils.newStringUtf8(org.apache.commons.codec.binary.Base64.encodeBase64(encoded));
                fileMeta.setFileContent(fileStr);
                fileMeta.setName(attachmentFile.getName());
            } catch (IOException e) {
                throw new RestApiException("Cannot read file from disk: " + row.getValue(StringConsts.RELATIVE_FILE_PATH));
            }
        } else {
            // When updating meta but not the file content, grab original fileContent because it is required for an update
            FileContent fileContent = restApi.getFileContent((Class<FileEntity>) entityInfo.getEntityClass(), entityId, fileMeta.getId());
            fileMeta.setFileContent(fileContent.getFileContent());
        }

        // Add file cell values that pertain directly to the file meta, not one of the predefined fields
        for (Cell cell : row.getCells()) {
            List<String> nonFileFields = Arrays.asList(StringConsts.PARENT_ENTITY_ID, StringConsts.RELATIVE_FILE_PATH, StringConsts.IS_RESUME);
            if (!cell.isAssociation() && !nonFileFields.contains(cell.getName())) {
                fileRow.addCell(cell);
            }
        }

        // Populate the file meta
        Record fileRecord = new Record(EntityInfo.FILE, fileRow, propertyFileUtil);
        for (Field field : fileRecord.getFields()) {
            field.populateFieldOnEntity(fileMeta);
        }

        if (isNewEntity) {
            FileWrapper fileWrapper = restApi.addFile((Class<FileEntity>) entityInfo.getEntityClass(), entityId, fileMeta);
            return Result.insert(fileWrapper.getId(), entityId);
        } else {
            FileWrapper fileWrapper = restApi.updateFile((Class<FileEntity>) entityInfo.getEntityClass(), entityId, fileMeta);
            return Result.update(fileWrapper.getId(), entityId);
        }
    }
}

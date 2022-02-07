package com.bullhorn.dataloader.task;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.codec.binary.StringUtils;
import org.apache.commons.lang.WordUtils;

import com.bullhorn.dataloader.data.ActionTotals;
import com.bullhorn.dataloader.data.Cell;
import com.bullhorn.dataloader.data.CsvFileWriter;
import com.bullhorn.dataloader.data.Result;
import com.bullhorn.dataloader.data.Row;
import com.bullhorn.dataloader.enums.EntityInfo;
import com.bullhorn.dataloader.enums.ErrorInfo;
import com.bullhorn.dataloader.rest.Cache;
import com.bullhorn.dataloader.rest.CompleteUtil;
import com.bullhorn.dataloader.rest.Field;
import com.bullhorn.dataloader.rest.Record;
import com.bullhorn.dataloader.rest.RestApi;
import com.bullhorn.dataloader.util.DataLoaderException;
import com.bullhorn.dataloader.util.FileUtil;
import com.bullhorn.dataloader.util.FindUtil;
import com.bullhorn.dataloader.util.PrintUtil;
import com.bullhorn.dataloader.util.PropertyFileUtil;
import com.bullhorn.dataloader.util.StringConsts;
import com.bullhornsdk.data.model.entity.core.type.BullhornEntity;
import com.bullhornsdk.data.model.entity.core.type.FileEntity;
import com.bullhornsdk.data.model.file.FileMeta;
import com.bullhornsdk.data.model.file.standard.StandardFileMeta;
import com.bullhornsdk.data.model.parameter.standard.ParamFactory;
import com.bullhornsdk.data.model.response.file.FileContent;
import com.bullhornsdk.data.model.response.file.FileWrapper;
import com.google.common.collect.Sets;

/**
 * Responsible for attaching a single row from a CSV input file.
 */
public class LoadAttachmentTask extends AbstractTask {

    LoadAttachmentTask(EntityInfo entityInfo,
                       Row row,
                       CsvFileWriter csvFileWriter,
                       PropertyFileUtil propertyFileUtil,
                       RestApi restApi,
                       PrintUtil printUtil,
                       ActionTotals actionTotals,
                       Cache cache, CompleteUtil completeUtil) {
        super(entityInfo, row, csvFileWriter, propertyFileUtil, restApi, printUtil, actionTotals, cache, completeUtil);
    }

    @SuppressWarnings("unchecked")
    protected Result handle() throws Exception {
        // Verify that the parent entity exist field is set, for example: candidateExistField
        // In the column header, the parent entity exist field needs to be: "parentEntityName.field", for example: "candidate.externalID"
        List<String> parentEntityExistFieldNames = propertyFileUtil.getEntityExistFields(entityInfo);
        if (parentEntityExistFieldNames.isEmpty()) {
            throw new DataLoaderException(ErrorInfo.MISSING_SETTING, "Properties file is missing the '"
                + WordUtils.uncapitalize(entityInfo.getEntityName()) + StringConsts.EXIST_FIELD_SUFFIX
                + "' property required to lookup the parent entity.");
        }

        // Verify that the parent entity exists and is active in REST
        List<Field> parentEntityExistFields = parentEntityExistFieldNames.stream().map(fieldName -> new Field(entityInfo,
            new Cell(fieldName, row.getValue(WordUtils.uncapitalize(entityInfo.getEntityName()) + "." + fieldName)),
            true, propertyFileUtil.getDateParser())).collect(Collectors.toList());
        List<BullhornEntity> foundParentEntityList = findActiveEntities(parentEntityExistFields, Sets.newHashSet(StringConsts.ID), false);
        if (foundParentEntityList.isEmpty()) {
            throw new DataLoaderException(ErrorInfo.MISSING_PARENT_ENTITY, "Parent Entity not found.");
        } else if (foundParentEntityList.size() > 1) {
            throw new DataLoaderException(ErrorInfo.DUPLICATE_RECORDS,
                FindUtil.getMultipleRecordsExistMessage(entityInfo, parentEntityExistFields, foundParentEntityList));
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
        Row fileRow = new Row(row.getFilePath(), row.getNumber());
        Map<String, String> parameterMap = ParamFactory.fileParams().getParameterMap();
        for (String parameterName : parameterMap.keySet()) {
            fileRow.addCell(new Cell(parameterName, parameterMap.get(parameterName)));
        }

        // Read the file from disk if this is a new file upload or an update to an existing file that updates the file content
        if (isNewEntity || row.hasValue(StringConsts.RELATIVE_FILE_PATH)) {
            File attachmentFile = FileUtil.getAttachmentFile(row);

            // If the externalID is not already being set, set it to the file name
            if (!fileRow.hasValue(StringConsts.EXTERNAL_ID)) {
                fileRow.addCell(new Cell(StringConsts.EXTERNAL_ID, attachmentFile.getName()));
            }

            try {
                byte[] encodedFileContent = Files.readAllBytes(attachmentFile.toPath());
                String fileContent = StringUtils.newStringUtf8(org.apache.commons.codec.binary.Base64.encodeBase64(encodedFileContent));
                fileMeta.setFileContent(fileContent);
                fileMeta.setName(attachmentFile.getName());
            } catch (IOException e) {
                throw new DataLoaderException(ErrorInfo.MISSING_ATTACHMENT_FILE,
                    "Cannot read file from disk: " + row.getValue(StringConsts.RELATIVE_FILE_PATH));
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

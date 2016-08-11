package com.bullhorn.dataloader.task;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.binary.StringUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;

import com.bullhorn.dataloader.consts.TaskConsts;
import com.bullhorn.dataloader.service.Command;
import com.bullhorn.dataloader.service.csv.CsvFileWriter;
import com.bullhorn.dataloader.service.csv.Result;
import com.bullhorn.dataloader.util.ActionTotals;
import com.bullhorn.dataloader.util.PrintUtil;
import com.bullhorn.dataloader.util.PropertyFileUtil;
import com.bullhornsdk.data.api.BullhornData;
import com.bullhornsdk.data.exception.RestApiException;
import com.bullhornsdk.data.model.entity.core.type.BullhornEntity;
import com.bullhornsdk.data.model.entity.core.type.FileEntity;
import com.bullhornsdk.data.model.entity.core.type.SearchEntity;
import com.bullhornsdk.data.model.file.FileMeta;
import com.bullhornsdk.data.model.parameter.standard.ParamFactory;
import com.bullhornsdk.data.model.response.file.FileWrapper;
import com.bullhornsdk.data.model.file.standard.StandardFileMeta;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * Responsible for attaching a single row from a CSV input file.
 */
public class LoadAttachmentTask <B extends BullhornEntity> extends AbstractTask<B> {

    private FileMeta fileMeta;
    private File attachmentFile;
    private boolean isNewEntity = true;
    private Map<String, Method> methodMap;

    public LoadAttachmentTask(Command command,
                              Integer rowNumber,
                              Class<B> entity,
                              LinkedHashMap<String, String> dataMap,
                              Map<String, Method> methodMap,
                              CsvFileWriter csvWriter,
                              PropertyFileUtil propertyFileUtil,
                              BullhornData bullhornData,
                              PrintUtil printUtil,
                              ActionTotals actionTotals) {
        super(command, rowNumber, entity, dataMap, csvWriter, propertyFileUtil, bullhornData, printUtil, actionTotals);
        this.methodMap = methodMap;
    }

    @Override
    public void run() {
        Result result;
        try {
            result = handle();
        } catch(Exception e){
            result = handleFailure(e);
        }
        writeToResultCSV(result);
    }

    private Result handle() throws Exception {
        getAndSetBullhornID((propertyFileUtil.getEntityExistFields(entityClass.getSimpleName())).get());
        addParentEntityIDtoDataMap();
        createFileMeta();
        populateFileMeta();
        Result result = addOrUpdateFile();
        return result;
    }

    private <S extends SearchEntity> void getAndSetBullhornID(List<String> properties) throws Exception {
        if (properties.contains(getEntityAssociatedPropertyName(TaskConsts.ID))){
            bullhornParentId = Integer.parseInt(dataMap.get(getEntityAssociatedPropertyName(TaskConsts.ID)));
        } else {
            List<String> propertiesWithValues = Lists.newArrayList();
            for (String property : properties) {
                String propertyValue = dataMap.get(getEntityAssociatedPropertyName(property));
                propertiesWithValues.add(getQueryStatement(property, propertyValue, getFieldType(entityClass, property)));
            }
            String query = Joiner.on(" AND ").join(propertiesWithValues);
            List<S> searchList = bullhornData.search((Class<S>) entityClass, query, Sets.newHashSet("id"), ParamFactory.searchParams()).getData();
            if (!searchList.isEmpty()) {
                bullhornParentId = searchList.get(0).getId();
            } else {
                throw new Exception("Parent Entity not found.");
            }
        }
    }

    private String getEntityAssociatedPropertyName(String property) {
        return entityClass.getSimpleName() + "." + property;
    }

    private <F extends FileEntity> void createFileMeta() {
        attachmentFile = new File(dataMap.get(TaskConsts.RELATIVE_FILE_PATH));
        fileMeta = new StandardFileMeta();

        List<FileMeta> allFileMetas = bullhornData.getFileMetaData((Class<F>) entityClass, bullhornParentId);

        for (FileMeta curFileMeta : allFileMetas) {
            if (curFileMeta.getExternalID().equalsIgnoreCase(dataMap.get(TaskConsts.EXTERNAL_ID))) {
                try {
                    isNewEntity = false;
                    fileMeta = curFileMeta;

                    // fileContent is required for an update
                    byte[] encoded = Files.readAllBytes(Paths.get(dataMap.get(TaskConsts.RELATIVE_FILE_PATH)));
                    String fileStr  = StringUtils.newStringUtf8(org.apache.commons.codec.binary.Base64.encodeBase64(encoded));
                    fileMeta.setFileContent(fileStr);

                    break;
                }
                catch (IOException ioe) {
                    // if setting fileContent fails, do insert instead
                    return;
                }
            }
        }
    }

    private void populateFileMeta() {
        // set values from FileParams
        Map<String, String> paramsMap = ParamFactory.fileParams().getParameterMap();
        for (String field : paramsMap.keySet()){
            populateFieldOnEntity(field, paramsMap.get(field), fileMeta, methodMap);
        }

        // set values from csv file
        for (String field : dataMap.keySet()){
            populateFieldOnEntity(field, dataMap.get(field), fileMeta, methodMap);
        }

        // external id cannot be null
        if (fileMeta.getExternalID() == null) {
            fileMeta.setExternalID(attachmentFile.getName());
        }
    }

    private  <F extends FileEntity> Result addOrUpdateFile() {
        if (isNewEntity) {
            FileWrapper fileWrapper =  bullhornData.addFile((Class<F>) entityClass, bullhornParentId, attachmentFile, fileMeta, false);
            return Result.Insert(fileWrapper.getId());
        }
        else {
            FileWrapper fileWrapper = bullhornData.updateFile((Class<F>) entityClass, bullhornParentId, fileMeta);
            return Result.Update(fileWrapper.getId());
        }
    }

}

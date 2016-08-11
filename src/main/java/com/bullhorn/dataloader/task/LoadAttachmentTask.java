package com.bullhorn.dataloader.task;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

    public static final String ATTACHMENT_EXTERNAL_ID_COLUMN = "attachmentExternalID";

    private FileMeta fileMeta;
    private File attachmentFile;
    private boolean isNewEntity = true;

    public LoadAttachmentTask(Command command,
                              Integer rowNumber,
                              Class<B> entity,
                              LinkedHashMap<String, String> dataMap,
                              CsvFileWriter csvWriter,
                              PropertyFileUtil propertyFileUtil,
                              BullhornData bullhornData,
                              PrintUtil printUtil,
                              ActionTotals actionTotals) {
        super(command, rowNumber, entity, dataMap, csvWriter, propertyFileUtil, bullhornData, printUtil, actionTotals);
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
        FileWrapper fileWrapper = addOrUpdateFile();
        return Result.Insert(fileWrapper.getId());
    }

    private <S extends SearchEntity> void getAndSetBullhornID(List<String> properties) throws Exception {
        if (properties.contains("id")){
            bullhornParentId = Integer.parseInt(dataMap.get("id"));
        } else {
            List<String> propertiesWithValues = Lists.newArrayList();
            for (String property : properties) {
                propertiesWithValues.add(getQueryStatement(property, dataMap.get(property), getFieldType(entityClass, property)));
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

    private <F extends FileEntity> void createFileMeta() {
        attachmentFile = new File(dataMap.get(TaskConsts.RELATIVE_FILE_PATH));
        fileMeta = new StandardFileMeta();

        List<FileMeta> allFileMetas = bullhornData.getFileMetaData((Class<F>) entityClass, bullhornParentId);

        for (FileMeta curFileMeta : allFileMetas) {
            if (curFileMeta.getExternalID().equalsIgnoreCase(dataMap.get(ATTACHMENT_EXTERNAL_ID_COLUMN))) {
                try {
                    isNewEntity = false;
                    fileMeta = curFileMeta;

                    byte[] encoded = Files.readAllBytes(Paths.get(dataMap.get(TaskConsts.RELATIVE_FILE_PATH)));
                    fileMeta.setFileContent(new String(encoded, Charset.defaultCharset()));

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
        Map<String, Method> methodMap = createMethodMap();

        // set values from FileParams
        Map<String, String> paramsMap = ParamFactory.fileParams().getParameterMap();
        for (String field : paramsMap.keySet()){
            populateFieldOnEntity(field, paramsMap.get(field), methodMap);
        }

        // set values from csv file
        for (String field : dataMap.keySet()){
            if(TaskConsts.ID.equalsIgnoreCase(field)   // id is for the parent entity
                || TaskConsts.EXTERNAL_ID.equalsIgnoreCase(field) // externalId from datamap is for the parent entity
                    ) {
                continue;
            }

            if (ATTACHMENT_EXTERNAL_ID_COLUMN.equalsIgnoreCase(field)) {
                populateFieldOnEntity(TaskConsts.EXTERNAL_ID, dataMap.get(field), methodMap);
            }
            else {
                populateFieldOnEntity(field, dataMap.get(field), methodMap);
            }
        }

        // external id cannot be null
        if (fileMeta.getExternalID() == null) {
            fileMeta.setExternalID(attachmentFile.getName());
        }
    }

    private <F extends FileEntity> FileWrapper addOrUpdateFile() {
        if (isNewEntity)
            return bullhornData.addFile((Class<F>) entityClass, bullhornParentId, attachmentFile, fileMeta, false);
        else
            return bullhornData.updateFile((Class<F>) entityClass, bullhornParentId, fileMeta);
    }

    private void populateFieldOnEntity(String field, String value, Map<String, Method> methodMap) {
        try {
            Method method = methodMap.get(field.toLowerCase());
            if (method != null && value != null && !"".equalsIgnoreCase(value)){
                method.invoke(fileMeta, convertStringToClass(method, value));
            }
        } catch (Exception e) {
            printUtil.printAndLog("Error populating " + field);
            printUtil.printAndLog(e.toString());
        }
    }

    private Map<String, Method> createMethodMap() {
        Map<String, Method> methodMap = new HashMap();
        for (Method method : Arrays.asList(StandardFileMeta.class.getMethods())){
            if ("set".equalsIgnoreCase(method.getName().substring(0, 3))) {
                methodMap.put(method.getName().substring(3).toLowerCase(), method);
            }
        }
        return methodMap;
    }

}

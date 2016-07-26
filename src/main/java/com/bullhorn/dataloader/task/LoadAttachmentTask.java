package com.bullhorn.dataloader.task;

import com.bullhorn.dataloader.service.csv.CsvFileWriter;
import com.bullhorn.dataloader.service.csv.Result;
import com.bullhorn.dataloader.util.PropertyFileUtil;
import com.bullhornsdk.data.api.BullhornData;
import com.bullhornsdk.data.model.entity.core.type.BullhornEntity;
import com.bullhornsdk.data.model.entity.core.type.FileEntity;
import com.bullhornsdk.data.model.entity.core.type.SearchEntity;
import com.bullhornsdk.data.model.enums.BullhornEntityInfo;
import com.bullhornsdk.data.model.parameter.standard.ParamFactory;
import com.bullhornsdk.data.model.response.file.FileWrapper;
import com.google.common.collect.Sets;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Responsible for attaching a single row from a CSV input file.
 */
public class LoadAttachmentTask <B extends BullhornEntity> implements Runnable {
    private static final Logger log = LogManager.getLogger(LoadAttachmentTask.class);

    private String entityName;
    private Integer bullhornParentId;
    private String externalId;
    private String attachmentFilePath;
    private String[] data;
    private CsvFileWriter csvWriter;
    private PropertyFileUtil propertyFileUtil;
    private BullhornData bullhornData;
    private Class<B> entity;

    public LoadAttachmentTask(String entityName,
                              String[] data,
                              String externalId,
                              String attachmentFilePath,
                              CsvFileWriter csvWriter,
                              PropertyFileUtil propertyFileUtil,
                              BullhornData bullhornData) {
        this.entityName = entityName;
        this.data = data;
        this.externalId = externalId;
        this.attachmentFilePath = attachmentFilePath;
        this.csvWriter = csvWriter;
        this.propertyFileUtil = propertyFileUtil;
        this.bullhornData = bullhornData;
    }

    /**
     * Run method on this runnable object called by the thread manager.
     * <p>
     * The createOrGetEntity method performs the update/insert in REST.
     * The results of this are passed to the csvWriter
     */
    @Override
    public void run() {
        Result result;
        try {
            result = handleAttachment();
        } catch(Exception e){
            result = handleAttachmentFailure(e);
        }
        writeToAttachmentResultCSV(result);
    }

    private Result handleAttachment() {
        getAndSetBullhornEntityInfo();
        getAndSetBullhornID();
        FileWrapper fileWrapper = attachFile();
        return getSuccessResult(fileWrapper);
    }

    private Result handleAttachmentFailure(Exception e) {
        System.out.println(e);
        log.error(e);
        return getFailureResult(e);
    }

    private void writeToAttachmentResultCSV(Result result) {
        try {
            csvWriter.writeAttachmentRow(data, result);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Result getFailureResult(Exception e) {
        Result result = new Result(Result.Status.FAILURE, null, e.getMessage());
        return result;
    }

    private Result getSuccessResult(FileWrapper fileWrapper) {
        Result result = new Result(Result.Status.SUCCESS, fileWrapper.getId(), "");
        return result;
    }


    private <F extends FileEntity> FileWrapper attachFile() {
        File attachementFile = new File(attachmentFilePath);
        return bullhornData.addFile((Class<F>) entity, bullhornParentId, attachementFile, externalId, ParamFactory.fileParams());
    }

    private void getAndSetBullhornEntityInfo() {
        entity = BullhornEntityInfo.getTypeFromName(entityName).getType();
    }

    private <S extends SearchEntity> void getAndSetBullhornID() {
        String query = "externalId:" + externalId;
        List<S> searchList = bullhornData.search((Class<S>) entity, query, Sets.newHashSet("id"), ParamFactory.searchParams()).getData();
        if (!searchList.isEmpty()){
            bullhornParentId = searchList.get(0).getId();
        }
    }


}

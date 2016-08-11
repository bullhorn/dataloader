package com.bullhorn.dataloader.task;

import java.util.LinkedHashMap;

import com.bullhorn.dataloader.consts.TaskConsts;
import com.bullhorn.dataloader.service.Command;
import com.bullhorn.dataloader.service.csv.CsvFileWriter;
import com.bullhorn.dataloader.service.csv.Result;
import com.bullhorn.dataloader.util.ActionTotals;
import com.bullhorn.dataloader.util.PrintUtil;
import com.bullhorn.dataloader.util.PropertyFileUtil;
import com.bullhornsdk.data.api.BullhornData;
import com.bullhornsdk.data.model.entity.core.type.BullhornEntity;
import com.bullhornsdk.data.model.entity.core.type.FileEntity;
import com.bullhornsdk.data.model.file.FileApiResponse;

/**
 * Responsible for deleting a single row from a CSV input file.
 */
public class DeleteAttachmentTask<B extends BullhornEntity> extends AbstractTask<B> {

    public DeleteAttachmentTask(Command method,
                                Integer rowNumber,
                                Class<B> entity,
                                LinkedHashMap<String, String> dataMap,
                                CsvFileWriter csvWriter,
                                PropertyFileUtil propertyFileUtil,
                                BullhornData bullhornData,
                                PrintUtil printUtil,
                                ActionTotals actionTotals) {
        super(method, rowNumber, entity, dataMap, csvWriter, propertyFileUtil, bullhornData, printUtil, actionTotals);
    }

    /**
     * Run method on this runnable object called by the thread manager.
     */
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
        FileApiResponse fileApiResponse = deleteFile();
        return Result.Delete(fileApiResponse.getFileId());
    }

    private <F extends FileEntity> FileApiResponse deleteFile() {
        return bullhornData.deleteFile((Class<F>) entityClass, Integer.valueOf(dataMap.get(TaskConsts.PARENT_ENTITY_ID)), Integer.valueOf(dataMap.get(TaskConsts.ATTACHMENT_ID)));
    }

}

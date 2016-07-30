package com.bullhorn.dataloader.task;

import java.util.LinkedHashMap;

import com.bullhorn.dataloader.service.Command;
import com.bullhorn.dataloader.service.csv.CsvFileWriter;
import com.bullhorn.dataloader.service.csv.Result;
import com.bullhorn.dataloader.util.ActionTotals;
import com.bullhorn.dataloader.util.PrintUtil;
import com.bullhorn.dataloader.util.PropertyFileUtil;
import com.bullhornsdk.data.api.BullhornData;
import com.bullhornsdk.data.model.entity.core.type.BullhornEntity;
import com.bullhornsdk.data.model.entity.core.type.FileEntity;
import com.bullhornsdk.data.model.response.file.FileApiResponse;

/**
 * Responsible for deleting a single row from a CSV input file.
 */
public class DeleteAttachmentTask<B extends BullhornEntity> extends AbstractTask<B> {

    public DeleteAttachmentTask(Command command,
                                String entityName,
                                LinkedHashMap<String, String> dataMap,
                                CsvFileWriter csvWriter,
                                PropertyFileUtil propertyFileUtil,
                                BullhornData bullhornData,
                                PrintUtil printUtil,
                                ActionTotals actionTotals) {
        super(command, entityName, dataMap, csvWriter, propertyFileUtil, bullhornData, printUtil, actionTotals);
    }

    /**
     * Run method on this runnable object called by the thread manager.
     */
    @Override
    public void run() {
        Result result;
        try {
            result = handleAttachment();
        } catch(Exception e){
            result = handleAttachmentFailure(e);
        }
        writeToResultCSV(result);
    }

    private Result handleAttachment() throws Exception {
        getAndSetBullhornEntityInfo();
        FileApiResponse fileApiResponse = deleteFile();
        return Result.Delete(fileApiResponse.getFileId());
    }

    private Result handleAttachmentFailure(Exception e) {
        printUtil.printAndLog(e.toString());
        return Result.Failure(e.toString());
    }

    private <F extends FileEntity> FileApiResponse deleteFile() {
        return bullhornData.deleteFile((Class<F>) entity, Integer.valueOf(dataMap.get(parentEntityID)), Integer.valueOf(dataMap.get(id)));
    }

}

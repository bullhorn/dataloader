package com.bullhorn.dataloader.task;

import java.io.File;
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
import com.bullhornsdk.data.model.parameter.standard.ParamFactory;
import com.bullhornsdk.data.model.response.file.FileWrapper;

/**
 * Responsible for attaching a single row from a CSV input file.
 */
public class LoadAttachmentTask <B extends BullhornEntity> extends AbstractTask<B> {

    public LoadAttachmentTask(Command command,
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
        getAndSetBullhornID();
        addParentEntityIDtoDataMap();
        FileWrapper fileWrapper = attachFile();
        return Result.Insert(fileWrapper.getId());
    }

    private Result handleAttachmentFailure(Exception e) {
        printUtil.printAndLog(e.toString());
        return Result.Failure(e.toString());
    }

    private <F extends FileEntity> FileWrapper attachFile() {
        File attachementFile = new File(dataMap.get(relativeFilePath));
        return bullhornData.addFile((Class<F>) entity, bullhornParentId, attachementFile, dataMap.get(externalID), ParamFactory.fileParams(), false);
    }

}

package com.bullhorn.dataloader.task;

import com.bullhorn.dataloader.data.ActionTotals;
import com.bullhorn.dataloader.data.CsvFileWriter;
import com.bullhorn.dataloader.data.Result;
import com.bullhorn.dataloader.data.Row;
import com.bullhorn.dataloader.enums.EntityInfo;
import com.bullhorn.dataloader.rest.RestApi;
import com.bullhorn.dataloader.util.PrintUtil;
import com.bullhorn.dataloader.util.PropertyFileUtil;
import com.bullhorn.dataloader.util.StringConsts;
import com.bullhornsdk.data.model.entity.association.EntityAssociations;
import com.bullhornsdk.data.model.entity.core.type.AssociationEntity;
import com.bullhornsdk.data.model.entity.core.type.BullhornEntity;
import com.bullhornsdk.data.model.entity.core.type.FileEntity;
import com.bullhornsdk.data.model.response.file.FileApiResponse;

import java.io.IOException;

/**
 * Responsible for deleting a single row from a CSV input file.
 */
public class DeleteAttachmentTask<A extends AssociationEntity, E extends EntityAssociations, B extends BullhornEntity> extends AbstractTask<A, E, B> {

    public DeleteAttachmentTask(EntityInfo entityInfo,
                                Row row,
                                CsvFileWriter csvFileWriter,
                                PropertyFileUtil propertyFileUtil,
                                RestApi restApi,
                                PrintUtil printUtil,
                                ActionTotals actionTotals) {
        super(entityInfo, row, csvFileWriter, propertyFileUtil, restApi, printUtil, actionTotals);
    }

    /**
     * Run method on this runnable object called by the thread manager.
     */
    @Override
    public void run() {
        Result result;
        try {
            result = handle();
        } catch (Exception e) {
            result = handleFailure(e);
        }
        writeToResultCSV(result);
    }

    private Result handle() throws Exception {
        FileApiResponse fileApiResponse = deleteFile();
        return Result.Delete(fileApiResponse.getFileId());
    }

    private <F extends FileEntity> FileApiResponse deleteFile() throws IOException {
        if (!dataMap.containsKey(StringConsts.PARENT_ENTITY_ID) || dataMap.get(StringConsts.PARENT_ENTITY_ID).isEmpty()) {
            throw new IOException("Row " + row.getNumber() + ": Missing the '" + StringConsts.PARENT_ENTITY_ID + "' column required for deleteAttachments");
        }
        if (!dataMap.containsKey(StringConsts.ID) || dataMap.get(StringConsts.ID).isEmpty()) {
            throw new IOException("Row " + row.getNumber() + ": Missing the '" + StringConsts.ID + "' column required for deleteAttachments");
        }
        return restApi.deleteFile((Class<F>) entityInfo.getEntityClass(), Integer.valueOf(dataMap.get(StringConsts.PARENT_ENTITY_ID)), Integer.valueOf(dataMap.get(StringConsts.ID)));
    }
}

package com.bullhorn.dataloader.task;

import java.io.IOException;

import com.bullhorn.dataloader.data.ActionTotals;
import com.bullhorn.dataloader.data.CsvFileWriter;
import com.bullhorn.dataloader.data.Result;
import com.bullhorn.dataloader.data.Row;
import com.bullhorn.dataloader.enums.EntityInfo;
import com.bullhorn.dataloader.rest.Cache;
import com.bullhorn.dataloader.rest.CompleteUtil;
import com.bullhorn.dataloader.rest.RestApi;
import com.bullhorn.dataloader.util.PrintUtil;
import com.bullhorn.dataloader.util.PropertyFileUtil;
import com.bullhorn.dataloader.util.StringConsts;
import com.bullhornsdk.data.model.entity.core.type.FileEntity;
import com.bullhornsdk.data.model.response.file.FileApiResponse;

/**
 * Responsible for deleting a single row from a CSV input file.
 */
public class DeleteAttachmentTask extends AbstractTask {

    DeleteAttachmentTask(EntityInfo entityInfo,
                         Row row,
                         CsvFileWriter csvFileWriter,
                         PropertyFileUtil propertyFileUtil,
                         RestApi restApi,
                         PrintUtil printUtil,
                         ActionTotals actionTotals,
                         Cache cache,
                         CompleteUtil completeUtil) {
        super(entityInfo, row, csvFileWriter, propertyFileUtil, restApi, printUtil, actionTotals, cache, completeUtil);
    }

    protected Result handle() throws Exception {
        FileApiResponse fileApiResponse = deleteFile();
        return Result.delete(fileApiResponse.getFileId());
    }

    @SuppressWarnings("unchecked")
    private <F extends FileEntity> FileApiResponse deleteFile() throws IOException {
        if (!row.hasValue(StringConsts.PARENT_ENTITY_ID) || row.getValue(StringConsts.PARENT_ENTITY_ID).isEmpty()) {
            throw new IOException("Missing the '" + StringConsts.PARENT_ENTITY_ID + "' column required for deleteAttachments");
        }
        if (!row.hasValue(StringConsts.ID) || row.getValue(StringConsts.ID).isEmpty()) {
            throw new IOException("Missing the '" + StringConsts.ID + "' column required for deleteAttachments");
        }
        return restApi.deleteFile((Class<F>) entityInfo.getEntityClass(),
            Integer.valueOf(row.getValue(StringConsts.PARENT_ENTITY_ID)),
            Integer.valueOf(row.getValue(StringConsts.ID)));
    }
}

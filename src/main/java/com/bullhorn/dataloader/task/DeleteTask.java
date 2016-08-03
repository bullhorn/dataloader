package com.bullhorn.dataloader.task;

import com.bullhorn.dataloader.service.Command;
import com.bullhorn.dataloader.service.csv.CsvFileWriter;
import com.bullhorn.dataloader.service.csv.Result;
import com.bullhorn.dataloader.util.ActionTotals;
import com.bullhorn.dataloader.util.PrintUtil;
import com.bullhorn.dataloader.util.PropertyFileUtil;
import com.bullhornsdk.data.api.BullhornData;
import com.bullhornsdk.data.model.entity.core.type.BullhornEntity;
import com.bullhornsdk.data.model.entity.core.type.DeleteEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.LinkedHashMap;

/**
 * Responsible for deleting a single row from a CSV input file.
 */
public class DeleteTask<B extends BullhornEntity> extends AbstractTask<B> {
    private static final Logger log = LogManager.getLogger(DeleteTask.class);
    private Integer entityID;

    public DeleteTask(Command command,
                      Integer rowNumber,
                      Class<B> entityClass,
                      LinkedHashMap<String, String> dataMap,
                      CsvFileWriter csvWriter,
                      PropertyFileUtil propertyFileUtil,
                      BullhornData bullhornData,
                      PrintUtil printUtil,
                      ActionTotals actionTotals) {
        super(command, rowNumber, entityClass, dataMap, csvWriter, propertyFileUtil, bullhornData, printUtil, actionTotals);
    }

    /**
     * Run method on this runnable object called by the thread manager.
     * <p>
     * At this point, we should have an entityClass type that we know we can delete (soft or hard).
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

    private <D extends DeleteEntity> Result handle(){
        entityID = Integer.parseInt(dataMap.get("id"));
        bullhornData.deleteEntity((Class<D>) entityClass, entityID);
        return Result.Delete(entityID);
    }

}

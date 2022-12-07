package com.bullhorn.dataloader.service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

import com.bullhorn.dataloader.data.ActionTotals;
import com.bullhorn.dataloader.enums.Command;
import com.bullhorn.dataloader.enums.EntityInfo;
import com.bullhorn.dataloader.rest.CompleteUtil;
import com.bullhorn.dataloader.rest.RestSession;
import com.bullhorn.dataloader.util.FileUtil;
import com.bullhorn.dataloader.util.PrintUtil;
import com.bullhorn.dataloader.util.PropertyFileUtil;
import com.bullhorn.dataloader.util.Timer;
import com.bullhorn.dataloader.util.ValidationUtil;

/**
 * Delete service implementation
 * <p>
 * Takes the user's command line arguments and runs a delete process
 */
public class DeleteService extends AbstractService implements Action {

    public DeleteService(PrintUtil printUtil,
                         PropertyFileUtil propertyFileUtil,
                         CompleteUtil completeUtil,
                         RestSession restSession,
                         ProcessRunner processRunner,
                         InputStream inputStream,
                         Timer timer) {
        super(printUtil, propertyFileUtil, completeUtil, restSession, processRunner, inputStream, timer);
    }

    @Override
    public void run(String[] args) throws IOException, InterruptedException {
        String filePath = args[1];
        SortedMap<EntityInfo, List<String>> entityToFileListMap = FileUtil.getDeletableCsvFilesFromPath(filePath, propertyFileUtil);
        if (promptUserForMultipleFiles(filePath, entityToFileListMap)) {
            for (Map.Entry<EntityInfo, List<String>> entityFileEntry : entityToFileListMap.entrySet()) {
                EntityInfo entityInfo = entityFileEntry.getKey();
                for (String fileName : entityFileEntry.getValue()) {
                    printUtil.printAndLog("Deleting " + entityInfo.getEntityName() + " records from: " + fileName + "...");
                    timer.start();
                    ActionTotals actionTotals = processRunner.run(Command.DELETE, entityInfo, fileName);
                    printUtil.printAndLog("Finished deleting " + entityInfo.getEntityName() + " records in " + timer.getDurationStringHms());
                    completeUtil.complete(Command.DELETE, fileName, entityInfo, actionTotals);
                }
            }
        }
    }

    @Override
    public boolean isValidArguments(String[] args) {
        if (!ValidationUtil.validateNumArgs(args, 2, printUtil)) {
            return false;
        }

        String filePath = args[1];
        File file = new File(filePath);
        if (file.isDirectory()) {
            if (FileUtil.getDeletableCsvFilesFromPath(filePath, propertyFileUtil).isEmpty()) {
                printUtil.printAndLog("ERROR: Could not find any valid CSV files (with entity name) to delete from directory: " + filePath);
                return false;
            }
            return true;
        }

        return ValidationUtil.validateCsvFile(filePath)
            && ValidationUtil.validateEntityFromFileNameOrProperty(filePath, propertyFileUtil, printUtil)
            && ValidationUtil.validateDeletableEntity(FileUtil.extractEntityFromFileNameOrProperty(filePath, propertyFileUtil), printUtil);
    }
}

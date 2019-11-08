package com.bullhorn.dataloader.service;

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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.concurrent.TimeUnit;

/**
 * Load (Insert/Update) service implementation
 *
 * Takes the user's command line arguments and runs a load process
 */
public class LoadService extends AbstractService implements Action {

    public LoadService(PrintUtil printUtil,
                       PropertyFileUtil propertyFileUtil,
                       CompleteUtil completeUtil,
                       RestSession restSession,
                       ProcessRunner processRunner,
                       InputStream inputStream,
                       Timer timer) {
        super(printUtil, propertyFileUtil, completeUtil, restSession, processRunner, inputStream, timer);
    }

    @Override
    public void run(String[] args) throws InterruptedException, IOException {
        String filePath = args[1];
        SortedMap<EntityInfo, List<String>> entityToFileListMap = FileUtil.getLoadableCsvFilesFromPath(
            filePath, propertyFileUtil);
        if (promptUserForMultipleFiles(filePath, entityToFileListMap)) {
            for (Map.Entry<EntityInfo, List<String>> entityFileEntry : entityToFileListMap.entrySet()) {
                EntityInfo entityInfo = entityFileEntry.getKey();
                for (String fileName : entityFileEntry.getValue()) {
                    printUtil.printAndLog("Loading " + entityInfo.getEntityName() + " records from: " + fileName + "...");
                    timer.start();
                    ActionTotals actionTotals = processRunner.run(Command.LOAD, entityInfo, fileName);
                    printUtil.printAndLog("Finished loading " + entityInfo.getEntityName() + " records in " + timer.getDurationStringHms());
                    completeUtil.complete(Command.LOAD, fileName, entityInfo, actionTotals);
                }

                Integer waitSeconds = propertyFileUtil.getWaitSecondsBetweenFilesInDirectory();
                if (waitSeconds > 0) {
                    printUtil.printAndLog("...Waiting " + waitSeconds + " seconds for indexers to catch up...");
                    TimeUnit.SECONDS.sleep(waitSeconds);
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
            if (FileUtil.getLoadableCsvFilesFromPath(filePath, propertyFileUtil).isEmpty()) {
                printUtil.printAndLog("ERROR: Could not find any valid CSV files (with entity name) to load from directory: " + filePath);
                return false;
            }
            return true;
        }

        return ValidationUtil.validateCsvFile(filePath, printUtil)
            && ValidationUtil.validateEntityFromFileNameOrProperty(filePath, propertyFileUtil, printUtil)
            && ValidationUtil.validateLoadableEntity(FileUtil.extractEntityFromFileNameOrProperty(filePath, propertyFileUtil), printUtil);
    }
}

package com.bullhorn.dataloader.service;

import com.bullhorn.dataloader.enums.Command;
import com.bullhorn.dataloader.enums.EntityInfo;
import com.bullhorn.dataloader.util.ActionTotals;
import com.bullhorn.dataloader.util.CompleteUtil;
import com.bullhorn.dataloader.util.ConnectionUtil;
import com.bullhorn.dataloader.util.FileUtil;
import com.bullhorn.dataloader.util.PrintUtil;
import com.bullhorn.dataloader.util.ProcessRunnerUtil;
import com.bullhorn.dataloader.util.PropertyFileUtil;
import com.bullhorn.dataloader.util.Timer;
import com.bullhorn.dataloader.util.validation.ValidationUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.concurrent.TimeUnit;

/**
 * Load (Insert/Update) service implementation
 * <p>
 * Takes the user's command line arguments and runs a load process
 */
public class LoadService extends AbstractService implements Action {

    public LoadService(PrintUtil printUtil,
                       PropertyFileUtil propertyFileUtil,
                       ValidationUtil validationUtil,
                       CompleteUtil completeUtil,
                       ConnectionUtil connectionUtil,
                       ProcessRunnerUtil processRunnerUtil,
                       InputStream inputStream,
                       Timer timer) throws IOException {
        super(printUtil, propertyFileUtil, validationUtil, completeUtil, connectionUtil, processRunnerUtil, inputStream, timer);
    }

    @Override
    public void run(String[] args) throws InterruptedException {
        if (!isValidArguments(args)) {
            throw new IllegalStateException("invalid command line arguments");
        }

        String filePath = args[1];
        SortedMap<EntityInfo, List<String>> entityToFileListMap = FileUtil.getLoadableCsvFilesFromPath(filePath, validationUtil);
        if (promptUserForMultipleFiles(filePath, entityToFileListMap)) {
            for (Map.Entry<EntityInfo, List<String>> entityFileEntry : entityToFileListMap.entrySet()) {
                EntityInfo entityInfo = entityFileEntry.getKey();
                for (String fileName : entityFileEntry.getValue()) {
                    try {
                        printUtil.printAndLog("Loading " + entityInfo.getEntityName() + " records from: " + fileName + "...");
                        timer.start();
                        ActionTotals actionTotals = processRunnerUtil.runLoadProcess(entityInfo, fileName);
                        printUtil.printAndLog("Finished loading " + entityInfo.getEntityName() + " records in " + timer.getDurationStringHMS());
                        completeUtil.complete(Command.LOAD, fileName, entityInfo, actionTotals, timer);
                    } catch (Exception e) {
                        printUtil.printAndLog("FAILED to load: " + entityInfo.getEntityName() + " records");
                        printUtil.printAndLog(e);
                    }
                }

                // region ~WORKAROUND~
                // Even V2 indexers can take a while to index during normal business hours in the QA environment.
                Integer waitTimeMsec = propertyFileUtil.getWaitTimeMsecBetweenFilesInDirectory();
                if (waitTimeMsec > 0) {
                    printUtil.printAndLog("...Waiting " + waitTimeMsec / 1000 + " seconds for indexers to catch up...");
                    TimeUnit.MILLISECONDS.sleep(waitTimeMsec);
                }
                // endregion
            }
        }
    }

    @Override
    public boolean isValidArguments(String[] args) {
        if (!validationUtil.isNumParametersValid(args, 2)) {
            return false;
        }

        String filePath = args[1];
        File file = new File(filePath);
        if (file.isDirectory()) {
            if (FileUtil.getLoadableCsvFilesFromPath(filePath, validationUtil).isEmpty()) {
                printUtil.printAndLog("ERROR: Could not find any valid CSV files (with entity name) to load from directory: " + filePath);
                return false;
            }
        } else {
            if (!validationUtil.isValidCsvFile(filePath)) {
                return false;
            }

            EntityInfo entityInfo = FileUtil.extractEntityFromFileName(filePath);
            if (entityInfo == null) {
                printUtil.printAndLog("ERROR: Could not determine entity from file name: " + filePath);
                return false;
            }

            if (!entityInfo.isLoadable()) {
                printUtil.printAndLog("ERROR: " + entityInfo.getEntityName() + " entity is read only.");
                return false;
            }
        }

        return true;
    }
}

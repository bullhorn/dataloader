package com.bullhorn.dataloader.service;

import com.bullhorn.dataloader.data.ActionTotals;
import com.bullhorn.dataloader.enums.Command;
import com.bullhorn.dataloader.enums.EntityInfo;
import com.bullhorn.dataloader.rest.CompleteCall;
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

/**
 * Delete service implementation
 *
 * Takes the user's command line arguments and runs a delete process
 */
public class DeleteService extends AbstractService implements Action {

    public DeleteService(PrintUtil printUtil,
                         PropertyFileUtil propertyFileUtil,
                         ValidationUtil validationUtil,
                         CompleteCall completeCall,
                         RestSession restSession,
                         ProcessRunner processRunner,
                         InputStream inputStream,
                         Timer timer) throws IOException {
        super(printUtil, propertyFileUtil, validationUtil, completeCall, restSession, processRunner, inputStream, timer);
    }

    @Override
    public void run(String[] args) {
        if (!isValidArguments(args)) {
            throw new IllegalStateException("invalid command line arguments");
        }

        String filePath = args[1];
        SortedMap<EntityInfo, List<String>> entityToFileListMap = FileUtil.getDeletableCsvFilesFromPath(filePath, validationUtil);
        if (promptUserForMultipleFiles(filePath, entityToFileListMap)) {
            for (Map.Entry<EntityInfo, List<String>> entityFileEntry : entityToFileListMap.entrySet()) {
                EntityInfo entityInfo = entityFileEntry.getKey();
                for (String fileName : entityFileEntry.getValue()) {
                    try {
                        printUtil.printAndLog("Deleting " + entityInfo.getEntityName() + " records from: " + fileName + "...");
                        timer.start();
                        ActionTotals actionTotals = processRunner.runDeleteProcess(entityInfo, fileName);
                        printUtil.printAndLog("Finished deleting " + entityInfo.getEntityName() + " records in " + timer.getDurationStringHMS());
                        completeCall.complete(Command.DELETE, fileName, entityInfo, actionTotals, timer);
                    } catch (Exception e) {
                        printUtil.printAndLog("FAILED to delete " + entityInfo.getEntityName() + " records");
                        printUtil.printAndLog(e);
                    }
                }
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
            if (FileUtil.getDeletableCsvFilesFromPath(filePath, validationUtil).isEmpty()) {
                printUtil.printAndLog("ERROR: Could not find any valid CSV files (with entity name) to delete from directory: " + filePath);
                return false;
            }
        } else {
            if (!validationUtil.isValidCsvFile(args[1])) {
                return false;
            }

            EntityInfo entityInfo = FileUtil.extractEntityFromFileName(filePath);
            if (entityInfo == null) {
                printUtil.printAndLog("Could not determine entity from file name: " + filePath);
                return false;
            }

            if (!entityInfo.isDeletable()) {
                printUtil.printAndLog("ERROR: " + entityInfo.getEntityName() + " entity is not deletable.");
                return false;
            }
        }

        return true;
    }
}

package com.bullhorn.dataloader.service;

import com.bullhorn.dataloader.enums.Command;
import com.bullhorn.dataloader.enums.EntityInfo;
import com.bullhorn.dataloader.service.executor.ConcurrencyService;
import com.bullhorn.dataloader.util.CompleteUtil;
import com.bullhorn.dataloader.util.ConnectionUtil;
import com.bullhorn.dataloader.util.PrintUtil;
import com.bullhorn.dataloader.util.PropertyFileUtil;
import com.bullhorn.dataloader.util.Timer;
import com.bullhorn.dataloader.util.validation.ValidationUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

/**
 * Delete service implementation
 */
public class DeleteService extends AbstractService implements Action {

    public DeleteService(PrintUtil printUtil,
                         PropertyFileUtil propertyFileUtil,
                         ValidationUtil validationUtil,
                         CompleteUtil completeUtil,
                         ConnectionUtil connectionUtil,
                         InputStream inputStream,
                         Timer timer) throws IOException {
        super(printUtil, propertyFileUtil, validationUtil, completeUtil, connectionUtil, inputStream, timer);
    }

    @Override
    public void run(String[] args) {
        if (!isValidArguments(args)) {
            throw new IllegalStateException("invalid command line arguments");
        }

        String filePath = args[1];
        SortedMap<EntityInfo, List<String>> entityToFileListMap = getDeletableCsvFilesFromPath(filePath);
        if (promptUserForMultipleFiles(filePath, entityToFileListMap)) {
            for (Map.Entry<EntityInfo, List<String>> entityFileEntry : entityToFileListMap.entrySet()) {
                EntityInfo entityInfo = entityFileEntry.getKey();
                for (String fileName : entityFileEntry.getValue()) {
                    try {
                        printUtil.printAndLog("Deleting " + entityInfo.getEntityName() + " records from: " + fileName + "...");
                        ConcurrencyService concurrencyService = createConcurrencyService(Command.DELETE, entityInfo, fileName);
                        timer.start();
                        concurrencyService.runDeleteProcess();
                        printUtil.printAndLog("Finished deleting " + entityInfo.getEntityName() + " records in " + timer.getDurationStringHMS());
                        completeUtil.complete(Command.DELETE, fileName, entityInfo, concurrencyService.getActionTotals(), timer.getDurationMillis(), concurrencyService.getBullhornRestApi());
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
            if (getDeletableCsvFilesFromPath(filePath).isEmpty()) {
                printUtil.printAndLog("ERROR: Could not find any valid CSV files (with entity name) to delete from directory: " + filePath);
                return false;
            }
        } else {
            if (!validationUtil.isValidCsvFile(args[1])) {
                return false;
            }

            EntityInfo entityInfo = extractEntityFromFileName(filePath);
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

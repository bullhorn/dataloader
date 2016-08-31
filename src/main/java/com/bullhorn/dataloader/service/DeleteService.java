package com.bullhorn.dataloader.service;

import com.bullhorn.dataloader.meta.EntityInfo;
import com.bullhorn.dataloader.service.executor.ConcurrencyService;
import com.bullhorn.dataloader.util.PrintUtil;
import com.bullhorn.dataloader.util.PropertyFileUtil;
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
                         ValidationUtil validationUtil, InputStream inputStream) throws IOException {
        super(printUtil, propertyFileUtil, validationUtil, inputStream);
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

            String entityName = extractEntityNameFromFileName(filePath);
            if (entityName == null) {
                printUtil.printAndLog("Could not determine entity from file name: " + filePath);
                return false;
            }

            if (!validationUtil.isDeletableEntity(entityName)) {
                return false;
            }
        }

        return true;
    }
}

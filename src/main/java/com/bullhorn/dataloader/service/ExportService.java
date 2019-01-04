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

/**
 * Exports service implementation
 *
 * Takes the user's command line arguments and runs an export process
 * that pulls data from Rest in the form of a CSV file.
 */
public class ExportService extends AbstractService implements Action {

    public ExportService(PrintUtil printUtil,
                         PropertyFileUtil propertyFileUtil,
                         ValidationUtil validationUtil,
                         CompleteUtil completeUtil,
                         RestSession restSession,
                         ProcessRunner processRunner,
                         InputStream inputStream,
                         Timer timer) {
        super(printUtil, propertyFileUtil, validationUtil, completeUtil, restSession, processRunner, inputStream, timer);
    }

    @Override
    public void run(String[] args) throws IOException, InterruptedException {
        if (!isValidArguments(args)) {
            throw new IllegalStateException("invalid command line arguments");
        }

        String filePath = args[1];
        SortedMap<EntityInfo, List<String>> entityToFileListMap = FileUtil.getValidCsvFiles(filePath, validationUtil, EntityInfo.loadOrderComparator);
        if (promptUserForMultipleFiles(filePath, entityToFileListMap)) {
            for (Map.Entry<EntityInfo, List<String>> entityFileEntry : entityToFileListMap.entrySet()) {
                EntityInfo entityInfo = entityFileEntry.getKey();
                for (String fileName : entityFileEntry.getValue()) {
                    printUtil.printAndLog("Exporting " + entityInfo.getEntityName() + " records from: " + fileName + "...");
                    timer.start();
                    ActionTotals actionTotals = processRunner.run(Command.EXPORT, entityInfo, fileName);
                    printUtil.printAndLog("Finished exporting " + entityInfo.getEntityName() + " records in " + timer.getDurationStringHms());
                    completeUtil.complete(Command.EXPORT, fileName, entityInfo, actionTotals);
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
            if (FileUtil.getValidCsvFiles(filePath, validationUtil, EntityInfo.loadOrderComparator).isEmpty()) {
                printUtil.printAndLog("ERROR: Could not find any valid CSV files (with entity name) to export from directory: " + filePath);
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
        }

        return true;
    }
}
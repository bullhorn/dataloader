package com.bullhorn.dataloader.service;

import com.bullhorn.dataloader.enums.EntityInfo;
import com.bullhorn.dataloader.util.CompleteUtil;
import com.bullhorn.dataloader.util.ConnectionUtil;
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
import java.util.Scanner;
import java.util.SortedMap;

/**
 * Base class for all command line actions that convert user input to a process that executes and reports results.
 * <p>
 * Contains common functionality.
 */
public abstract class AbstractService {

    final protected PrintUtil printUtil;
    final protected PropertyFileUtil propertyFileUtil;
    final protected ValidationUtil validationUtil;
    final protected CompleteUtil completeUtil;
    final protected ConnectionUtil connectionUtil;
    final protected ProcessRunnerUtil processRunnerUtil;
    final protected InputStream inputStream;
    final protected Timer timer;

    public AbstractService(PrintUtil printUtil,
                           PropertyFileUtil propertyFileUtil,
                           ValidationUtil validationUtil,
                           CompleteUtil completeUtil,
                           ConnectionUtil connectionUtil,
                           ProcessRunnerUtil processRunnerUtil,
                           InputStream inputStream,
                           Timer timer) throws IOException {
        this.printUtil = printUtil;
        this.propertyFileUtil = propertyFileUtil;
        this.validationUtil = validationUtil;
        this.completeUtil = completeUtil;
        this.connectionUtil = connectionUtil;
        this.processRunnerUtil = processRunnerUtil;
        this.inputStream = inputStream;
        this.timer = timer;
    }

    /**
     * When loading from directory, give the user a chance to hit ENTER or CTRL+C once they see all the files about
     * to be processed. Handles the case where there are multiple entities with multiple files or one entity with
     * multiple files.
     *
     * @param filePath            The user provided directory where these files came from
     * @param entityToFileListMap The list of files that will be loaded
     * @return true if the user has responded with yes, false if no
     */
    protected Boolean promptUserForMultipleFiles(String filePath, SortedMap<EntityInfo, List<String>> entityToFileListMap) {
        if (entityToFileListMap.size() > 1 ||
            (!entityToFileListMap.isEmpty() &&
                entityToFileListMap.get(entityToFileListMap.firstKey()).size() > 1)) {
            printUtil.printAndLog("Ready to process the following CSV files from the " + filePath + " directory in the following order:");

            Integer count = 1;
            for (Map.Entry<EntityInfo, List<String>> entityFileEntry : entityToFileListMap.entrySet()) {
                String entityName = entityFileEntry.getKey().getEntityName();
                for (String fileName : entityFileEntry.getValue()) {
                    File file = new File(fileName);
                    printUtil.printAndLog("   " + count++ + ". " + entityName + " records from " + file.getName());
                }
            }

            printUtil.print("Do you want to continue? [Y/N]");
            Scanner scanner = new Scanner(inputStream);
            Boolean yesOrNoResponse = false;
            while (!yesOrNoResponse) {
                String input = scanner.nextLine();
                if (input.startsWith("y") || input.startsWith("Y")) {
                    yesOrNoResponse = true;
                } else if (input.startsWith("n") || input.startsWith("N")) {
                    return false;
                }
            }
        }

        return true;
    }
}

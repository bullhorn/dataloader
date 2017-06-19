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

import java.io.IOException;
import java.io.InputStream;

/**
 * Handles converting attachments
 * <p>
 * Takes the user's command line arguments and converts attachments from doc/pdf to html.
 */
public class ConvertAttachmentsService extends AbstractService implements Action {

    public ConvertAttachmentsService(PrintUtil printUtil,
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
    public void run(String[] args) {
        if (!isValidArguments(args)) {
            throw new IllegalStateException("invalid command line arguments");
        }

        String filePath = args[1];
        EntityInfo entityInfo = FileUtil.extractEntityFromFileName(filePath);

        try {
            printUtil.printAndLog("Converting " + entityInfo + " attachments from: " + filePath + "...");
            timer.start();
            ActionTotals actionTotals = processRunnerUtil.runConvertAttachmentsProcess(entityInfo, filePath);
            printUtil.printAndLog("Finished converting " + entityInfo + " attachments in " + timer.getDurationStringHMS());
            completeUtil.complete(Command.CONVERT_ATTACHMENTS, filePath, entityInfo, actionTotals, timer);
        } catch (Exception e) {
            printUtil.printAndLog("FAILED to convert " + entityInfo + " attachments");
            printUtil.printAndLog(e);
        }
    }

    @Override
    public boolean isValidArguments(String[] args) {
        if (!validationUtil.isNumParametersValid(args, 2)) {
            return false;
        }

        String filePath = args[1];
        if (!validationUtil.isValidCsvFile(args[1])) {
            return false;
        }

        EntityInfo entityInfo = FileUtil.extractEntityFromFileName(filePath);
        if (entityInfo == null) {
            printUtil.printAndLog("Could not determine entity from file name: " + filePath);
            return false;
        }

        return true;
    }
}

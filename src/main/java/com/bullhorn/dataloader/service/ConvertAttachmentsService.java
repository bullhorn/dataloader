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

import java.io.IOException;
import java.io.InputStream;

/**
 * Handles converting attachments
 *
 * Takes the user's command line arguments and converts attachments from doc/pdf to html.
 */
public class ConvertAttachmentsService extends AbstractService implements Action {

    public ConvertAttachmentsService(PrintUtil printUtil,
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
        EntityInfo entityInfo = FileUtil.extractEntityFromFileName(filePath);

        printUtil.printAndLog("Converting " + entityInfo.getEntityName() + " attachments from: " + filePath + "...");
        timer.start();
        ActionTotals actionTotals = processRunner.run(Command.CONVERT_ATTACHMENTS, entityInfo, filePath);
        printUtil.printAndLog("Finished converting " + entityInfo.getEntityName() + " attachments in " + timer.getDurationStringHms());
        completeUtil.complete(Command.CONVERT_ATTACHMENTS, filePath, entityInfo, actionTotals);
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

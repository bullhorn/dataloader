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

import java.io.IOException;
import java.io.InputStream;

/**
 * Handles loading attachments
 *
 * Takes the user's command line arguments and runs a load attachments process
 */
public class LoadAttachmentsService extends AbstractService implements Action {

    public LoadAttachmentsService(PrintUtil printUtil,
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
        EntityInfo entityInfo = FileUtil.extractEntityFromFileName(filePath);

        try {
            printUtil.printAndLog("Loading " + entityInfo + " attachments from: " + filePath + "...");
            timer.start();
            ActionTotals actionTotals = processRunner.runLoadAttachmentsProcess(entityInfo, filePath);
            printUtil.printAndLog("Finished loading " + entityInfo + " attachments in " + timer.getDurationStringHMS());
            completeCall.complete(Command.LOAD_ATTACHMENTS, filePath, entityInfo, actionTotals, timer);
        } catch (Exception e) {
            printUtil.printAndLog("FAILED to load " + entityInfo + " attachments");
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

        if (!entityInfo.isAttachmentEntity()) {
            printUtil.printAndLog("ERROR: " + entityInfo.getEntityName() + " entity does not support attachments.");
            return false;
        }

        return true;
    }
}

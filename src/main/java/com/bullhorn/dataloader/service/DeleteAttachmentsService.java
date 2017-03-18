package com.bullhorn.dataloader.service;

import com.bullhorn.dataloader.enums.Command;
import com.bullhorn.dataloader.enums.EntityInfo;
import com.bullhorn.dataloader.service.executor.ConcurrencyService;
import com.bullhorn.dataloader.util.CompleteUtil;
import com.bullhorn.dataloader.util.PrintUtil;
import com.bullhorn.dataloader.util.PropertyFileUtil;
import com.bullhorn.dataloader.util.Timer;
import com.bullhorn.dataloader.util.validation.ValidationUtil;

import java.io.IOException;
import java.io.InputStream;

/**
 * Handles deleting attachments
 */
public class DeleteAttachmentsService extends AbstractService implements Action {

    public DeleteAttachmentsService(PrintUtil printUtil,
                                    PropertyFileUtil propertyFileUtil,
                                    ValidationUtil validationUtil,
                                    CompleteUtil completeUtil,
                                    InputStream inputStream,
                                    Timer timer) throws IOException {
        super(printUtil, propertyFileUtil, validationUtil, completeUtil, inputStream, timer);
    }

    @Override
    public void run(String[] args) {
        if (!isValidArguments(args)) {
            throw new IllegalStateException("invalid command line arguments");
        }

        String filePath = args[1];
        EntityInfo entityInfo = extractEntityFromFileName(filePath);
        if (entityInfo == null) {
            throw new IllegalArgumentException("unknown or missing entityInfo");
        }

        try {
            printUtil.printAndLog("Deleting " + entityInfo + " attachments from: " + filePath + "...");
            ConcurrencyService concurrencyService = createConcurrencyService(Command.DELETE_ATTACHMENTS, entityInfo, filePath);
            timer.start();
            concurrencyService.runDeleteAttachmentsProcess();
            printUtil.printAndLog("Finished deleting " + entityInfo + " attachments in " + timer.getDurationStringHMS());
            completeUtil.complete(Command.DELETE_ATTACHMENTS, filePath, entityInfo, concurrencyService.getActionTotals(), timer.getDurationMillis(), concurrencyService.getBullhornData());
        } catch (Exception e) {
            printUtil.printAndLog("FAILED to delete " + entityInfo + " attachments");
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

        String entityName = extractEntityNameFromFileName(filePath);
        if (entityName == null) {
            printUtil.printAndLog("Could not determine entity from file name: " + filePath);
            return false;
        }

        if (!isValidAttachmentEntity(entityName)) {
            printUtil.printAndLog("deleteAttachments not available for " + entityName);
            return false;
        }

        return true;
    }
}

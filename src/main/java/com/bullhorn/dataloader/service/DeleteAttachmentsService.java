package com.bullhorn.dataloader.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

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

/**
 * Handles deleting attachments
 *
 * Takes the user's command line arguments and runs a delete attachments process
 */
public class DeleteAttachmentsService extends AbstractService implements Action {

    public DeleteAttachmentsService(PrintUtil printUtil,
                                    PropertyFileUtil propertyFileUtil,
                                    CompleteUtil completeUtil,
                                    RestSession restSession,
                                    ProcessRunner processRunner,
                                    InputStream inputStream,
                                    Timer timer) {
        super(printUtil, propertyFileUtil, completeUtil, restSession, processRunner, inputStream, timer);
    }

    @Override
    public void run(String[] args) throws IOException, InterruptedException {
        String filePath = args[1];
        EntityInfo entityInfo = FileUtil.extractEntityFromFileNameOrProperty(filePath, propertyFileUtil);
        printUtil.printAndLog("Deleting " + Objects.requireNonNull(entityInfo).getEntityName() + " attachments from: " + filePath + "...");
        timer.start();
        ActionTotals actionTotals = processRunner.run(Command.DELETE_ATTACHMENTS, entityInfo, filePath);
        printUtil.printAndLog("Finished deleting " + entityInfo.getEntityName() + " attachments in " + timer.getDurationStringHms());
        completeUtil.complete(Command.DELETE_ATTACHMENTS, filePath, entityInfo, actionTotals);
    }

    @Override
    public boolean isValidArguments(String[] args) {
        return ValidationUtil.validateNumArgs(args, 2, printUtil)
            && ValidationUtil.validateCsvFile(args[1])
            && ValidationUtil.validateEntityFromFileNameOrProperty(args[1], propertyFileUtil, printUtil)
            && ValidationUtil.validateAttachmentEntity(FileUtil.extractEntityFromFileNameOrProperty(args[1], propertyFileUtil), printUtil);
    }
}

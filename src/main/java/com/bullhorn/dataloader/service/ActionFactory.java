package com.bullhorn.dataloader.service;

import java.io.InputStream;

import com.bullhorn.dataloader.enums.Command;
import com.bullhorn.dataloader.rest.CompleteUtil;
import com.bullhorn.dataloader.rest.RestSession;
import com.bullhorn.dataloader.util.PrintUtil;
import com.bullhorn.dataloader.util.PropertyFileUtil;
import com.bullhorn.dataloader.util.Timer;

/**
 * Given a command, this class returns an action that can execute that command.
 */
public class ActionFactory {

    private final PrintUtil printUtil;
    private final PropertyFileUtil propertyFileUtil;
    private final CompleteUtil completeUtil;
    private final RestSession restSession;
    private final ProcessRunner processRunner;
    private final InputStream inputStream;
    private final Timer timer;

    public ActionFactory(PrintUtil printUtil,
                         PropertyFileUtil propertyFileUtil,
                         CompleteUtil completeUtil,
                         RestSession restSession,
                         ProcessRunner processRunner,
                         InputStream inputStream,
                         Timer timer) {
        this.printUtil = printUtil;
        this.propertyFileUtil = propertyFileUtil;
        this.completeUtil = completeUtil;
        this.restSession = restSession;
        this.processRunner = processRunner;
        this.inputStream = inputStream;
        this.timer = timer;
    }

    /**
     * Given a command enum, this returns the action that will accomplish that command.
     *
     * @param command The user's command
     * @return The corresponding action
     */
    public Action getAction(Command command) {
        Action action = null;
        if (command.equals(Command.CONVERT_ATTACHMENTS)) {
            action = new ConvertAttachmentsService(printUtil, propertyFileUtil, completeUtil, restSession, processRunner, inputStream, timer);
        } else if (command.equals(Command.DELETE)) {
            action = new DeleteService(printUtil, propertyFileUtil, completeUtil, restSession, processRunner, inputStream, timer);
        } else if (command.equals(Command.DELETE_ATTACHMENTS)) {
            action = new DeleteAttachmentsService(printUtil, propertyFileUtil, completeUtil, restSession, processRunner, inputStream, timer);
        } else if (command.equals(Command.EXPORT)) {
            action = new ExportService(printUtil, propertyFileUtil, completeUtil, restSession, processRunner, inputStream, timer);
        } else if (command.equals(Command.HELP)) {
            action = new HelpService(printUtil);
        } else if (command.equals(Command.LOAD)) {
            action = new LoadService(printUtil, propertyFileUtil, completeUtil, restSession, processRunner, inputStream, timer);
        } else if (command.equals(Command.LOAD_ATTACHMENTS)) {
            action = new LoadAttachmentsService(printUtil, propertyFileUtil, completeUtil, restSession, processRunner, inputStream, timer);
        } else if (command.equals(Command.LOGIN)) {
            action = new LoginService(restSession, printUtil);
        } else if (command.equals(Command.META)) {
            action = new MetaService(restSession, printUtil);
        } else if (command.equals(Command.PARSE_RESUMES)) {
            action = new ParseResumeService(restSession, propertyFileUtil, printUtil, completeUtil, timer);
        } else if (command.equals(Command.TEMPLATE)) {
            action = new TemplateService(printUtil, propertyFileUtil, completeUtil, restSession, processRunner, inputStream, timer);
        }
        return action;
    }
}

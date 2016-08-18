package com.bullhorn.dataloader.service;

import com.bullhorn.dataloader.util.PrintUtil;
import com.bullhorn.dataloader.util.PropertyFileUtil;
import com.bullhorn.dataloader.util.validation.ValidationUtil;

import java.io.IOException;
import java.io.InputStream;

/**
 * Given a command, this class builds the action that is required to execute that command.
 */
public class ActionFactory {

    final private PrintUtil printUtil;
    final private ValidationUtil validationUtil;
    final private PropertyFileUtil propertyFileUtil;
    final private InputStream inputStream;

    public ActionFactory(PrintUtil printUtil,
                         PropertyFileUtil propertyFileUtil,
                         ValidationUtil validationUtil, 
                         InputStream inputStream) {
        this.printUtil = printUtil;
        this.validationUtil = validationUtil;
        this.propertyFileUtil = propertyFileUtil;
        this.inputStream = inputStream;
    }

    /**
     * Given a command enum, this returns the action that will accomplish that command.
     * @param command The user's command
     * @return The corresponding action
     */
    public Action getAction(Command command) throws IOException {
        Action action = null;
        if (command.equals(Command.HELP)) {
            action = new HelpService(printUtil, propertyFileUtil, validationUtil, inputStream);
        } else if (command.equals(Command.TEMPLATE)) {
            action = new TemplateService(printUtil, propertyFileUtil, validationUtil, inputStream);
        } else if (command.equals(Command.CONVERT_ATTACHMENTS)) {
            action = new ConvertAttachmentsService(printUtil, propertyFileUtil, validationUtil, inputStream);
        } else if (command.equals(Command.LOAD)) {
            action = new LoadService(printUtil, propertyFileUtil, validationUtil, inputStream);
        } else if (command.equals(Command.DELETE)) {
            action = new DeleteService(printUtil, propertyFileUtil, validationUtil, inputStream);
        } else if (command.equals(Command.LOAD_ATTACHMENTS)) {
            action = new LoadAttachmentsService(printUtil, propertyFileUtil, validationUtil, inputStream);
        } else if (command.equals(Command.DELETE_ATTACHMENTS)) {
            action = new DeleteAttachmentsService(printUtil, propertyFileUtil, validationUtil, inputStream);
        }
        return action;
    }
}

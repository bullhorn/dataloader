package com.bullhorn.dataloader.service;

import java.io.IOException;

import com.bullhorn.dataloader.util.PrintUtil;

/**
 * Given a command, this class builds the action that is required to execute that command.
 */
public class ActionBuilder {

    private PrintUtil printUtil;
    private String propertyFilePath;

    public ActionBuilder(PrintUtil printUtil, String propertyFilePath) {
        this.printUtil = printUtil;
        this.propertyFilePath = propertyFilePath;
    }

    /**
     * Given a command enum, this returns the action that will accomplish that command.
     * @param command The user's command
     * @return The corresponding action
     */
    public Action getAction(Command command) throws IOException {
        Action action = null;
        if (command.equals(Command.TEMPLATE)) {
            action = new TemplateService(printUtil, propertyFilePath);
        } else if (command.equals(Command.LOAD)) {
            action = new LoadService(printUtil, propertyFilePath);
        } else if (command.equals(Command.DELETE)) {
            action = new DeleteService(printUtil, propertyFilePath);
        } else if (command.equals(Command.LOAD_ATTACHMENTS)) {
            action = new LoadAttachmentsService(printUtil, propertyFilePath);
        } else if (command.equals(Command.DELETE_ATTACHMENTS)) {
            action = new DeleteAttachmentsService(printUtil, propertyFilePath);
        }
        return action;
    }
}

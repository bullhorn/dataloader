package com.bullhorn.dataloader.service;

/**
 * Enumeration of Command Line Actions
 */
public enum Command {

    TEMPLATE("template"),
    LOAD("load"),
    DELETE("delete"),
    LOAD_ATTACHMENTS("loadAttachments"),
    DELETE_ATTACHMENTS("deleteAttachments");

    private final String methodName;

    private Command(String methodName) {
        this.methodName = methodName;
    }

    /**
     * Return command line method name
     *
     * @return the action the user specifies on the command line
     */
    public String getMethodName() {
        return this.methodName;
    }
}

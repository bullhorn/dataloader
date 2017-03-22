package com.bullhorn.dataloader.enums;

/**
 * Enumeration of Command Line Actions
 */
public enum Command {

    HELP("help"),
    TEMPLATE("template"),
    CONVERT_ATTACHMENTS("convertAttachments"),
    LOAD("load"),
    DELETE("delete"),
    LOAD_ATTACHMENTS("loadAttachments"),
    DELETE_ATTACHMENTS("deleteAttachments");

    private final String methodName;

    Command(String methodName) {
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
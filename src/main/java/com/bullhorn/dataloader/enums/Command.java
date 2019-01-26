package com.bullhorn.dataloader.enums;

/**
 * Enumeration of Command Line Actions
 */
public enum Command {

    CONVERT_ATTACHMENTS("convertAttachments"),
    DELETE("delete"),
    DELETE_ATTACHMENTS("deleteAttachments"),
    EXPORT("export"),
    HELP("help"),
    LOAD("load"),
    LOAD_ATTACHMENTS("loadAttachments"),
    LOGIN("login"),
    TEMPLATE("template");

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

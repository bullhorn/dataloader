package com.bullhorn.dataloader.service.consts;

public enum Method {

    TEMPLATES("templates"),
    LOAD("load"),
    DELETE("delete"),
    LOADATTACHMENTS("loadAttachments"),
    DELETEATTACHMENTS("deleteAttachments");


    private final String methodName;

    private Method(String methodName){
       this.methodName = methodName;
    }

    public String getMethodName(){
        return this.methodName;
    }
}

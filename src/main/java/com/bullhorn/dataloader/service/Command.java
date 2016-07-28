package com.bullhorn.dataloader.service;

public enum Command {

    TEMPLATE("template", new TemplateService()),
    LOAD("load", new LoadService()),
    DELETE("delete", new DeleteService()),
    LOAD_ATTACHMENTS("loadAttachments", new LoadAttachmentsService()),
    DELETE_ATTACHMENTS("deleteAttachments", new DeleteAttachmentsService());

	private Action action;
    private final String methodName;
	
    private Command(String methodName, Action action){
       this.methodName = methodName;
       this.action = action;
    }

	public Action getAction() {
		return this.action;
	}

    public String getMethodName(){
        return this.methodName;
    }
    
    void setAction(Action action) {
    	this.action = action;
    }
}

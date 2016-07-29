package com.bullhorn.dataloader.service;

/**
 * Command line actions 
 * 
 * @author jlrutledge
 *
 */
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

    /**
     * Return implementing interface instance
     * 
     * @return Action implementation
     */
	public Action getAction() {
		return this.action;
	}

	/**
	 * Return command line method name
	 * 
	 * @return the action the user specifies on the command line
	 */
    public String getMethodName(){
        return this.methodName;
    }
    
    /**
     * Override for unit testing
     * 
     * @param action mock Action implementation
     */
    void setAction(Action action) {
    	this.action = action;
    }
}

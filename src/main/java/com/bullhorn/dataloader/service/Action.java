package com.bullhorn.dataloader.service;

/**
 * The command line action definition
 * 
 * @author jlrutledge
 *
 */
public interface Action {

	/**
	 * Perform the action
	 * 
	 * @param args command line args pass to main()
	 */
	public void run(String[] args);
	
	/**
	 * Validate the command line arguments
	 * 
	 * @param args command line args pass to main()
	 * @return true if the arguments are valid
	 */
	public boolean isValidArguments(String[] args);
	
	/**
	 * Prints the command line usage for this action.
	 */
	public void printUsage();
	
}

package com.bullhorn.dataloader.service;

/**
 * The command line action interface
 */
public interface Action {

    /**
     * Perform the action
     *
     * @param args command line args pass to main()
     */
    void run(String[] args) throws InterruptedException;

    /**
     * Validate the command line arguments
     *
     * @param args command line args pass to main()
     * @return true if the arguments are valid
     */
    boolean isValidArguments(String[] args);
}

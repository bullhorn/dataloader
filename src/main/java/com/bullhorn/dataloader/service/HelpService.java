package com.bullhorn.dataloader.service;

import com.bullhorn.dataloader.util.PrintUtil;

/**
 * Prints the usage information for DataLoader on command
 */
public class HelpService implements Action {

    private final PrintUtil printUtil;

    HelpService(PrintUtil printUtil) {
        this.printUtil = printUtil;
    }

    @Override
    public void run(String[] args) {
        printUtil.printUsage();
    }

    @Override
    public boolean isValidArguments(String[] args) {
        return true;
    }
}

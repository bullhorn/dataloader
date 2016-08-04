package com.bullhorn.dataloader;

import com.bullhorn.dataloader.service.ActionBuilder;
import com.bullhorn.dataloader.service.CommandLineInterface;
import com.bullhorn.dataloader.util.PrintUtil;
import com.bullhorn.dataloader.util.StringConsts;

public class Main {

    /*
     * Allows for timestamp for most recent log file. With log4j, most recent log file with timestamp is not natively
     * supported. log4j assumes control of log file operations when main method is called.
     */
    static {
        System.setProperty("dataloader", "dataloader_" + StringConsts.TIMESTAMP);
    }

    public static void main(String[] args) {
        PrintUtil printUtil = new PrintUtil();
        String propertyFilePath = "dataloader.properties";
        ActionBuilder actionBuilder = new ActionBuilder(printUtil, propertyFilePath);
        CommandLineInterface commandLineInterface = new CommandLineInterface(printUtil, actionBuilder);
        commandLineInterface.start(args);
    }
}

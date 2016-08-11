package com.bullhorn.dataloader;

import com.bullhorn.dataloader.service.ActionFactory;
import com.bullhorn.dataloader.service.CommandLineInterface;
import com.bullhorn.dataloader.util.PrintUtil;
import com.bullhorn.dataloader.util.PropertyFileUtil;
import com.bullhorn.dataloader.util.StringConsts;
import com.bullhorn.dataloader.util.validation.PropertyValidation;
import com.bullhorn.dataloader.util.validation.ValidationUtil;

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

        try {
            PropertyValidation propertyValidation = new PropertyValidation();
            PropertyFileUtil propertyFileUtil = new PropertyFileUtil("dataloader.properties", propertyValidation, printUtil);
            ValidationUtil validationUtil = new ValidationUtil(printUtil);
            ActionFactory actionFactory = new ActionFactory(printUtil, propertyFileUtil, validationUtil, System.in);

            CommandLineInterface commandLineInterface = new CommandLineInterface(printUtil, actionFactory);
            commandLineInterface.start(args);
        } catch (Exception e) {
            printUtil.printAndLog(e);
        }
    }
}

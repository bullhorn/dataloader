package com.bullhorn.dataloader;

import com.bullhorn.dataloader.service.ActionFactory;
import com.bullhorn.dataloader.util.CompleteUtil;
import com.bullhorn.dataloader.util.ConnectionUtil;
import com.bullhorn.dataloader.util.PrintUtil;
import com.bullhorn.dataloader.util.PropertyFileUtil;
import com.bullhorn.dataloader.util.StringConsts;
import com.bullhorn.dataloader.util.Timer;
import com.bullhorn.dataloader.util.validation.PropertyValidation;
import com.bullhorn.dataloader.util.validation.ValidationUtil;
import org.apache.commons.httpclient.HttpClient;

public class Main {

    /*
     * Allows for timestamp for most recent log file. With log4j, most recent log file with timestamp is not natively
     * supported. log4j assumes control of log file operations when main method is called.
     */
    static {
        System.setProperty("dataloader", "dataloader_" + StringConsts.TIMESTAMP);
    }

    public static void main(String[] args) {
        HttpClient httpClient = new HttpClient();
        PrintUtil printUtil = new PrintUtil();
        Timer timer = new Timer();

        try {
            PropertyValidation propertyValidation = new PropertyValidation();
            PropertyFileUtil propertyFileUtil = new PropertyFileUtil("dataloader.properties", System.getenv(), System.getProperties(), args, propertyValidation, printUtil);
            ValidationUtil validationUtil = new ValidationUtil(printUtil);
            CompleteUtil completeUtil = new CompleteUtil(httpClient, propertyFileUtil, printUtil);
            ConnectionUtil connectionUtil = new ConnectionUtil(propertyFileUtil);
            ActionFactory actionFactory = new ActionFactory(printUtil, propertyFileUtil, validationUtil, completeUtil, connectionUtil, System.in, timer);

            CommandLineInterface commandLineInterface = new CommandLineInterface(printUtil, actionFactory);

            args = propertyFileUtil.getRemainingArgs();
            printUtil.recordStart(args);
            commandLineInterface.start(args);
        } catch (Exception e) {
            printUtil.printAndLog(e);
        }
    }
}

package com.bullhorn.dataloader;

import org.apache.commons.httpclient.HttpClient;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;

import com.bullhorn.dataloader.rest.Cache;
import com.bullhorn.dataloader.rest.CompleteUtil;
import com.bullhorn.dataloader.rest.Preloader;
import com.bullhorn.dataloader.rest.RestApiExtension;
import com.bullhorn.dataloader.rest.RestSession;
import com.bullhorn.dataloader.service.ActionFactory;
import com.bullhorn.dataloader.service.ProcessRunner;
import com.bullhorn.dataloader.util.PrintUtil;
import com.bullhorn.dataloader.util.PropertyFileUtil;
import com.bullhorn.dataloader.util.StringConsts;
import com.bullhorn.dataloader.util.ThreadPoolUtil;
import com.bullhorn.dataloader.util.Timer;

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
            PropertyFileUtil propertyFileUtil = new PropertyFileUtil("dataloader.properties",
                System.getenv(), System.getProperties(), args, printUtil);
            RestApiExtension restApiExtension = new RestApiExtension(printUtil);
            RestSession restSession = new RestSession(restApiExtension, propertyFileUtil, printUtil);
            Preloader preloader = new Preloader(restSession, printUtil);
            CompleteUtil completeUtil = new CompleteUtil(restSession, httpClient, propertyFileUtil, printUtil, timer);
            ThreadPoolUtil threadPoolUtil = new ThreadPoolUtil(propertyFileUtil);
            Cache cache = new Cache(propertyFileUtil);
            ProcessRunner processRunner = new ProcessRunner(restSession, preloader, printUtil, propertyFileUtil,
                threadPoolUtil, cache, completeUtil);
            ActionFactory actionFactory = new ActionFactory(printUtil, propertyFileUtil, completeUtil, restSession,
                processRunner, System.in, timer);

            if (propertyFileUtil.getVerbose()) {
                Configurator.setLevel(PrintUtil.class.getName(), Level.DEBUG);
            }

            CommandLineInterface commandLineInterface = new CommandLineInterface(printUtil, actionFactory);

            args = propertyFileUtil.getRemainingArgs();
            printUtil.recordStart(args);
            commandLineInterface.start(args);
        } catch (Exception e) {
            printUtil.printAndLog(e);
        }
    }
}

package com.bullhorn.dataloader.service;

import com.bullhorn.dataloader.service.api.BullhornAPI;
import com.bullhorn.dataloader.service.consts.Method;
import com.bullhorn.dataloader.service.executor.EntityAttachmentConcurrencyService;
import com.bullhorn.dataloader.service.executor.EntityConcurrencyService;
import com.bullhorn.dataloader.util.CommandLineInterfaceUtil;
import com.bullhorn.dataloader.util.PrintUtil;
import com.bullhorn.dataloader.util.TemplateUtil;
import com.bullhorn.dataloader.util.Timer;
import com.bullhorn.dataloader.util.validation.ValidationUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CommandLineInterface extends CommandLineInterfaceUtil {

    protected Logger log = LogManager.getLogger(CommandLineInterface.class);

    final private Timer timer;
    protected PrintUtil printUtil;
    protected ValidationUtil validationUtil;

    public CommandLineInterface() {
        printUtil = new PrintUtil();
        validationUtil = new ValidationUtil(printUtil);
        timer = new Timer();
    }

    /**
     * Starts the Command Line Interface
     *
     * @param args The user's command line parameters
     */
    public void start(String[] args) {
        log.info("Args: " + String.join(" ", args));

        try {
            if (validationUtil.isValidParameters(args)) {
                if (args.length == 2) {
                    if (args[0].equalsIgnoreCase(Method.TEMPLATES.getMethodName())) {
                        template(args[1]);
                    }
                } else if (args.length == 3) {
                    if (args[0].equalsIgnoreCase(Method.LOAD.getMethodName())) {
                        load(Method.LOAD, args[1], args[2]);
                    }
                    else if (args[0].equalsIgnoreCase(Method.DELETE.getMethodName())) {
                        delete(Method.DELETE, args[1], args[2]);
                    }
                    else if (args[0].equalsIgnoreCase(Method.LOADATTACHMENTS.getMethodName())) {
                        loadAttachments(Method.LOADATTACHMENTS, args[1], args[2]);
                    }
                    else {
                        System.out.println("ERROR: Expected a valid method, but was provided: " + args[0]);
                        printUtil.printUsage();
                    }
                }
            }
        } catch (Exception e) {
            printAndLog(e.toString());
        }
    }

    protected void template(String entityName) throws Exception {
        printAndLog("Creating Template for " + entityName + "...");
        final BullhornAPI bhApi = createSession();
        TemplateUtil templateUtil = new TemplateUtil(bhApi);
        templateUtil.writeExampleEntityCsv(entityName);
        printAndLog("Generated template in " + timer.getDurationStringSec());
    }

    protected void load(Method method, String entityName, String filePath) throws Exception {
        if (validationUtil.isLoadableEntity(entityName) && validationUtil.isValidCsvFile(filePath)) {
            printAndLog("Loading " + entityName + " records from: " + filePath + "...");
            final BullhornAPI bhApi = createSession();
            EntityConcurrencyService concurrencyService = createEntityConcurrencyService(method, entityName, filePath);
            concurrencyService.runLoadProcess();
            printAndLog("Completed setup (establishing connection, retrieving meta, front loading) in " + timer.getDurationStringSec());
        }
    }

    protected void delete(Method method, String entityName, String filePath) throws Exception {
        if (validationUtil.isDeletableEntity(entityName) && validationUtil.isValidCsvFile(filePath)) {
            printAndLog("Deleting " + entityName + " records from: " + filePath + "...");
            final BullhornAPI bhApi = createSession();
            EntityConcurrencyService concurrencyService = createEntityConcurrencyService(method, entityName, filePath);
            concurrencyService.runDeleteProcess();
            printAndLog("Completed setup (establishing connection, retrieving meta) in " + timer.getDurationStringSec());
        }
    }

    protected void loadAttachments(Method method, String entityName, String filePath) throws Exception {
        if (validationUtil.isValidCsvFile(filePath)) {
            System.out.println("Loading " + entityName + " attachments from: " + filePath);
            EntityAttachmentConcurrencyService entityConcurrencyService = createEntityAttachmentConcurrencyService(method, entityName, filePath);
            entityConcurrencyService.runLoadAttchmentProcess();
        }
    }
}

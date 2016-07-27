package com.bullhorn.dataloader.service;

import com.bullhorn.dataloader.service.api.BullhornAPI;
import com.bullhorn.dataloader.service.consts.Method;
import com.bullhorn.dataloader.service.executor.EntityAttachmentConcurrencyService;
import com.bullhorn.dataloader.service.executor.EntityConcurrencyService;
import com.bullhorn.dataloader.util.CommandLineInterfaceUtil;
import com.bullhorn.dataloader.util.PrintUtil;
import com.bullhorn.dataloader.util.TemplateUtil;
import com.bullhorn.dataloader.util.ValidationUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CommandLineInterface extends CommandLineInterfaceUtil {

    protected Logger log = LogManager.getLogger(CommandLineInterface.class);

    protected PrintUtil printUtil;
    protected ValidationUtil validationUtil;

    public CommandLineInterface() {
        printUtil = new PrintUtil();
        validationUtil = new ValidationUtil(printUtil);
    }

    /**
     * Starts the Command Line Interface
     *
     * @param args The user's command line parameters
     */
    public void start(String[] args) {
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
            System.out.println(e);
            log.error(e);
        }
    }

    protected void template(String entityName) throws Exception {
        final BullhornAPI bhApi = createSession();
        TemplateUtil templateUtil = new TemplateUtil(bhApi);
        System.out.println("Creating Template for " + entityName);
        templateUtil.writeExampleEntityCsv(entityName);
    }

    protected void load(Method method, String entityName, String filePath) throws Exception {
        if (validationUtil.isLoadableEntity(entityName) && validationUtil.isValidCsvFile(filePath)) {
            System.out.println("Loading " + entityName + " records from: " + filePath);
            EntityConcurrencyService entityConcurrencyService = createEntityConcurrencyService(method, entityName, filePath);
            entityConcurrencyService.runLoadProcess();
        }
    }

    protected void delete(Method method, String entityName, String filePath) throws Exception {
        if (validationUtil.isDeletableEntity(entityName) && validationUtil.isValidCsvFile(filePath)) {
            System.out.println("Deleting " + entityName + " records from: " + filePath);
            EntityConcurrencyService entityConcurrencyService = createEntityConcurrencyService(method, entityName, filePath);
            entityConcurrencyService.runDeleteProcess();
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

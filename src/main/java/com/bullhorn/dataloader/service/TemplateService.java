package com.bullhorn.dataloader.service;

import com.bullhorn.dataloader.util.CompleteUtil;
import com.bullhorn.dataloader.util.PrintUtil;
import com.bullhorn.dataloader.util.PropertyFileUtil;
import com.bullhorn.dataloader.util.TemplateUtil;
import com.bullhorn.dataloader.util.validation.ValidationUtil;
import com.bullhornsdk.data.api.BullhornData;

import java.io.IOException;
import java.io.InputStream;

/**
 * Create example template implementation
 */
public class TemplateService extends AbstractService implements Action {

    public TemplateService(PrintUtil printUtil,
                           PropertyFileUtil propertyFileUtil,
                           ValidationUtil validationUtil,
                           CompleteUtil completeUtil,
                           InputStream inputStream) throws IOException {
        super(printUtil, propertyFileUtil, validationUtil, completeUtil, inputStream);
    }

    @Override
    public void run(String[] args) {
        try {
            String entityName = validateArguments(args);
            createTemplate(entityName, getBullhornData());
        } catch (Exception e) {
            printUtil.printAndLog("Failed to create REST session.");
            printUtil.printAndLog(e);
        }
    }

    protected void createTemplate(String entityName, BullhornData bullhornData) {
        try {
            printUtil.printAndLog("Creating Template for " + entityName + "...");
            timer.start();
            TemplateUtil templateUtil = new TemplateUtil(bullhornData);
            templateUtil.writeExampleEntityCsv(entityName);
            printUtil.printAndLog("Generated template in " + timer.getDurationStringSec());
        } catch (Exception e) {
            printUtil.printAndLog("Failed to create template for " + entityName);
            printUtil.printAndLog(e);
        }
    }

    protected String validateArguments(String[] args) {
        if (!isValidArguments(args)) {
            throw new IllegalArgumentException("Invalid arguments");
        }

        String entityName = extractEntityNameFromString(args[1]);
        if (entityName == null) {
            throw new IllegalArgumentException("unknown entity");
        }
        return entityName;
    }

    @Override
    public boolean isValidArguments(String[] args) {
        if (!validationUtil.isNumParametersValid(args, 2)) {
            return false;
        }

        String entityName = extractEntityNameFromString(args[1]);
        if (entityName == null) {
            printUtil.printAndLog("Template requested is not valid. " + args[1] + " is not a valid entity.");
            return false;
        }

        return true;
    }
}

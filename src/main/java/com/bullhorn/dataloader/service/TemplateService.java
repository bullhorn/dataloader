package com.bullhorn.dataloader.service;

import com.bullhorn.dataloader.enums.EntityInfo;
import com.bullhorn.dataloader.rest.CompleteUtil;
import com.bullhorn.dataloader.rest.RestApi;
import com.bullhorn.dataloader.rest.RestSession;
import com.bullhorn.dataloader.util.FileUtil;
import com.bullhorn.dataloader.util.PrintUtil;
import com.bullhorn.dataloader.util.PropertyFileUtil;
import com.bullhorn.dataloader.util.TemplateUtil;
import com.bullhorn.dataloader.util.Timer;
import com.bullhorn.dataloader.util.ValidationUtil;

import java.io.File;
import java.io.InputStream;

/**
 * Create example template implementation
 */
public class TemplateService extends AbstractService implements Action {

    TemplateService(PrintUtil printUtil,
                    PropertyFileUtil propertyFileUtil,
                    CompleteUtil completeUtil,
                    RestSession restSession,
                    ProcessRunner processRunner,
                    InputStream inputStream,
                    Timer timer) {
        super(printUtil, propertyFileUtil, completeUtil, restSession, processRunner, inputStream, timer);
    }

    @Override
    public void run(String[] args) {
        RestApi restApi;

        try {
            restApi = restSession.getRestApi();
        } catch (Exception e) {
            printUtil.printAndLog("Failed to create REST session.");
            printUtil.printAndLog(e);
            return;
        }

        String entityNameOrFile = args[1];
        EntityInfo entityInfo = FileUtil.extractEntityFromFileName(entityNameOrFile);
        File file = new File(entityNameOrFile);
        if (file.exists()) {
            compareMetaToExampleFile(entityInfo, file.getPath(), restApi);
        } else {
            createTemplate(entityInfo, restApi);
        }
    }

    @Override
    public boolean isValidArguments(String[] args) {
        return ValidationUtil.validateNumArgs(args, 2, printUtil)
            && ValidationUtil.validateEntityName(args[1], printUtil);
    }

    /**
     * Create a template file by combining the fields available in the SDK with the fields configured in Rest.
     * For custom objects, this allows for only the fields that are configured in the template to be returned,
     * since only those will be visible in the App.
     */
    private void createTemplate(EntityInfo entityInfo, RestApi restApi) {
        try {
            printUtil.printAndLog("Creating Template for " + entityInfo.getEntityName() + "...");
            timer.start();
            TemplateUtil templateUtil = new TemplateUtil(restApi, propertyFileUtil, printUtil);
            templateUtil.writeExampleEntityCsv(entityInfo);
            printUtil.printAndLog("Generated template in " + timer.getDurationStringSec());
        } catch (Exception e) {
            printUtil.printAndLog("ERROR: Failed to create template for " + entityInfo.getEntityName());
            printUtil.printAndLog(e);
        }
    }

    /**
     * Instead of creating a template, compare that template against an existing example CSV file and update
     * that file to include any missing fields and remove any extra fields so that it matches the SDK and Rest.
     */
    private void compareMetaToExampleFile(EntityInfo entityInfo, String exampleFile, RestApi restApi) {
        try {
            printUtil.printAndLog("Comparing latest " + entityInfo.getEntityName() + " meta against example file: "
                + exampleFile + "...");
            TemplateUtil templateUtil = new TemplateUtil(restApi, propertyFileUtil, printUtil);
            templateUtil.compareMetaToExampleFile(entityInfo, exampleFile);
        } catch (Exception e) {
            printUtil.printAndLog("ERROR: Failed to compare meta to example file: " + exampleFile);
            printUtil.printAndLog(e);
        }
    }
}

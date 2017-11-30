package com.bullhorn.dataloader.service;

import com.bullhorn.dataloader.enums.EntityInfo;
import com.bullhorn.dataloader.rest.CompleteUtil;
import com.bullhorn.dataloader.rest.RestApi;
import com.bullhorn.dataloader.rest.RestSession;
import com.bullhorn.dataloader.util.PrintUtil;
import com.bullhorn.dataloader.util.PropertyFileUtil;
import com.bullhorn.dataloader.util.TemplateUtil;
import com.bullhorn.dataloader.util.Timer;
import com.bullhorn.dataloader.util.ValidationUtil;

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
                           RestSession restSession,
                           ProcessRunner processRunner,
                           InputStream inputStream,
                           Timer timer) throws IOException {
        super(printUtil, propertyFileUtil, validationUtil, completeUtil, restSession, processRunner, inputStream, timer);
    }

    @Override
    public void run(String[] args) {
        if (!isValidArguments(args)) {
            throw new IllegalArgumentException("invalid command line arguments");
        }

        RestApi restApi;

        try {
            restApi = restSession.getRestApi();
        } catch (Exception e) {
            printUtil.printAndLog("Failed to create REST session.");
            printUtil.printAndLog(e);
            return;
        }

        EntityInfo entityInfo = EntityInfo.fromString(args[1]);
        createTemplate(entityInfo, restApi);
    }

    @Override
    public boolean isValidArguments(String[] args) {
        if (!validationUtil.isNumParametersValid(args, 2)) {
            return false;
        }

        String entityName = args[1];
        EntityInfo entityInfo = EntityInfo.fromString(entityName);
        if (entityInfo == null) {
            printUtil.printAndLog("ERROR: Template requested is not valid: \"" + entityName
                + "\" is not a valid entity.");
            return false;
        }

        return true;
    }

    private void createTemplate(EntityInfo entityInfo, RestApi restApi) {
        try {
            printUtil.printAndLog("Creating Template for " + entityInfo.getEntityName() + "...");
            timer.start();
            TemplateUtil templateUtil = new TemplateUtil(restApi);
            templateUtil.writeExampleEntityCsv(entityInfo);
            printUtil.printAndLog("Generated template in " + timer.getDurationStringSec());
        } catch (Exception e) {
            printUtil.printAndLog("ERROR: Failed to create template for " + entityInfo.getEntityName());
            printUtil.printAndLog(e);
        }
    }
}

package com.bullhorn.dataloader.service;

import com.bullhorn.dataloader.enums.EntityInfo;
import com.bullhorn.dataloader.rest.CompleteCall;
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
                           CompleteCall completeCall,
                           RestSession restSession,
                           ProcessRunner processRunner,
                           InputStream inputStream,
                           Timer timer) throws IOException {
        super(printUtil, propertyFileUtil, validationUtil, completeCall, restSession, processRunner, inputStream, timer);
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

        EntityInfo entityInfo = validateArguments(args);
        createTemplate(entityInfo, restApi);
    }

    protected void createTemplate(EntityInfo entityInfo, RestApi restApi) {
        try {
            printUtil.printAndLog("Creating Template for " + entityInfo.getEntityName() + "...");
            timer.start();
            TemplateUtil templateUtil = new TemplateUtil(restApi);
            templateUtil.writeExampleEntityCsv(entityInfo.getEntityName());
            printUtil.printAndLog("Generated template in " + timer.getDurationStringSec());
        } catch (Exception e) {
            printUtil.printAndLog("Failed to create template for " + entityInfo);
            printUtil.printAndLog(e);
        }
    }

    protected EntityInfo validateArguments(String[] args) {
        if (!isValidArguments(args)) {
            throw new IllegalArgumentException("Invalid arguments");
        }

        EntityInfo entityInfo = EntityInfo.fromString(args[1]);
        if (entityInfo == null) {
            throw new IllegalArgumentException("unknown entity");
        }
        return entityInfo;
    }

    @Override
    public boolean isValidArguments(String[] args) {
        if (!validationUtil.isNumParametersValid(args, 2)) {
            return false;
        }

        EntityInfo entityInfo = EntityInfo.fromString(args[1]);
        if (entityInfo == null) {
            printUtil.printAndLog("Template requested is not valid. " + args[1] + " is not a valid entity.");
            return false;
        }

        return true;
    }
}

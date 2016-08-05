package com.bullhorn.dataloader.service;

import java.io.IOException;

import com.bullhorn.dataloader.util.PrintUtil;
import com.bullhorn.dataloader.util.TemplateUtil;
import com.bullhornsdk.data.api.BullhornData;

/**
 * Create example template implementation
 */
public class TemplateService extends AbstractService implements Action {

	public TemplateService (PrintUtil printUtil, String propertyFilePath) throws IOException {
		super(printUtil, propertyFilePath);
	}

	@Override
	public void run(String[] args) {
		try {
			String entityName = validateArguments(args);
			createTemplate(entityName, getBullhornData());
		} catch (Exception e) {
			printUtil.printAndLog("Failed to create REST session - " + e.toString());
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
            printUtil.printAndLog("Failed to create template for " + entityName + " - " + e.toString());
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
			printUtil.printAndLog("Could not determine entity from file name: " + entityName);
			return false;
		}

		return true;
	}
}

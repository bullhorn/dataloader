package com.bullhorn.dataloader.service;

import com.bullhorn.dataloader.util.PrintUtil;
import com.bullhorn.dataloader.util.TemplateUtil;

import java.io.IOException;

/**
 * Create example template implementation
 */
public class TemplateService extends AbstractService implements Action {

	public TemplateService (PrintUtil printUtil) throws IOException {
		super(printUtil);
	}

	@Override
	public void run(String[] args) {
		if (!isValidArguments(args)) {
			throw new IllegalArgumentException("Invalid arguments");
		}

		String entityName = extractEntityNameFromString(args[1]);
		if (entityName == null) {
			throw new IllegalArgumentException("unknown entity");
		}

        try {
            printUtil.printAndLog("Creating Template for " + entityName + "...");
        	TemplateUtil templateUtil = getTemplateUtil();
            timer.start();
        	templateUtil.writeExampleEntityCsv(entityName);
			printUtil.printAndLog("Generated template in " + timer.getDurationStringSec());
        } catch (Exception e) {
			printUtil.printAndLog("Failed to create template for " + entityName + " - " + e.toString());
        }
	}

	protected TemplateUtil getTemplateUtil() throws Exception {
		return new TemplateUtil(getBullhornData());
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

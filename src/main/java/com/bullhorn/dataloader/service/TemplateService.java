package com.bullhorn.dataloader.service;

import com.bullhorn.dataloader.service.api.BullhornAPI;
import com.bullhorn.dataloader.util.PrintUtil;
import com.bullhorn.dataloader.util.TemplateUtil;

/**
 * Create example template implementation
 */
public class TemplateService extends AbstractService implements Action {

	public TemplateService (PrintUtil printUtil) {
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
        	final BullhornAPI bhApi = createSession();
        	TemplateUtil templateUtil = new TemplateUtil(bhApi);
            timer.start();
        	templateUtil.writeExampleEntityCsv(entityName);
			printUtil.printAndLog("Generated template in " + timer.getDurationStringSec());
        } catch (Exception e) {
			printUtil.printAndLog("Failed to create template for " + entityName + " - " + e.toString());
        }
	}

	@Override
	public boolean isValidArguments(String[] args) {
		if (args.length == 2) {
			String entityName = extractEntityNameFromString(args[1]);
			if (entityName == null) {
				printUtil.printAndLog("Unknown entity " + args[1]);
				return false;
			}
			return true;
		} else {
			printUtil.printAndLog("Wrong number of arguments");
			return false;
		}
	}
}

package com.bullhorn.dataloader.service;

import com.bullhorn.dataloader.service.executor.EntityConcurrencyService;
import com.bullhorn.dataloader.util.PrintUtil;

/**
 * Delete entity implementation
 */
public class DeleteService extends AbstractService implements Action {

	public DeleteService(PrintUtil printUtil) {
		super(printUtil);
	}

	@Override
	public void run(String[] args) {
		if (!isValidArguments(args)) {
			throw new IllegalStateException("invalid command line arguments");
		}

		String entityName = extractEntityNameFromString(args[1]);
		String fileName = args[2];

		try {
			printUtil.printAndLog("Deleting " + entityName + " records from: " + fileName + "...");
            EntityConcurrencyService concurrencyService = createEntityConcurrencyService(Command.DELETE, entityName, fileName);
            timer.start();
            concurrencyService.runDeleteProcess();
			printUtil.printAndLog("Deleting " + entityName + " records in " + timer.getDurationStringSec());
		} catch (Exception e) {
			printUtil.printAndLog("Failure to delete " + entityName + " = " + e.getMessage());
		}
	}

	@Override
	public boolean isValidArguments(String[] args) {
		
		if (args.length == 3) {
			String entityName = extractEntityNameFromString(args[1]);
			String fileName = args[2];
			
			if (entityName == null) {
				printUtil.printAndLog("Unknown entity " + args[1]);
				return false;
			}
			
			if (fileName == null || fileName.length() == 0) {
				printUtil.printAndLog("Empty file name");
				return false;
			}
			
			return true;
		} else {
			printUtil.printAndLog("Wrong number of arguments");
			return false;
		}
	}
}

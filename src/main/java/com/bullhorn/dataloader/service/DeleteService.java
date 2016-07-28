package com.bullhorn.dataloader.service;

import com.bullhorn.dataloader.service.executor.EntityConcurrencyService;

public class DeleteService extends AbstractService implements Action {

	@Override
	public void run(String[] args) {
		if (!isValidArguments(args)) {
			throw new IllegalStateException("invalid command line arguments");
		}

		String entityName = extractEntityNameFromString(args[1]);
		String fileName = args[2];

		try {
            printAndLog("Deleting " + entityName + " records from: " + fileName + "...");
            EntityConcurrencyService concurrencyService = createEntityConcurrencyService(Command.DELETE, entityName, fileName);
            concurrencyService.runDeleteProcess();
            printAndLog("Completed setup (establishing connection, retrieving meta) in " + timer.getDurationStringSec());			
		} catch (Exception e) {
			printAndLog("Failure to delete " + entityName + " = " + e.getMessage());
		}
	}

	@Override
	public boolean isValidArguments(String[] args) {
		
		if (args.length == 3) {
			String entityName = extractEntityNameFromString(args[1]);
			String fileName = args[2];
			
			if (entityName == null) {
				printAndLog("Unknown entity " + args[1]);
				return false;
			}
			
			if (fileName == null || fileName.length() == 0) {
				printAndLog("Empty file name");
				return false;
			}
			
			return true;
		} else {
			printAndLog("Wrong number of arguments");
			return false;
		}

	}

	@Override
	public void printUsage() {
		printUtil.printUsage();
	}

}

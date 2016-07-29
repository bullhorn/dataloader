package com.bullhorn.dataloader.service;

import com.bullhorn.dataloader.service.executor.EntityConcurrencyService;

public class LoadService extends AbstractService implements Action {

	@Override
	public void run(String[] args) {
		if (!isValidArguments(args)) {
			throw new IllegalStateException("invalid command line arguments");
		}

		String fileName = args[1];
		
		String	entityName = extractEntityNameFromFileName(fileName);
		
		if (entityName == null) {
			throw new IllegalArgumentException("unknown or missing entity");
		}
		
		if (fileName == null || fileName.length() == 0) {
			throw new IllegalArgumentException("missing file name");
		}
		
        try {
            printAndLog("Loading " + entityName + " records from: " + fileName + "...");
        	EntityConcurrencyService concurrencyService = createEntityConcurrencyService(Command.LOAD, entityName, fileName);
        	timer.start();
        	concurrencyService.runLoadProcess();
            printAndLog("Finished loading " + entityName + " in " + timer.getDurationStringSec());
        } catch (Exception e) {
        	printAndLog("Failure starting load: " + e.getMessage());
        }

	}

	@Override
	public boolean isValidArguments(String[] args) {
		
		if (args.length == 2) {
			// Filename only
			String fileName = args[1];
			
			if (fileName == null || fileName.length() == 0) {
				printAndLog("Empty file name");
				return false;
			}
			
			String entityName = extractEntityNameFromFileName(fileName);
			
			if (entityName == null) {
				printAndLog("Could not determine entity from file name: " + fileName);
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

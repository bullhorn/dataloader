package com.bullhorn.dataloader.service;

import java.io.IOException;

import com.bullhorn.dataloader.service.executor.ConcurrencyService;
import com.bullhorn.dataloader.util.PrintUtil;
import com.bullhorn.dataloader.util.PropertyFileUtil;
import com.bullhorn.dataloader.util.validation.ValidationUtil;

/**
 * Load (Insert/Update) service implementation
 */
public class LoadService extends AbstractService implements Action {

	public LoadService(PrintUtil printUtil,
					   PropertyFileUtil propertyFileUtil,
					   ValidationUtil validationUtil) throws IOException {
		super(printUtil, propertyFileUtil, validationUtil);
	}

	@Override
	public void run(String[] args) {
		if (!isValidArguments(args)) {
			throw new IllegalStateException("invalid command line arguments");
		}

		String filePath = args[1];
		String entityName = extractEntityNameFromFileName(filePath);
		if (entityName == null) {
			throw new IllegalArgumentException("unknown or missing entity");
		}
		
        try {
            printUtil.printAndLog("Loading " + entityName + " records from: " + filePath + "...");
			ConcurrencyService concurrencyService = createConcurrencyService(Command.LOAD, entityName, filePath);
        	timer.start();
        	concurrencyService.runLoadProcess();
			printUtil.printAndLog("Finished loading " + entityName + " records in " + timer.getDurationStringHMS());
        } catch (Exception e) {
			printUtil.printAndLog("FAILED to load: " + entityName + " records - " + e.getMessage());
        }
	}

	@Override
	public boolean isValidArguments(String[] args) {
		if (!validationUtil.isNumParametersValid(args, 2)) {
			return false;
		}

		String filePath = args[1];
		if (!validationUtil.isValidCsvFile(args[1])) {
			return false;
		}

		String entityName = extractEntityNameFromFileName(filePath);
		if (entityName == null) {
			printUtil.printAndLog("Could not determine entity from file name: " + filePath);
			return false;
		}

		if (!validationUtil.isLoadableEntity(entityName)) {
			return false;
		}

		return true;
	}
}

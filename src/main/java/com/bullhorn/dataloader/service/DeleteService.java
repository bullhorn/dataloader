package com.bullhorn.dataloader.service;

import java.io.IOException;

import com.bullhorn.dataloader.service.executor.EntityConcurrencyService;
import com.bullhorn.dataloader.util.PrintUtil;

/**
 * Delete service implementation
 */
public class DeleteService extends AbstractService implements Action {

	public DeleteService(PrintUtil printUtil) throws IOException {
		super(printUtil);
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
			printUtil.printAndLog("Deleting " + entityName + " records from: " + filePath + "...");
            EntityConcurrencyService concurrencyService = createEntityConcurrencyService(Command.DELETE, entityName, filePath);
            timer.start();
            concurrencyService.runDeleteProcess();
			printUtil.printAndLog("Finished deleting " + entityName + " records in " + timer.getDurationStringHMS());
		} catch (Exception e) {
			printUtil.printAndLog("FAILED to delete " + entityName + " records - " + e.getMessage());
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

		if (!validationUtil.isDeletableEntity(entityName)) {
			return false;
		}

		return true;
	}
}

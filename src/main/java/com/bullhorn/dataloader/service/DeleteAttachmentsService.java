package com.bullhorn.dataloader.service;

import com.bullhorn.dataloader.service.executor.EntityAttachmentsConcurrencyService;
import com.bullhorn.dataloader.util.PrintUtil;

/**
 * Handles deleting attachments
 */
public class DeleteAttachmentsService extends AbstractService implements Action {

	public DeleteAttachmentsService(PrintUtil printUtil) {
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
			printUtil.printAndLog("Deleting " + entityName + " attachments from: " + filePath + "...");
			EntityAttachmentsConcurrencyService concurrencyService = createEntityAttachmentConcurrencyService(Command.DELETE_ATTACHMENTS, entityName, filePath);
			timer.start();
			concurrencyService.runDeleteAttachmentsProcess();
			printUtil.printAndLog("Finished deleting " + entityName + " in " + timer.getDurationStringSec());
		} catch (Exception e) {
			printUtil.printAndLog("Failure to delete " + entityName + " = " + e.getMessage());
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

		return true;
	}
}

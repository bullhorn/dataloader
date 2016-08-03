package com.bullhorn.dataloader.service;

import java.io.IOException;

import com.bullhorn.dataloader.service.executor.EntityAttachmentsConcurrencyService;
import com.bullhorn.dataloader.util.PrintUtil;

/**
 * Handles loading attachments
 */
public class LoadAttachmentsService extends AbstractService implements Action {

	public LoadAttachmentsService(PrintUtil printUtil) throws IOException {
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
			printUtil.printAndLog("Loading " + entityName + " attachments from: " + filePath + "...");
            EntityAttachmentsConcurrencyService concurrencyService = createEntityAttachmentConcurrencyService(Command.LOAD_ATTACHMENTS, entityName, filePath);
            timer.start();
            concurrencyService.runLoadAttachmentsProcess();
			printUtil.printAndLog("Finished loading " + entityName + " attachments in " + timer.getDurationStringHMS());
		} catch (Exception e) {
			printUtil.printAndLog("FAILED to load " + entityName + " attachments - " + e.getMessage());
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

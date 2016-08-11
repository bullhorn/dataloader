package com.bullhorn.dataloader.service;

import java.io.IOException;
import java.io.InputStream;

import com.bullhorn.dataloader.service.executor.ConcurrencyService;
import com.bullhorn.dataloader.util.PrintUtil;
import com.bullhorn.dataloader.util.PropertyFileUtil;
import com.bullhorn.dataloader.util.validation.ValidationUtil;

/**
 * Handles loading attachments
 */
public class ConvertAttachmentsService extends AbstractService implements Action {

	public ConvertAttachmentsService(PrintUtil printUtil,
									 PropertyFileUtil propertyFileUtil,
									 ValidationUtil validationUtil, InputStream inputStream) throws IOException {
		super(printUtil, propertyFileUtil, validationUtil, inputStream);
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
			printUtil.printAndLog("Converting " + entityName + " attachments from: " + filePath + "...");
            ConcurrencyService concurrencyService = createConcurrencyService(Command.CONVERT_ATTACHMENTS, entityName, filePath);
            timer.start();
            concurrencyService.runConvertAttachmentsProcess();
			printUtil.printAndLog("Finished converting " + entityName + " attachments in " + timer.getDurationStringHMS());
		} catch (Exception e) {
			printUtil.printAndLog("FAILED to convert " + entityName + " attachments");
			printUtil.printAndLog(e);
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

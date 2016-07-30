package com.bullhorn.dataloader.service;

import com.bullhorn.dataloader.service.executor.EntityAttachmentConcurrencyService;
import com.bullhorn.dataloader.util.PrintUtil;

/**
 * Handles loading attachments
 */
public class LoadAttachmentsService extends AbstractService implements Action {

	public LoadAttachmentsService(PrintUtil printUtil) {
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
			printUtil.printAndLog("Loading " + entityName + " attachment from: " + fileName + "...");
            EntityAttachmentConcurrencyService concurrencyService = createEntityAttachmentConcurrencyService(Command.LOAD_ATTACHMENTS, entityName, fileName);
            timer.start();
            concurrencyService.runLoadAttachmentProcess();
			printUtil.printAndLog("Finished loading " + entityName + " in " + timer.getDurationStringSec());
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

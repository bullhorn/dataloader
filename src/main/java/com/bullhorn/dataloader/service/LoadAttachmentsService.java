package com.bullhorn.dataloader.service;

import java.io.IOException;
import java.io.InputStream;

import com.bullhorn.dataloader.service.executor.ConcurrencyService;
import com.bullhorn.dataloader.util.PrintUtil;
import com.bullhorn.dataloader.util.PropertyFileUtil;
import com.bullhorn.dataloader.util.validation.ValidationUtil;
import com.bullhornsdk.data.model.entity.core.standard.Candidate;
import com.bullhornsdk.data.model.entity.core.standard.ClientContact;
import com.bullhornsdk.data.model.entity.core.standard.ClientCorporation;
import com.bullhornsdk.data.model.entity.core.standard.JobOrder;
import com.bullhornsdk.data.model.entity.core.standard.Opportunity;
import com.bullhornsdk.data.model.entity.core.standard.Placement;

/**
 * Handles loading attachments
 */
public class LoadAttachmentsService extends AbstractService implements Action {

	public LoadAttachmentsService(PrintUtil printUtil,
								  PropertyFileUtil propertyFileUtil,
								  ValidationUtil validationUtil,
								  InputStream inputStream) throws IOException {
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
			printUtil.printAndLog("Loading " + entityName + " attachments from: " + filePath + "...");
            ConcurrencyService concurrencyService = createConcurrencyService(Command.LOAD_ATTACHMENTS, entityName, filePath);
            timer.start();
            concurrencyService.runLoadAttachmentsProcess();
			printUtil.printAndLog("Finished loading " + entityName + " attachments in " + timer.getDurationStringHMS());
		} catch (Exception e) {
			printUtil.printAndLog("FAILED to load " + entityName + " attachments");
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

        if (!isValidAttachmentEntity(entityName)) {
            printUtil.printAndLog("loadAttachments not available for " + entityName.toLowerCase());
            return false;
        }

		return true;
	}

    /**
     * checks if entity can load attachments
     *
     */
    protected boolean isValidAttachmentEntity(String entityName) {
        if (entityName.equalsIgnoreCase(Candidate.class.getSimpleName())
            || entityName.equalsIgnoreCase(ClientContact.class.getSimpleName())
            || entityName.equalsIgnoreCase(ClientCorporation.class.getSimpleName())
            || entityName.equalsIgnoreCase(JobOrder.class.getSimpleName())
            || entityName.equalsIgnoreCase(Opportunity.class.getSimpleName())
            || entityName.equalsIgnoreCase(Placement.class.getSimpleName())
            ) {
            return true;
        }
        else return false;
    }
}

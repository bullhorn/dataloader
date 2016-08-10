package com.bullhorn.dataloader.service;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import com.bullhorn.dataloader.meta.Entity;
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

		// Creating list of all files in the specified directory
		String filePath = args[1];
		SortedMap<Entity, List<String>> entityToFileListMap = getLoadableCsvFilesFromPath(filePath);
		for (Map.Entry<Entity, List<String>> entityFileEntry : entityToFileListMap.entrySet()) {
			String entityName = entityFileEntry.getKey().getEntityName();
            for (String fileName : entityFileEntry.getValue()) {
                try {
                    printUtil.printAndLog("Loading " + entityName + " records from: " + fileName + "...");
                    ConcurrencyService concurrencyService = createConcurrencyService(Command.LOAD, entityName, fileName);
                    timer.start();
                    concurrencyService.runLoadProcess();
                    printUtil.printAndLog("Finished loading " + entityName + " records in " + timer.getDurationStringHMS());
                } catch (Exception e) {
                    printUtil.printAndLog("FAILED to load: " + entityName + " records - " + e.getMessage());
                }
            }
		}
	}

	@Override
	public boolean isValidArguments(String[] args) {
		if (!validationUtil.isNumParametersValid(args, 2)) {
			return false;
		}

		String filePath = args[1];
		File file = new File(filePath);
		if (file.isDirectory()) {
			if (getLoadableCsvFilesFromPath(filePath).isEmpty()) {
                printUtil.printAndLog("ERROR: Could not find any valid CSV files (with entity name) to load from directory: " + filePath);
				return false;
			}
		} else {
			if (!validationUtil.isValidCsvFile(filePath)) {
				return false;
			}

			String entityName = extractEntityNameFromFileName(filePath);
			if (entityName == null) {
				printUtil.printAndLog("ERROR: Could not determine entity from file name: " + filePath);
				return false;
			}

			if (!validationUtil.isLoadableEntity(entityName)) {
				return false;
			}
		}

		return true;
	}

    /**
     * Returns the list of loadable csv files
     * @param filePath The given file or directory
     * @return the subset of getValidCsvFilesFromPath that are loadable
     */
	private SortedMap<Entity, List<String>> getLoadableCsvFilesFromPath(String filePath) {
        SortedMap<Entity, List<String>> loadableEntityToFileListMap = new TreeMap<>();

        SortedMap<Entity, List<String>> entityToFileListMap = getValidCsvFilesFromPath(filePath);
        for (Map.Entry<Entity, List<String>> entityFileEntry : entityToFileListMap.entrySet()) {
            String entityName = entityFileEntry.getKey().getEntityName();
            if (validationUtil.isLoadableEntity(entityName, false)) {
                loadableEntityToFileListMap.put(entityFileEntry.getKey(), entityFileEntry.getValue());
            }
        }

        return loadableEntityToFileListMap;
    }
}

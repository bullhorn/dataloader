package com.bullhorn.dataloader.service;


import com.bullhorn.dataloader.meta.Entity;
import com.bullhorn.dataloader.service.csv.CsvFileWriter;
import com.bullhorn.dataloader.service.executor.ConcurrencyService;
import com.bullhorn.dataloader.util.ActionTotals;
import com.bullhorn.dataloader.util.PrintUtil;
import com.bullhorn.dataloader.util.PropertyFileUtil;
import com.bullhorn.dataloader.util.Timer;
import com.bullhorn.dataloader.util.validation.ValidationUtil;
import com.bullhornsdk.data.api.BullhornData;
import com.bullhornsdk.data.api.BullhornRestCredentials;
import com.bullhornsdk.data.api.StandardBullhornData;
import com.csvreader.CsvReader;
import com.google.common.collect.Sets;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Base class for all command line actions.
 *
 * Contains common functionality.
 */
public abstract class AbstractService {

    final protected Timer timer;
    final protected PrintUtil printUtil;
    final protected ValidationUtil validationUtil;
    final protected PropertyFileUtil propertyFileUtil;

    public AbstractService(PrintUtil printUtil) throws IOException {
    	this.printUtil = printUtil;
    	validationUtil = new ValidationUtil(printUtil);
        propertyFileUtil = new PropertyFileUtil("dataloader.properties");
    	timer = new Timer();
    }

    public PropertyFileUtil getPropertyFileUtil() {
        return propertyFileUtil;
    }

    protected BullhornData getBullhornData() throws Exception {
        BullhornData bullhornData = new StandardBullhornData(getBullhornRestCredentials(getPropertyFileUtil()));
        return bullhornData;
    }

    protected BullhornRestCredentials getBullhornRestCredentials(PropertyFileUtil propertyFileUtil) throws Exception {
        BullhornRestCredentials bullhornRestCredentials = new BullhornRestCredentials();
        bullhornRestCredentials.setPassword(propertyFileUtil.getPassword());
        bullhornRestCredentials.setRestAuthorizeUrl(propertyFileUtil.getAuthorizeUrl());
        bullhornRestCredentials.setRestClientId(propertyFileUtil.getClientId());
        bullhornRestCredentials.setRestClientSecret(propertyFileUtil.getClientSecret());
        bullhornRestCredentials.setRestLoginUrl(propertyFileUtil.getLoginUrl());
        bullhornRestCredentials.setRestTokenUrl(propertyFileUtil.getTokenUrl());
        bullhornRestCredentials.setUsername(propertyFileUtil.getUsername());
        bullhornRestCredentials.setRestSessionMinutesToLive("60");
        return bullhornRestCredentials;
    }

    /**
     * Create a thread pool executor service for processing entities
     *
     * @param propertyFileUtil - properties for the thread pool
     * @return java.util.concurrent.ExecutorService
     */
    protected ExecutorService getExecutorService(PropertyFileUtil propertyFileUtil) {
        return Executors.newFixedThreadPool(propertyFileUtil.getNumThreads());
    }

    /**
     * Create thread pool for processing entityClass attachment changes
     *
     * @param command - command line action to perform
     * @param entityName - entityClass name
     * @param filePath - CSV file with attachment data
     * @return ConcurrencyService thread pool service
     * @throws Exception if error when opening session, loading entityClass data, or reading CSV
     */
    protected ConcurrencyService createConcurrencyService(Command command, String entityName, String filePath) throws Exception {
        final PropertyFileUtil propertyFileUtil = getPropertyFileUtil();

        final BullhornData bullhornData = getBullhornData();
        final ExecutorService executorService = getExecutorService(propertyFileUtil);
        final CsvReader csvReader = getCsvReader(filePath);
        final CsvFileWriter csvFileWriter = new CsvFileWriter(command, filePath, csvReader.getHeaders());
        ActionTotals actionTotals = new ActionTotals();

        ConcurrencyService concurrencyService = new ConcurrencyService(
        		command,
                entityName,
                csvReader,
                csvFileWriter,
                executorService,
                propertyFileUtil,
                bullhornData,
                printUtil,
                actionTotals
        );

        return concurrencyService;
    }

    /**
     * Extractions entity name from a file path.
     *
     * The file name must start with the name of the entity
     *
     * @param fileName path from which to extract entity name
     * @return the SDK-Rest name of the entity, or null if not found
     */
	protected String extractEntityNameFromFileName(String fileName) {
		File file = new File(fileName);

		String upperCaseFileName = file.getName().toUpperCase();
		Entity bestMatch = null;
		for (Entity entity: Entity.values()) {
			if (upperCaseFileName.startsWith(entity.getUpperCase())) {
				if (bestMatch == null) {
					bestMatch = entity;
				} else if (bestMatch.getEntityName().length() < entity.getEntityName().length()) {
					// longer name is better
					bestMatch = entity;
				}
			}
		}

		if (bestMatch == null) {
			return null;
		} else {
			return bestMatch.getEntityName();
		}
	}

	/**
	 * Return properly capitalize SDK-REST entity name from a string with any capitalization
	 *
	 * @param string a string of the entity name
	 * @return SDK-REST entity name
	 */
	protected String extractEntityNameFromString(String string) {
		for (Entity entity: Entity.values()) {
			if (string.equalsIgnoreCase(entity.getEntityName())) {
				return entity.getEntityName();
			}
		}
		return null;
	}

    private CsvReader getCsvReader(String filePath) throws IOException {
        final CsvReader csvReader = new CsvReader(filePath);
        csvReader.readHeaders();
        if (Arrays.asList(csvReader.getHeaders()).size() != Sets.newHashSet(csvReader.getHeaders()).size()){
            throw new IllegalStateException("Provided CSV file contains duplicate headers");
        }
        return csvReader;
    }
}

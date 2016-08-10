package com.bullhorn.dataloader.service;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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

    public AbstractService(PrintUtil printUtil,
                           PropertyFileUtil propertyFileUtil,
                           ValidationUtil validationUtil) throws IOException {
    	this.printUtil = printUtil;
    	this.validationUtil = validationUtil;
        this.propertyFileUtil = propertyFileUtil;
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
     * Given a file or directory, this method will determine all valid CSV files that can be used by DataLoader and
     * collect them into a list indexed by the entity that they correspond to based on the filename. For a filename
     * argument, this lits will be either empty or contain exactly one matching entity to filename.
     *
     * @param filePath any file or directory
     * @return a Map of entity enums to lists of valid files.
     */
    protected SortedMap<Entity, List<String>> getValidCsvFilesFromPath(String filePath) {
        SortedMap<Entity, List<String>> entityToFileListMap = new TreeMap<>(Entity.loadOrderComparator);

        File file = new File(filePath);
        if (file.isDirectory()) {
            for (String fileName : file.list()) {
                String absoluteFilePath = file.getAbsolutePath() + File.separator + fileName;
                if (validationUtil.isValidCsvFile(absoluteFilePath, false)) {
                    Entity entity = extractEntityFromFileName(fileName);
                    if (entity != null) {
                        if (!entityToFileListMap.containsKey(entity)) {
                            entityToFileListMap.put(entity, new ArrayList<>());
                        }
                        List<String> files = entityToFileListMap.get(entity);
                        files.add(absoluteFilePath);
                    }
                }
            }
        } else {
            if (validationUtil.isValidCsvFile(filePath, false)) {
                Entity entity = extractEntityFromFileName(filePath);
                if (entity != null) {
                    entityToFileListMap.put(entity, Arrays.asList(filePath));
                }
            }
        }

        return entityToFileListMap;
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
        Entity bestMatch = null;
        bestMatch = extractEntityFromFileName(fileName);

        if (bestMatch == null) {
            return null;
        } else {
            return bestMatch.getEntityName();
        }
    }

    /**
     * Extractions entity type from a file path.
     *
     * The file name must start with the name of the entity
     *
     * @param fileName path from which to extract entity name
     * @return the SDK-Rest entity, or null if not found
     */
    protected Entity extractEntityFromFileName(String fileName) {
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
            return bestMatch;
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
            StringBuilder sb = getDuplicates(csvReader);
            throw new IllegalStateException("Provided CSV file contains the following duplicate headers:\n" + sb.toString());
        }
        return csvReader;
    }

    private StringBuilder getDuplicates(CsvReader csvReader) throws IOException {
        List<String> nonDupe = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        for (String header : csvReader.getHeaders()){
            if (nonDupe.contains(header)){
                sb.append("\t" + header + "\n");
            }
            nonDupe.add(header);
        }
        return sb;
    }
}

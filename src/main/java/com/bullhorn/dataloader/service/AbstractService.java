package com.bullhorn.dataloader.service;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

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
import com.bullhornsdk.data.model.entity.core.standard.Candidate;
import com.bullhornsdk.data.model.entity.core.standard.ClientContact;
import com.bullhornsdk.data.model.entity.core.standard.ClientCorporation;
import com.bullhornsdk.data.model.entity.core.standard.JobOrder;
import com.bullhornsdk.data.model.entity.core.standard.Opportunity;
import com.bullhornsdk.data.model.entity.core.standard.Placement;
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
    final protected PropertyFileUtil propertyFileUtil;
    final protected ValidationUtil validationUtil;
    final protected InputStream inputStream;

    public AbstractService(PrintUtil printUtil,
                           PropertyFileUtil propertyFileUtil,
                           ValidationUtil validationUtil,
                           InputStream inputStream) throws IOException {
    	this.printUtil = printUtil;
        this.propertyFileUtil = propertyFileUtil;
        this.validationUtil = validationUtil;
        this.inputStream = inputStream;
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
        final BlockingQueue taskPoolSize = new ArrayBlockingQueue(getTaskPoolSize());
        return new ThreadPoolExecutor(propertyFileUtil.getNumThreads(), propertyFileUtil.getNumThreads(), 10, TimeUnit.SECONDS, taskPoolSize, new ThreadPoolExecutor.CallerRunsPolicy());
    }

    /**
     * Gets task pool size limit on basis of system memory
     *
     * @return task pool size limit
     */
    protected int getTaskPoolSize() {
        long memorySize = ((com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean()).getTotalPhysicalMemorySize() / 1024;
        if(memorySize < 16456252) {
            return 1000;
        }
        return 10000;
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
     * @param comparator specifies how the sorted list should be sorted
     * @return a Map of entity enums to lists of valid files.
     */
    protected SortedMap<Entity, List<String>> getValidCsvFiles(String filePath, Comparator<Entity> comparator) {
        File file = new File(filePath);
        if (file.isDirectory()) {
            return getValidCsvFilesFromDirectory(file, comparator);
        } else {
            return getValidCsvFilesFromFilePath(filePath, comparator);
        }
    }

    /**
     * Given a directory, this method searches the directory for all valid CSV files and returns the map
     */
    private SortedMap<Entity, List<String>> getValidCsvFilesFromDirectory(File directory, Comparator<Entity> comparator) {
        SortedMap<Entity, List<String>> entityToFileListMap = new TreeMap<>(comparator);

        for (String fileName : directory.list()) {
            String absoluteFilePath = directory.getAbsolutePath() + File.separator + fileName;
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

        return entityToFileListMap;
    }

    /**
     * Given an individual file path, this method constructs the entity to file map and returns it.
     */
    private SortedMap<Entity, List<String>> getValidCsvFilesFromFilePath(String filePath, Comparator<Entity> comparator) {
        SortedMap<Entity, List<String>> entityToFileListMap = new TreeMap<>(comparator);

        if (validationUtil.isValidCsvFile(filePath, false)) {
            Entity entity = extractEntityFromFileName(filePath);
            if (entity != null) {
                entityToFileListMap.put(entity, Arrays.asList(filePath));
            }
        }

        return entityToFileListMap;
    }

    /**
     * Returns the list of loadable csv files in load order
     *
     * @param filePath The given file or directory
     * @return the subset of getValidCsvFiles that are loadable
     */
    protected SortedMap<Entity, List<String>> getLoadableCsvFilesFromPath(String filePath) {
        SortedMap<Entity, List<String>> loadableEntityToFileListMap = new TreeMap<>(Entity.loadOrderComparator);

        SortedMap<Entity, List<String>> entityToFileListMap = getValidCsvFiles(filePath, Entity.loadOrderComparator);
        for (Map.Entry<Entity, List<String>> entityFileEntry : entityToFileListMap.entrySet()) {
            String entityName = entityFileEntry.getKey().getEntityName();
            if (validationUtil.isLoadableEntity(entityName, false)) {
                loadableEntityToFileListMap.put(entityFileEntry.getKey(), entityFileEntry.getValue());
            }
        }

        return loadableEntityToFileListMap;
    }

    /**
     * Returns the list of deletable csv files in delete order
     *
     * @param filePath The given file or directory
     * @return the subset of getValidCsvFiles that are deletable
     */
    protected SortedMap<Entity, List<String>> getDeletableCsvFilesFromPath(String filePath) {
        SortedMap<Entity, List<String>> deletableEntityToFileListMap = new TreeMap<>(Entity.deleteOrderComparator);

        SortedMap<Entity, List<String>> entityToFileListMap = getValidCsvFiles(filePath, Entity.deleteOrderComparator);
        for (Map.Entry<Entity, List<String>> entityFileEntry : entityToFileListMap.entrySet()) {
            String entityName = entityFileEntry.getKey().getEntityName();
            if (validationUtil.isDeletableEntity(entityName, false)) {
                deletableEntityToFileListMap.put(entityFileEntry.getKey(), entityFileEntry.getValue());
            }
        }

        return deletableEntityToFileListMap;
    }

    /**
     * When loading from directory, give the user a chance to hit ENTER or CTRL+C once they see all the files about
     * to be processed. Handles the case where there are multiple entities with multiple files or one entity with
     * multiple files.
     *
     * @param filePath The user provided directory where these files came from
     * @param entityToFileListMap The list of files that will be loaded
     * @return true if the user has responded with yes, false if no
     */
    protected Boolean promptUserForMultipleFiles(String filePath, SortedMap<Entity, List<String>> entityToFileListMap) {
        if (entityToFileListMap.size() > 1 ||
                (!entityToFileListMap.isEmpty() &&
                        entityToFileListMap.get(entityToFileListMap.firstKey()).size() > 1)) {
            printUtil.printAndLog("Ready to process the following CSV files from the " + filePath + " directory in the following order:");

            Integer count = 1;
            for (Map.Entry<Entity, List<String>> entityFileEntry : entityToFileListMap.entrySet()) {
                String entityName = entityFileEntry.getKey().getEntityName();
                for (String fileName : entityFileEntry.getValue()) {
                    File file = new File(fileName);
                    printUtil.printAndLog("   " + count++ + ". " + entityName + " records from " + file.getName());
                }
            }

            printUtil.print("Do you want to continue? [Y/N]");
            Scanner scanner = new Scanner(inputStream);
            Boolean yesOrNoResponse = false;
            while (!yesOrNoResponse) {
                String input = scanner.nextLine();
                if (input.startsWith("y") || input.startsWith("Y")) {
                    yesOrNoResponse = true;
                } else if (input.startsWith("n") || input.startsWith("N")) {
                    return false;
                }
            };
        }

        return true;
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

        return false;
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

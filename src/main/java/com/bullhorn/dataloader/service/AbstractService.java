package com.bullhorn.dataloader.service;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.lang3.text.WordUtils;

import com.bullhorn.dataloader.meta.Entity;
import com.bullhorn.dataloader.meta.MetaMap;
import com.bullhorn.dataloader.service.api.BullhornAPI;
import com.bullhorn.dataloader.service.api.BullhornApiAssociator;
import com.bullhorn.dataloader.service.api.BullhornApiUpdater;
import com.bullhorn.dataloader.service.csv.CsvFileReader;
import com.bullhorn.dataloader.service.csv.CsvFileWriter;
import com.bullhorn.dataloader.service.csv.Result;
import com.bullhorn.dataloader.service.executor.EntityAttachmentConcurrencyService;
import com.bullhorn.dataloader.service.executor.EntityConcurrencyService;
import com.bullhorn.dataloader.service.query.EntityCache;
import com.bullhorn.dataloader.service.query.EntityQuery;
import com.bullhorn.dataloader.util.ActionTotals;
import com.bullhorn.dataloader.util.PrintUtil;
import com.bullhorn.dataloader.util.PropertyFileUtil;
import com.bullhorn.dataloader.util.Timer;
import com.bullhorn.dataloader.util.validation.ValidationUtil;
import com.bullhornsdk.data.api.BullhornData;
import com.bullhornsdk.data.api.BullhornRestCredentials;
import com.bullhornsdk.data.api.StandardBullhornData;
import com.csvreader.CsvReader;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.LoadingCache;

/**
 * Base class for all command line actions.
 *
 * Contains common functionality.
 */
public abstract class AbstractService {

    final protected Timer timer;
    final protected PrintUtil printUtil;
    final protected ValidationUtil validationUtil;

    public AbstractService(PrintUtil printUtil) {
    	this.printUtil = printUtil;
    	validationUtil = new ValidationUtil(printUtil);
    	timer = new Timer();
    }

    private BullhornData getBullhornData(PropertyFileUtil propertyFileUtil) throws Exception {
        BullhornData bullhornData = new StandardBullhornData(getBullhornRestCredentials(propertyFileUtil));
        return bullhornData;
    }

    private BullhornRestCredentials getBullhornRestCredentials(PropertyFileUtil propertyFileUtil) throws Exception {
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

    private PropertyFileUtil getPropertyFileUtil() throws IOException {
        return new PropertyFileUtil("dataloader.properties");
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
     * Create BullhornAPI instance and open a session
     *
     * @return BullhornAPI instance
     * @throws Exception if error reading in properties or error creating session
     */
    protected BullhornAPI createSession() throws Exception {
        final PropertyFileUtil propertyFileUtil = getPropertyFileUtil();
        final BullhornAPI bhApi = new BullhornAPI(propertyFileUtil);
        bhApi.createSession();
        return bhApi;
    }

    /**
     * Create thread pool for processing entity changes
     *
     * @param command - command line action to perform
     * @param entity - entity name
     * @param filePath - CSV file with entity data
     * @return EntityConcurrencyService thread pool service
     * @throws Exception if error when opening session, loading entity data, or reading CSV
     */
    protected EntityConcurrencyService createEntityConcurrencyService(Command command, String entity, String filePath) throws Exception {
        final BullhornAPI bhApi = createSession();
        final BullhornApiUpdater bhApiUpdater = new BullhornApiUpdater(bhApi);
        final BullhornApiAssociator bhApiAssociator = new BullhornApiAssociator(bhApi);
        final LoadingCache<EntityQuery, Result> associationCache = CacheBuilder.newBuilder()
                .maximumSize(bhApi.getPropertyFileUtil().getCacheSize())
                .build(new EntityCache(bhApiUpdater));

        bhApi.frontLoad();
        MetaMap metaMap = bhApi.getRootMetaDataTypes(entity);
        final CsvFileReader csvFileReader = new CsvFileReader(filePath, metaMap);
        final CsvFileWriter csvFileWriter = new CsvFileWriter(command, filePath, csvFileReader.getHeaders());

        final ExecutorService executorService = getExecutorService(getPropertyFileUtil());
        final EntityConcurrencyService entityConcurrencyService = new EntityConcurrencyService(
                WordUtils.capitalize(entity),
                csvFileReader,
                csvFileWriter,
                bhApi,
                bhApiAssociator,
                executorService,
                associationCache,
                bhApi.getPropertyFileUtil()
        );

        return entityConcurrencyService;
    }

    /**
     * Create thread pool for processing entity attachment changes
     *
     * @param command - command line action to perform
     * @param entityName - entity name
     * @param filePath - CSV file with attachment data
     * @return EntityAttachmentConcurrencyService thread pool service
     * @throws Exception if error when opening session, loading entity data, or reading CSV
     */
    protected EntityAttachmentConcurrencyService createEntityAttachmentConcurrencyService(Command command, String entityName, String filePath) throws Exception {
        final PropertyFileUtil propertyFileUtil = getPropertyFileUtil();

        final BullhornData bullhornData = getBullhornData(propertyFileUtil);
        final ExecutorService executorService = getExecutorService(propertyFileUtil);
        final CsvReader csvReader = new CsvReader(filePath);
        csvReader.readHeaders();
        final CsvFileWriter csvFileWriter = new CsvFileWriter(command, filePath, csvReader.getHeaders());
        ActionTotals actionTotals = new ActionTotals();

        EntityAttachmentConcurrencyService entityAttachmentConcurrencyService = new EntityAttachmentConcurrencyService(
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

        return entityAttachmentConcurrencyService;
    }

    /**
     * Extractions entity name from a file path.
     *
     * The entity name is the prefix of the tail of the file name.
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
}

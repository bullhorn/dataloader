package com.bullhorn.dataloader.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang.WordUtils;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.bullhorn.dataloader.enums.EntityInfo;
import com.bullhorn.dataloader.enums.ErrorInfo;
import com.bullhorn.dataloader.enums.Property;
import com.google.common.base.CaseFormat;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * Wrapper around the properties that handles all interaction with properties throughout a session.
 */
public class PropertyFileUtil {

    private final PrintUtil printUtil;
    private String[] remainingArgs;

    // Property values in the properties file:
    private String username;
    private String password;
    private String clientId;
    private String clientSecret;
    private String authorizeUrl;
    private String tokenUrl;
    private String loginUrl;
    private Map<String, List<String>> entityExistFieldsMap = Maps.newHashMap();
    private Map<String, String> columnNameMap = Maps.newHashMap();
    private String listDelimiter;
    private DateTimeFormatter dateParser;
    private Boolean processEmptyAssociations;
    private Boolean wildcardMatching;
    private Boolean singleByteEncoding;
    private Boolean skipDuplicates;
    private Boolean executeFormTriggers;
    private Integer numThreads;
    private Boolean caching;

    // Property values for developers only:
    private EntityInfo entity;
    private Boolean resultsFileEnabled;
    private String resultsFilePath;
    private Integer resultsFileWriteIntervalMsec;
    private Integer waitSecondsBetweenFilesInDirectory;
    private Boolean verbose;

    /**
     * Constructor that assembles the dataloader properties from a variety of possible methods.
     *
     * @param fileName         the property filename to load
     * @param envVars          the environment variables to use (overrides the properties file)
     * @param systemProperties the system properties to use (overrides the properties file and envVars)
     * @param args             the command line arguments (overrides all others)
     * @param printUtil        for logging properties
     * @throws DataLoaderException for property file not found
     */
    public PropertyFileUtil(String fileName,
                            Map<String, String> envVars,
                            Properties systemProperties,
                            String[] args,
                            PrintUtil printUtil) throws DataLoaderException {
        this.printUtil = printUtil;

        // If the users has specified a -Dpropertyfile command line parameter, use that fileName instead
        if (null != systemProperties.getProperty(StringConsts.PROPERTY_FILE_ARG)) {
            fileName = systemProperties.getProperty(StringConsts.PROPERTY_FILE_ARG);
        }

        // Read in from file and environment variables
        Properties fileProperties = getFileProperties(fileName);
        Properties envVarProperties = getEnvVarProperties(envVars);
        Properties validSystemProperties = getValidProperties(systemProperties);
        Properties argumentProperties = getArgumentProperties(args);

        // Combine the properties into one
        Properties properties = new Properties();
        properties.putAll(fileProperties);
        properties.putAll(envVarProperties);
        properties.putAll(validSystemProperties);
        properties.putAll(argumentProperties);

        // Process and log
        processProperties(properties);
        logProperties(fileName, properties);
    }

    /**
     * Returns a relative path that can be used to persist converted attachments (documents that have been parsed into
     * HTML files) to disk so that they can be later retrieved and added to the description field of entities when
     * loading.
     *
     * @param entityInfo the entity type
     * @param externalId the entity's externalID
     * @return a string containing the filepath to read to or write from
     */
    public String getConvertedAttachmentFilepath(EntityInfo entityInfo, String externalId) {
        return StringConsts.CONVERTED_ATTACHMENTS_DIRECTORY + "/" + entityInfo.getEntityName() + "/" + externalId + ".html";
    }

    /**
     * Returns the arguments list after the property arguments have been parsed out
     *
     * @return The command line arguments without the given property arguments
     */
    public String[] getRemainingArgs() {
        return remainingArgs;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getClientId() {
        return clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public String getAuthorizeUrl() {
        return authorizeUrl;
    }

    public String getTokenUrl() {
        return tokenUrl;
    }

    public String getLoginUrl() {
        return loginUrl;
    }

    /**
     * Returns the list of entity exist fields for a given entity.
     *
     * @param entityInfo The entity type
     * @return The list of field names if they exist, an empty list otherwise
     */
    public List<String> getEntityExistFields(EntityInfo entityInfo) {
        List<String> entityExistFields = entityExistFieldsMap.get(entityInfo.getEntityName());
        return entityExistFields == null ? Lists.newArrayList() : entityExistFields;
    }

    /**
     * Returns true if the given column name is mapped to a different column name.
     */
    public Boolean hasColumnNameMapping(String columnName) {
        return columnNameMap.containsKey(columnName.toLowerCase());
    }

    /**
     * If the given column name is mapped to a different column name, returns the mapped name.
     *
     * @param columnName The user provided name of the column
     * @return The mapped column name if it exists, otherwise the given name.
     */
    public String getColumnNameMapping(String columnName) {
        return columnNameMap.getOrDefault(columnName.toLowerCase(), columnName);
    }

    public String getListDelimiter() {
        return listDelimiter;
    }

    public DateTimeFormatter getDateParser() {
        return dateParser;
    }

    public Integer getNumThreads() {
        return numThreads;
    }

    public Integer getWaitSecondsBetweenFilesInDirectory() {
        return waitSecondsBetweenFilesInDirectory;
    }

    public Boolean getProcessEmptyAssociations() {
        return processEmptyAssociations;
    }

    public Boolean getWildcardMatching() {
        return wildcardMatching;
    }

    public Boolean getSingleByteEncoding() {
        return singleByteEncoding;
    }

    public Boolean getSkipDuplicates() {
        return skipDuplicates;
    }

    public Boolean getExecuteFormTriggers() {
        return executeFormTriggers;
    }

    public EntityInfo getEntity() {
        return entity;
    }

    public Boolean getResultsFileEnabled() {
        return resultsFileEnabled;
    }

    public String getResultsFilePath() {
        return resultsFilePath;
    }

    public Integer getResultsFileWriteIntervalMsec() {
        return resultsFileWriteIntervalMsec;
    }

    public Boolean getVerbose() {
        return verbose;
    }

    public Boolean getCaching() {
        return caching;
    }

    /**
     * Parses the given filename to pull out properties
     *
     * @param fileName the name of the file to parse
     * @return the properties
     * @throws DataLoaderException for File not found
     */
    private Properties getFileProperties(String fileName) throws DataLoaderException {
        try {
            FileInputStream fileInputStream = new FileInputStream(fileName);
            Properties properties = new Properties();
            properties.load(fileInputStream);
            fileInputStream.close();
            return properties;
        } catch (IOException exception) {
            throw new DataLoaderException(ErrorInfo.MISSING_PROPERTIES_FILE, "Cannot read the properties file: " + fileName);
        }
    }

    /**
     * Parses the environment variables to pull out DataLoader specific properties
     *
     * Environment Variables must start with "DATALOADER_" in order to be used, and the log will show if an environment
     * variable has been used to override values from the property file.
     *
     * @param envVars the map of environment variables and their values
     * @return the valid properties
     */
    private Properties getEnvVarProperties(Map<String, String> envVars) {
        Properties properties = new Properties();
        for (Map.Entry<String, String> envVar : envVars.entrySet()) {
            String key = envVar.getKey();
            String value = envVar.getValue();
            if (key.startsWith(StringConsts.DATALOADER_PREFIX)) {
                String name = CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, key.split(StringConsts.DATALOADER_PREFIX)[1]);
                Property propertyEnum = Property.fromString(name);
                if (propertyEnum != null) {
                    properties.setProperty(propertyEnum.getName(), value);
                    printUtil.log("Using Environment Variable '" + key + "' to Override Property File Value");
                } else if (name.endsWith(StringConsts.EXIST_FIELD_SUFFIX) || name.endsWith(StringConsts.COLUMN_NAME_ALIAS_SUFFIX)) {
                    properties.setProperty(name, value);
                    printUtil.log("Using Environment Variable '" + key + "' to Override Property File Value");
                }
            }
        }
        return properties;
    }

    /**
     * Validates the given properties to limit to only DataLoader specific ones
     *
     * @param properties a list of properties
     * @return only the valid properties
     */
    private Properties getValidProperties(Properties properties) {
        Properties validProperties = new Properties();
        for (String propertyName : properties.stringPropertyNames()) {
            Property propertyEnum = Property.fromString(propertyName);
            String propertyValue = properties.getProperty(propertyName);
            if (propertyEnum != null) {
                validProperties.setProperty(propertyEnum.getName(), propertyValue);
            } else if (propertyName.endsWith(StringConsts.EXIST_FIELD_SUFFIX) || propertyName.endsWith(StringConsts.COLUMN_NAME_ALIAS_SUFFIX)) {
                validProperties.setProperty(propertyName, propertyValue);
            }
        }
        return validProperties;
    }

    /**
     * Returns the properties parsed from command line arguments, and sets the remaining args
     *
     * @param args user provided command line arguments
     * @return the properties gleaned from the command line
     */
    private Properties getArgumentProperties(String[] args) {
        Properties properties = new Properties();
        List<String> consumedArgs = new ArrayList<>();

        for (int i = 0; i + 1 < args.length; ++i) {
            String argName = args[i];
            String argValue = args[i + 1];

            Property property = Property.fromString(argName);
            if (property != null) {
                consumedArgs.add(argName);
                consumedArgs.add(argValue);
                properties.setProperty(property.getName(), argValue);
                ++i; // skip over value
            } else if (argName.contains(StringConsts.EXIST_FIELD_SUFFIX) || argName.contains(StringConsts.COLUMN_NAME_ALIAS_SUFFIX)) {
                consumedArgs.add(argName);
                consumedArgs.add(argValue);
                properties.setProperty(argName, argValue);
                ++i; // skip over value
            }
        }

        List<String> argList = new ArrayList<>(Arrays.asList(args));
        argList.removeAll(consumedArgs);
        remainingArgs = argList.toArray(new String[0]);

        return properties;
    }

    /**
     * Convert properties into higher level local variables for convenience
     *
     * @param properties The raw contents of the properties file
     */
    private void processProperties(Properties properties) {
        username = PropertyValidationUtil.validateRequiredStringField(Property.USERNAME.getName(),
            properties.getProperty(Property.USERNAME.getName()));
        password = PropertyValidationUtil.validateRequiredStringField(Property.PASSWORD.getName(),
            properties.getProperty(Property.PASSWORD.getName()));
        clientId = PropertyValidationUtil.validateRequiredStringField(Property.CLIENT_ID.getName(),
            properties.getProperty(Property.CLIENT_ID.getName()));
        clientSecret = PropertyValidationUtil.validateRequiredStringField(Property.CLIENT_SECRET.getName(),
            properties.getProperty(Property.CLIENT_SECRET.getName()));
        authorizeUrl = PropertyValidationUtil.validateRequiredStringField(Property.AUTHORIZE_URL.getName(),
            properties.getProperty(Property.AUTHORIZE_URL.getName()));
        tokenUrl = PropertyValidationUtil.validateRequiredStringField(Property.TOKEN_URL.getName(),
            properties.getProperty(Property.TOKEN_URL.getName()));
        loginUrl = PropertyValidationUtil.validateRequiredStringField(Property.LOGIN_URL.getName(),
            properties.getProperty(Property.LOGIN_URL.getName()));
        entityExistFieldsMap = createEntityExistFieldsMap(properties);
        PropertyValidationUtil.validateEntityExistFields(entityExistFieldsMap);
        columnNameMap = createColumnNameMap(properties);
        listDelimiter = PropertyValidationUtil.validateRequiredStringField(Property.LIST_DELIMITER.getName(),
            properties.getProperty(Property.LIST_DELIMITER.getName()));
        dateParser = getDateTimeFormatter(properties);
        processEmptyAssociations = PropertyValidationUtil.validateBooleanProperty(
            Boolean.valueOf(properties.getProperty(Property.PROCESS_EMPTY_ASSOCIATIONS.getName())));
        wildcardMatching = PropertyValidationUtil.validateBooleanProperty(
            Boolean.valueOf(properties.getProperty(Property.WILDCARD_MATCHING.getName())));
        singleByteEncoding = PropertyValidationUtil.validateBooleanProperty(
            Boolean.valueOf(properties.getProperty(Property.SINGLE_BYTE_ENCODING.getName())));
        skipDuplicates = PropertyValidationUtil.validateBooleanProperty(
            Boolean.valueOf(properties.getProperty(Property.SKIP_DUPLICATES.getName())));
        executeFormTriggers = PropertyValidationUtil.validateBooleanProperty(
            Boolean.valueOf(properties.getProperty(Property.EXECUTE_FORM_TRIGGERS.getName())));
        entity = PropertyValidationUtil.validateEntityInfoProperty(properties.getProperty(
            Property.ENTITY.getName()));
        numThreads = PropertyValidationUtil.validateNumThreads(Integer.valueOf(
            properties.getProperty(Property.NUM_THREADS.getName())));
        caching = PropertyValidationUtil.validateBooleanProperty(
            Boolean.valueOf(properties.getProperty(Property.CACHING.getName())));

        resultsFileEnabled = PropertyValidationUtil.validateBooleanProperty(
            Boolean.valueOf(properties.getProperty(Property.RESULTS_FILE_ENABLED.getName())));
        resultsFilePath = PropertyValidationUtil.validateResultsFilePath(
            properties.getProperty(Property.RESULTS_FILE_PATH.getName()));
        resultsFileWriteIntervalMsec = PropertyValidationUtil.validateIntervalMsec(
            properties.getProperty(Property.RESULTS_FILE_WRITE_INTERVAL_MSEC.getName()));
        waitSecondsBetweenFilesInDirectory = PropertyValidationUtil.validateWaitSeconds(properties.getProperty(
            Property.WAIT_SECONDS_BETWEEN_FILES_IN_DIRECTORY.getName()));
        verbose = PropertyValidationUtil.validateBooleanProperty(
            Boolean.valueOf(properties.getProperty(Property.VERBOSE.getName())));
    }

    private DateTimeFormatter getDateTimeFormatter(Properties properties) {
        try {
            return DateTimeFormat.forPattern(properties.getProperty(Property.DATE_FORMAT.getName()));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Provided dateFormat is invalid: cannot convert: '"
                + properties.getProperty(Property.DATE_FORMAT.getName()) + "' to a valid date format. "
                + "Valid formats are specified here: "
                + "http://docs.oracle.com/javase/8/docs/api/java/text/SimpleDateFormat.html");
        }
    }

    /**
     * Creates a Map of EntityName to Exist Fields. The exist fields are a String List of fields.
     *
     * @param properties The properties object
     * @return The newly created one-to-many map
     */
    private Map<String, List<String>> createEntityExistFieldsMap(Properties properties) {
        Map<String, List<String>> entityExistFields = Maps.newHashMap();
        for (String propertyName : properties.stringPropertyNames()) {
            if (propertyName.endsWith(StringConsts.EXIST_FIELD_SUFFIX)) {
                String entityName = propertyName.split(StringConsts.EXIST_FIELD_SUFFIX)[0];
                String upperCaseEntityName = WordUtils.capitalize(entityName);
                entityExistFields.put(upperCaseEntityName, Arrays.asList(properties.getProperty(propertyName).split(",")));
            }
        }
        return entityExistFields;
    }

    /**
     * Creates a Map of property values to property name where the property name have a given suffix.
     *
     * @param properties The properties object
     * @return The newly created one-to-many map
     */
    private Map<String, String> createColumnNameMap(Properties properties) {
        Map<String, String> map = Maps.newHashMap();
        for (String propertyName : properties.stringPropertyNames()) {
            if (propertyName.endsWith(StringConsts.COLUMN_NAME_ALIAS_SUFFIX)) {
                String alias = propertyName.split(StringConsts.COLUMN_NAME_ALIAS_SUFFIX)[0];
                String columnName = properties.getProperty(propertyName);
                map.put(alias.toLowerCase(), columnName);
            }
        }
        return map;
    }

    /**
     * Logs the contents of the properties files, but only a very select set of properties to keep user login
     * information safe. Uses the properties object to capture the original value before any transformation or
     * validation of the properties is done.
     *
     * @param fileName   The name of the properties file
     * @param properties The properties object
     */
    private void logProperties(String fileName, Properties properties) {
        printUtil.log("Using properties file: " + fileName);

        printUtil.log("# Section 2 -- Environment URLs");
        logPropertyIfExists(properties, Property.AUTHORIZE_URL.getName());
        logPropertyIfExists(properties, Property.TOKEN_URL.getName());
        logPropertyIfExists(properties, Property.LOGIN_URL.getName());

        printUtil.log("# Section 3 -- Exist Fields");
        logPropertiesEndingWith(properties, StringConsts.EXIST_FIELD_SUFFIX);

        printUtil.log("# Section 4 -- Column Mapping");
        logPropertiesEndingWith(properties, StringConsts.COLUMN_NAME_ALIAS_SUFFIX);

        printUtil.log("# Section 5 -- Formatting");
        logPropertyIfExists(properties, Property.LIST_DELIMITER.getName());
        logPropertyIfExists(properties, Property.DATE_FORMAT.getName());
        logPropertyIfExists(properties, Property.PROCESS_EMPTY_ASSOCIATIONS.getName());
        logPropertyIfExists(properties, Property.WILDCARD_MATCHING.getName());
        logPropertyIfExists(properties, Property.SINGLE_BYTE_ENCODING.getName());
        logPropertyIfExists(properties, Property.EXECUTE_FORM_TRIGGERS.getName());
        logPropertyIfExists(properties, Property.ENTITY.getName());

        printUtil.log("# Section 6 -- Performance");
        logPropertyIfExists(properties, Property.NUM_THREADS.getName());
        logPropertyIfExists(properties, Property.CACHING.getName());
    }

    private void logPropertiesEndingWith(Properties properties, String endingText) {
        List<String> propertyNames = new ArrayList<>(properties.stringPropertyNames());
        Collections.sort(propertyNames);
        for (String property : propertyNames) {
            if (property.endsWith(endingText)) {
                logPropertyIfExists(properties, property);
            }
        }
    }

    private void logPropertyIfExists(Properties properties, String property) {
        if (properties.containsKey(property)) {
            printUtil.log("   " + property + "=" + properties.getProperty(property));
        }
    }
}

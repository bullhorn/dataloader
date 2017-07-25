package com.bullhorn.dataloader.util;

import com.bullhorn.dataloader.enums.Property;
import com.google.common.base.CaseFormat;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.apache.commons.lang.WordUtils;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

/**
 * Wrapper around the properties that handles all interaction with properties throughout a session.
 */
public class PropertyFileUtil {

    final private String EXIST_FIELD_SUFFIX = "ExistField";
    final private String DATALOADER_PREFIX = "DATALOADER_";
    final private PrintUtil printUtil;
    private String[] remainingArgs;

    // Property values from the property file, saved in a more convenient format
    private String username;
    private String password;
    private String clientId;
    private String clientSecret;
    private String authorizeUrl;
    private String tokenUrl;
    private String loginUrl;
    private Map<String, List<String>> entityExistFieldsMap = Maps.newHashMap();
    private String listDelimiter;
    private DateTimeFormatter dateParser;
    private Integer numThreads;
    private Integer waitTimeMSecBetweenFilesInDirectory;

    /**
     * Constructor that assembles the dataloader properties from a variety of possible methods.
     *
     * @param fileName           the property filename to load
     * @param envVars            the environment variables to use (overrides the properties file)
     * @param systemProperties   the system properties to use (overrides the properties file and envVars)
     * @param args               the command line arguments (overrides all others)
     * @param propertyValidation validates the properties
     * @param printUtil          for logging properties
     * @throws IOException for file not found
     */
    public PropertyFileUtil(String fileName,
                            Map<String, String> envVars,
                            Properties systemProperties,
                            String[] args,
                            PropertyValidation propertyValidation,
                            PrintUtil printUtil) throws IOException {
        this.printUtil = printUtil;

        // If the users has specified a -Dpropertyfile command line parameter, use that fileName instead
        if (null != systemProperties.getProperty(StringConsts.PROPERTYFILE_ARG)) {
            fileName = systemProperties.getProperty(StringConsts.PROPERTYFILE_ARG);
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
        processProperties(properties, propertyValidation);
        logProperties(fileName, properties);
    }

    /**
     * Parses the given filename to pull out properties
     *
     * @param fileName the name of the file to parse
     * @return the properties
     * @throws IOException for File not found
     */
    private Properties getFileProperties(String fileName) throws IOException {
        FileInputStream fileInputStream = new FileInputStream(fileName);
        Properties properties = new Properties();
        properties.load(fileInputStream);
        fileInputStream.close();
        return properties;
    }

    /**
     * Parses the environment variables to pull out DataLoader specific properties
     *
     * Environment Variables must start with "DATALOADER_" in order to be used, and the log will show if an
     * environment variable has been used to override values from the property file.
     *
     * @param envVars the map of environment variables and their values
     * @return the valid properties
     */
    private Properties getEnvVarProperties(Map<String, String> envVars) {
        Properties properties = new Properties();
        for (Map.Entry<String, String> envVar : envVars.entrySet()) {
            String key = envVar.getKey();
            String value = envVar.getValue();
            if (key.startsWith(DATALOADER_PREFIX)) {
                String name = CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, key.split(DATALOADER_PREFIX)[1]);
                Property propertyEnum = Property.fromString(name);
                if (propertyEnum != null) {
                    properties.setProperty(propertyEnum.getName(), value);
                    printUtil.printAndLog("Using Environment Variable \'" + key + "\' to Override Property File Value");
                } else if (name.endsWith(EXIST_FIELD_SUFFIX)) {
                    properties.setProperty(name, value);
                    printUtil.printAndLog("Using Environment Variable \'" + key + "\' to Override Property File Value");
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
            } else if (propertyName.endsWith(EXIST_FIELD_SUFFIX)) {
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
            } else if (argName.contains(EXIST_FIELD_SUFFIX)) {
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
     * @param properties         The raw contents of the properties file
     * @param propertyValidation The validation utility
     */
    private void processProperties(Properties properties, PropertyValidation propertyValidation) {
        this.numThreads = propertyValidation.validateNumThreads(Integer.valueOf(properties.getProperty(Property.NUM_THREADS.getName())));
        this.username = propertyValidation.validateUsername(properties.getProperty(Property.USERNAME.getName()));
        this.password = propertyValidation.validatePassword(properties.getProperty(Property.PASSWORD.getName()));
        this.authorizeUrl = propertyValidation.validateAuthorizeUrl(properties.getProperty(Property.AUTHORIZE_URL.getName()));
        this.tokenUrl = propertyValidation.validateTokenUrl(properties.getProperty(Property.TOKEN_URL.getName()));
        this.clientId = propertyValidation.validateClientId(properties.getProperty(Property.CLIENT_ID.getName()));
        this.clientSecret = propertyValidation.validateClientSecret(properties.getProperty(Property.CLIENT_SECRET.getName()));
        this.loginUrl = propertyValidation.validateLoginUrl(properties.getProperty(Property.LOGIN_URL.getName()));
        this.listDelimiter = propertyValidation.validateListDelimiter(properties.getProperty(Property.LIST_DELIMITER.getName()));
        this.dateParser = getDateTimeFormatter(properties);
        this.entityExistFieldsMap = ImmutableMap.copyOf(createEntityExistFieldsMap(properties));
        propertyValidation.validateEntityExistFields(entityExistFieldsMap);
        this.waitTimeMSecBetweenFilesInDirectory = propertyValidation.validateWaitTimeMSec(properties.getProperty(Property.WAIT_TIME_MSEC_BETWEEN_FILES_IN_DIRECTORY.getName()));
    }

    private DateTimeFormatter getDateTimeFormatter(Properties properties) {
        try {
            return DateTimeFormat.forPattern(properties.getProperty(Property.DATE_FORMAT.getName()));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Creates a Map of EntityName to Exist Fields. The exist fields are a String List of fields.
     *
     * @param properties The properties object
     * @return The newly created map
     */
    private Map<String, List<String>> createEntityExistFieldsMap(Properties properties) {
        Map<String, List<String>> entityExistFields = Maps.newHashMap();
        for (String propertyName : properties.stringPropertyNames()) {
            if (propertyName.endsWith(EXIST_FIELD_SUFFIX)) {
                String entityName = propertyName.split(EXIST_FIELD_SUFFIX)[0];
                String upperCaseEntityName = WordUtils.capitalize(entityName);
                entityExistFields.put(upperCaseEntityName, Arrays.asList(properties.getProperty(propertyName).split(",")));
            }
        }
        return entityExistFields;
    }

    /**
     * Logs the contents of the properties files, but only a very select set of properties to keep user login
     * information safe. Uses the properties object to capture the original value before any transformation
     * or validation of the properties is done.
     *
     * @param fileName   The name of the properties file
     * @param properties The properties object
     */
    private void logProperties(String fileName, Properties properties) {
        printUtil.log("Using properties file: " + fileName);

        printUtil.log("# Section 2");
        logPropertyIfExists(properties, Property.AUTHORIZE_URL.getName());
        logPropertyIfExists(properties, Property.TOKEN_URL.getName());
        logPropertyIfExists(properties, Property.LOGIN_URL.getName());

        printUtil.log("# Section 3");
        logPropertiesEndingWith(properties, EXIST_FIELD_SUFFIX);

        printUtil.log("# Section 4");
        logPropertyIfExists(properties, Property.LIST_DELIMITER.getName());
        logPropertyIfExists(properties, Property.DATE_FORMAT.getName());

        printUtil.log("# Section 5");
        logPropertyIfExists(properties, Property.NUM_THREADS.getName());
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
     * Returns the list of entity exist fields for a given entity
     *
     * @param entity The entity name
     * @return The list of field names, if they exist.
     */
    public Optional<List<String>> getEntityExistFields(String entity) {
        return Optional.ofNullable(entityExistFieldsMap.get(entity));
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

    public Integer getWaitTimeMsecBetweenFilesInDirectory() {
        return waitTimeMSecBetweenFilesInDirectory;
    }
}

package com.bullhorn.dataloader.util;

import com.bullhorn.dataloader.util.validation.PropertyValidation;
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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;

/**
 * Wrapper around the properties file that handles all interaction with properties throughout a session.
 */
public class PropertyFileUtil {

    // Names of properties used in the properties file
    public static final String AUTHORIZE_URL = "authorizeUrl";
    public static final String CLIENT_ID = "clientId";
    public static final String CLIENT_SECRET = "clientSecret";
    public static final String DATE_FORMAT = "dateFormat";
    public static final String EXIST_FIELD = "ExistField";
    public static final String LIST_DELIMITER = "listDelimiter";
    public static final String LOGIN_URL = "loginUrl";
    public static final String NUM_THREADS = "numThreads";
    public static final String PASSWORD = "password";
    public static final String TOKEN_URL = "tokenUrl";
    public static final String USERNAME = "username";

    final private PropertyValidation propertyValidation;
    final private Properties systemProperties;
    final private PrintUtil printUtil;

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

    /**
     * Constructor that takes the filename of the property file and pulls out all of the values and logs them.
     *
     * @param fileName The property file to load and log
     */
    public PropertyFileUtil(String fileName,
                            Properties systemProperties,
                            PropertyValidation propertyValidation,
                            PrintUtil printUtil) throws IOException {
        this.systemProperties = systemProperties;
        this.propertyValidation = propertyValidation;
        this.printUtil = printUtil;

        // If the users has specified a -Dpropertyfile command line parameter, use that fileName instead
        if (null != systemProperties.getProperty(StringConsts.PROPERTYFILE_ARG)) {
            fileName = systemProperties.getProperty(StringConsts.PROPERTYFILE_ARG);
        }

        Properties properties = getProperties(fileName);

        processProperties(properties);
        logProperties(fileName, properties);
    }

    private Properties getProperties(String fileName) throws IOException {
        FileInputStream fileInputStream = new FileInputStream(fileName);
        Properties properties = new Properties();
        properties.load(fileInputStream);
        fileInputStream.close();
        return properties;
    }

    /**
     * Convert properties into higher level local variables for convenience
     *
     * @param properties The raw contents of the properties file
     */
    private void processProperties(Properties properties) {
        this.numThreads = propertyValidation.validateNumThreads(Integer.valueOf(getProperty(NUM_THREADS, properties)));
        this.username = propertyValidation.validateUsername(getProperty(USERNAME, properties));
        this.password = propertyValidation.validatePassword(getProperty(PASSWORD, properties));
        this.authorizeUrl = propertyValidation.validateAuthorizeUrl(getProperty(AUTHORIZE_URL, properties));
        this.tokenUrl = propertyValidation.validateTokenUrl(getProperty(TOKEN_URL, properties));
        this.clientId = propertyValidation.validateClientId(getProperty(CLIENT_ID, properties));
        this.clientSecret = propertyValidation.validateClientSecret(getProperty(CLIENT_SECRET, properties));
        this.loginUrl = propertyValidation.validateLoginUrl(getProperty(LOGIN_URL, properties));
        this.listDelimiter = propertyValidation.validateListDelimiter(getProperty(LIST_DELIMITER, properties));
        this.dateParser = getDateTimeFormatter(properties);
        this.entityExistFieldsMap = ImmutableMap.copyOf(createEntityExistFieldsMap(properties));

        propertyValidation.validateEntityExistFields(entityExistFieldsMap);
    }

    /**
     * Gets the property from the given property file, but allows a system property with the same name to take
     * precedence over the property file.
     *
     * @param key the name of the property
     * @param properties the property file object
     * @return the value of the property, or null if it does not exist
     */
    private String getProperty(String key, Properties properties) {
        String value = systemProperties.getProperty(key);
        if (value == null) {
            value = properties.getProperty(key);
        }
        return value;
    }

    /**
     * Returns the union of all system and property file properties
     * @param properties property file properties
     * @return all property keys
     */
    private Set<String> getPropertyNames(Properties properties) {
        Set<String> allPropertyNames = new HashSet<>();
        allPropertyNames.addAll(systemProperties.stringPropertyNames());
        allPropertyNames.addAll(properties.stringPropertyNames());
        return allPropertyNames;
    }

    private DateTimeFormatter getDateTimeFormatter(Properties properties) {
        try {
            return DateTimeFormat.forPattern(getProperty(DATE_FORMAT, properties));
        } catch (IllegalArgumentException e) {
            return null;
        }
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

    /**
     * Creates a Map of EntityName to Exist Fields. The exist fields are a String List of fields.
     *
     * @param properties The properties object
     * @return The newly created map
     */
    private Map<String, List<String>> createEntityExistFieldsMap(Properties properties) {
        Map<String, List<String>> entityExistFields = Maps.newHashMap();

        for (String property : getPropertyNames(properties)) {
            if (property.endsWith(EXIST_FIELD)) {
                String entityName = property.split(EXIST_FIELD)[0];
                String upperCaseEntityName = WordUtils.capitalize(entityName);
                entityExistFields.put(upperCaseEntityName, Arrays.asList(getProperty(property, properties).split(",")));
            }
        }

        return entityExistFields;
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

    public String getListDelimiter() {
        return listDelimiter;
    }

    public DateTimeFormatter getDateParser() {
        return dateParser;
    }

    public Integer getNumThreads() {
        return numThreads;
    }

    /**
     * Logs the contents of the properties files, but only a very select set of properties to keep user login
     * information safe.
     * <p>
     * NOTE: Though we could pull from the internally stored variables for these properties, it is actually
     * better to pull from the properties file using just the name, because then we don't have to convert back from a
     * set or map to the original string value.
     *
     * @param fileName   The name of the properties file
     * @param properties The properties object
     */
    private void logProperties(String fileName, Properties properties) {
        printUtil.log("Using properties file: " + fileName);

        printUtil.log("# Section 2");
        logPropertyIfExists(properties, AUTHORIZE_URL);
        logPropertyIfExists(properties, TOKEN_URL);
        logPropertyIfExists(properties, LOGIN_URL);

        printUtil.log("# Section 3");
        logPropertiesEndingWith(properties, EXIST_FIELD);

        printUtil.log("# Section 4");
        logPropertyIfExists(properties, LIST_DELIMITER);
        logPropertyIfExists(properties, DATE_FORMAT);

        printUtil.log("# Section 5");
        logPropertyIfExists(properties, NUM_THREADS);
    }

    private void logPropertiesEndingWith(Properties properties, String endingText) {
        List<String> propertyNames = new ArrayList<String>(getPropertyNames(properties));
        Collections.sort(propertyNames);
        for (String property : propertyNames) {
            if (property.endsWith(endingText)) {
                logPropertyIfExists(properties, property);
            }
        }
    }

    private void logPropertyIfExists(Properties properties, String property) {
        if (properties.containsKey(property)) {
            printUtil.log("   " + property + "=" + getProperty(property, properties));
        }
    }
}

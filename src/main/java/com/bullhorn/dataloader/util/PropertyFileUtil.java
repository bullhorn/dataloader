package com.bullhorn.dataloader.util;

import com.bullhorn.dataloader.util.validation.PropertyValidation;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
import java.util.Set;

/**
 * Wrapper around the properties file that handles all interaction with properties throughout a session.
 */
public class PropertyFileUtil {

    private static Logger log = LogManager.getLogger(PropertyFileUtil.class);
    private static PropertyValidation propertyFileValidation = new PropertyValidation();

    // Names of properties used in the properties file
    public static final String AUTHORIZE_URL = "authorizeUrl";
    public static final String CLIENT_ID = "clientId";
    public static final String CLIENT_SECRET = "clientSecret";
    public static final String DATE_FORMAT = "dateFormat";
    public static final String EXIST_FIELD = "ExistField";
    public static final String FRONT_LOADED_ENTITIES = "frontLoadedEntities";
    public static final String LIST_DELIMITER = "listDelimiter";
    public static final String LOGIN_URL = "loginUrl";
    public static final String NUM_THREADS = "numThreads";
    public static final String PASSWORD = "password";
    public static final String TOKEN_URL = "tokenUrl";
    public static final String USERNAME = "username";

    // Property values from the property file, saved in a more convenient format
    private String username;
    private String password;
    private String clientId;
    private String clientSecret;
    private String authorizeUrl;
    private String tokenUrl;
    private String loginUrl;
    private Map<String, List<String>> entityExistFieldsMap = Maps.newHashMap();
    private Set<String> frontLoadedEntities = Sets.newHashSet();
    private String listDelimiter;
    private DateTimeFormatter dateParser;
    private Integer numThreads;

    /**
     * Constructor that takes the filename of the property file and pulls out all of the values and logs them.
     *
     * @param fileName The property file to load and log
     */
    public PropertyFileUtil (String fileName) throws IOException {
        // If the users has specified a -Dpropertyfile command line parameter, use that fileName instead
        if (null != System.getProperty(StringConsts.PROPERTYFILE_ARG)) {
            fileName = System.getProperty(StringConsts.PROPERTYFILE_ARG);
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
    protected void processProperties(Properties properties) {
        this.numThreads = propertyFileValidation.validateNumThreads(Integer.valueOf(properties.getProperty(NUM_THREADS)));
        this.username = propertyFileValidation.validateUsername(properties.getProperty(USERNAME));
        this.password = propertyFileValidation.validatePassword(properties.getProperty(PASSWORD));
        this.authorizeUrl = propertyFileValidation.validateAuthorizeUrl(properties.getProperty(AUTHORIZE_URL));
        this.tokenUrl = propertyFileValidation.validateTokenUrl(properties.getProperty(TOKEN_URL));
        this.clientId = propertyFileValidation.validateClientId(properties.getProperty(CLIENT_ID));
        this.clientSecret = propertyFileValidation.validateClientSecret(properties.getProperty(CLIENT_SECRET));
        this.loginUrl = propertyFileValidation.validateLoginUrl(properties.getProperty(LOGIN_URL));
        this.listDelimiter = propertyFileValidation.validateListDelimiter(properties.getProperty(LIST_DELIMITER));
        this.dateParser = getDateTimeFormatter(properties);
        this.entityExistFieldsMap = ImmutableMap.copyOf(createEntityExistFieldsMap(properties));

        propertyFileValidation.validateEntityExistFields(entityExistFieldsMap);

        String frontLoadedEntitiesProperty = properties.getProperty(FRONT_LOADED_ENTITIES);
        if (!frontLoadedEntitiesProperty.isEmpty()) {
            this.frontLoadedEntities.addAll(Arrays.asList(frontLoadedEntitiesProperty.split(",")));
        }
        propertyFileValidation.validateFrontLoadedEntities(frontLoadedEntities);
    }

    private DateTimeFormatter getDateTimeFormatter(Properties properties) {
        try {
            return DateTimeFormat.forPattern(properties.getProperty(DATE_FORMAT));
        } catch(IllegalArgumentException e) {
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

        for (String property : properties.stringPropertyNames()) {
            if (property.endsWith(EXIST_FIELD)) {
                String entityName = property.split(EXIST_FIELD)[0];
                String upperCaseEntityName = entityName.substring(0, 1).toUpperCase() + entityName.substring(1);
                entityExistFields.put(upperCaseEntityName, Arrays.asList(properties.getProperty(property).split(",")));
            }
        }

        return entityExistFields;
    }

    /**
     * Return the names of all entities to front load
     *
     * @return The set of entity names
     */
    public Set<String> getFrontLoadedEntities() {
        return frontLoadedEntities;
    }

    /**
     * Returns true if the entity is marked as being front loaded
     *
     * @param entity Name of entity
     * @return True if listed in the property file as an entity to front load
     */
    public boolean shouldFrontLoadEntity(String entity) {
        return frontLoadedEntities.contains(entity);
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
     *
     * NOTE: Though we could pull from the internally stored variables for these properties, it is actually
     * better to pull from the properties file using just the name, because then we don't have to convert back from a
     * set or map to the original string value.
     *
     * @param fileName The name of the properties file
     * @param properties The properties object
     */
    private void logProperties(String fileName, Properties properties) {
        log.info("Using properties file: " + fileName);

        log.info("# Section 2");
        logPropertyIfExists(properties, AUTHORIZE_URL);
        logPropertyIfExists(properties, TOKEN_URL);
        logPropertyIfExists(properties, LOGIN_URL);

        log.info("# Section 3");
        logPropertiesEndingWith(properties, EXIST_FIELD);
        logPropertyIfExists(properties, FRONT_LOADED_ENTITIES);

        log.info("# Section 4");
        logPropertyIfExists(properties, LIST_DELIMITER);
        logPropertyIfExists(properties, DATE_FORMAT);

        log.info("# Section 5");
        logPropertyIfExists(properties, NUM_THREADS);
    }

    private void logPropertiesEndingWith(Properties properties, String endingText) {
        List<String> propertyNames = new ArrayList<String>(properties.stringPropertyNames());
        Collections.sort(propertyNames);
        for (String property : propertyNames) {
            if (property.endsWith(endingText)) {
                logPropertyIfExists(properties, property);
            }
        }
    }

    private void logPropertyIfExists(Properties properties, String property) {
        if (properties.containsKey(property)) {
            log.info("   " + property + "=" + properties.getProperty(property));
        }
    }
}

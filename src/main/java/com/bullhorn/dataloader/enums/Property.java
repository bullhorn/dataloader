package com.bullhorn.dataloader.enums;

import com.google.common.base.CaseFormat;

/**
 * Names of properties used by DataLoader for setting up a session
 */
public enum Property {

    AUTHORIZE_URL("authorizeUrl"),
    CLIENT_ID("clientId"),
    CLIENT_SECRET("clientSecret"),
    DATE_FORMAT("dateFormat"),
    LIST_DELIMITER("listDelimiter"),
    LOGIN_URL("loginUrl"),
    NUM_THREADS("numThreads"),
    PASSWORD("password"),
    PROCESS_EMPTY_ASSOCIATIONS("processEmptyAssociations"),
    RESULTS_FILE_ENABLED("resultsFileEnabled"),
    RESULTS_FILE_PATH("resultsFilePath"),
    RESULTS_FILE_WRITE_INTERVAL_MSEC("resultsFileWriteIntervalMsec"),
    SINGLE_BYTE_ENCODING("singleByteEncoding"),
    TOKEN_URL("tokenUrl"),
    USERNAME("username"),
    VERBOSE("verbose"),
    WAIT_SECONDS_BETWEEN_FILES_IN_DIRECTORY("waitSecondsBetweenFilesInDirectory");

    private final String propertyName;

    Property(String propertyName) {
        this.propertyName = propertyName;
    }

    /**
     * Returns the property that matches the given string, or null otherwise
     *
     * @param propertyName Any property name, in camelCase or UPPER_UNDERSCORE, with or without a "-" prefix
     * @return the property if it exists, null otherwise
     */
    public static Property fromString(String propertyName) {
        propertyName = propertyName.replaceFirst("^-+", "");

        String lowerCamelCaseName = CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, propertyName);
        for (Property property : Property.values()) {
            if (property.getName().equalsIgnoreCase(lowerCamelCaseName)) {
                return property;
            }
        }

        return null;
    }

    public String getName() {
        return this.propertyName;
    }
}

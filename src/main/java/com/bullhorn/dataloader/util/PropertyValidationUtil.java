package com.bullhorn.dataloader.util;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;

import com.bullhorn.dataloader.enums.EntityInfo;
import com.bullhorn.dataloader.enums.Property;

/**
 * Validates the user's entries in the properties file.
 */
class PropertyValidationUtil {
    private static final Integer MAX_NUM_THREADS = 15;
    private static final Integer MAX_WAIT_SECONDS = 3600; // 1 hour
    private static final Integer DEFAULT_INTERVAL_MSEC = 500; // Wait for half a second
    private static final String DEFAULT_RESULTS_FILE_PATH = "./results.json";

    static void validateEntityExistFields(Map<String, List<String>> entityExistFieldsMap) {
        for (Map.Entry<String, List<String>> entityEntry : entityExistFieldsMap.entrySet()) {
            // Clean up fields by trimming whitespace
            for (String value : entityEntry.getValue()) {
                String trimmed = value.trim();
                entityEntry.getValue().set(entityEntry.getValue().indexOf(value), trimmed);
            }

            // Check that the exist field matches a real entity
            if (EntityInfo.fromString(entityEntry.getKey()) == null) {
                throw new IllegalArgumentException("DataLoader Properties Error: "
                    + WordUtils.uncapitalize(entityEntry.getKey())
                    + "ExistField property does not match a supported entity - unrecognized entity: '"
                    + entityEntry.getKey() + "'");
            }
        }
    }

    static Integer validateNumThreads(Integer numThreads) {
        if (numThreads < 0 || numThreads > MAX_NUM_THREADS) {
            throw new IllegalArgumentException("DataLoader Properties Error: numThreads property must be in the range of 1 to " + MAX_NUM_THREADS);
        }
        if (numThreads == 0) {
            numThreads = (Runtime.getRuntime().availableProcessors() * 2) + 1;
        }
        return Math.min(numThreads, MAX_NUM_THREADS);
    }

    static Integer validateWaitSeconds(String waitSecondsString) {
        int waitSeconds = 0;
        if (waitSecondsString != null) {
            waitSeconds = Integer.parseInt(waitSecondsString);
        }
        if (waitSeconds < 0 || waitSeconds > MAX_WAIT_SECONDS) {
            throw new IllegalArgumentException(
                "DataLoader Properties Error: " + Property.WAIT_SECONDS_BETWEEN_FILES_IN_DIRECTORY.getName()
                    + " property must be in the range of 0 to " + MAX_WAIT_SECONDS);
        }
        return waitSeconds;
    }

    static String validateResultsFilePath(String resultsFilePath) {
        return resultsFilePath == null ? DEFAULT_RESULTS_FILE_PATH : resultsFilePath;
    }

    static Integer validateIntervalMsec(String intervalMsecString) {
        return intervalMsecString == null ? DEFAULT_INTERVAL_MSEC : Integer.valueOf(intervalMsecString);
    }

    static Boolean validateBooleanProperty(Boolean value) {
        return value != null && value;
    }

    static String validateRequiredStringField(String name, String value) {
        if (value == null) {
            throw new IllegalArgumentException("DataLoader Properties Error: missing '" + name + "' property");
        }
        String trimmedUsername = value.trim();
        if (trimmedUsername.isEmpty()) {
            throw new IllegalArgumentException("DataLoader Properties Error: '" + name + "' property must not be blank");
        }
        return value;
    }

    static EntityInfo validateEntityInfoProperty(String value) {
        EntityInfo entityInfo = null;
        if (!StringUtils.isEmpty(value)) {
            entityInfo = EntityInfo.fromString(value);
            if (entityInfo == null) {
                throw new IllegalArgumentException("DataLoader Properties Error: Could not determine entity from entity property: '" + value + "'");
            }
        }
        return entityInfo;
    }

    static String validateOptionalStringField(String value) {
        return value == null ? "" : value;
    }
}

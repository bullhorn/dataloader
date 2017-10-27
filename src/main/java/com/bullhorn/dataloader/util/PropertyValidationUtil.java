package com.bullhorn.dataloader.util;

import com.bullhorn.dataloader.enums.EntityInfo;
import org.apache.commons.lang.WordUtils;

import java.util.List;
import java.util.Map;

/**
 * Validates the user's entries in the properties file.
 */
public class PropertyValidationUtil {
    private static final Integer MAX_NUM_THREADS = 15;
    private static final Integer MAX_WAIT_TIME_SECONDS = 3600 * 1000; // 1 hour

    public PropertyValidationUtil() {
    }

    String validateUsername(String username) {
        return validateStringField("username", username);
    }

    String validatePassword(String password) {
        return validateStringField("password", password);
    }

    String validateClientId(String clientId) {
        return validateStringField("clientId", clientId);
    }

    String validateClientSecret(String clientSecret) {
        return validateStringField("clientSecret", clientSecret);
    }

    String validateAuthorizeUrl(String authorizeUrl) {
        return validateStringField("authorizeUrl", authorizeUrl);
    }

    String validateTokenUrl(String tokenUrl) {
        return validateStringField("tokenUrl", tokenUrl);
    }

    String validateLoginUrl(String loginUrl) {
        return validateStringField("loginUrl", loginUrl);
    }

    void validateEntityExistFields(Map<String, List<String>> entityExistFieldsMap) {
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

    String validateListDelimiter(String listDelimiter) {
        return validateStringField("listDelimiter", listDelimiter);
    }

    Integer validateNumThreads(Integer numThreads) {
        if (numThreads < 0 || numThreads > MAX_NUM_THREADS) {
            throw new IllegalArgumentException("DataLoader Properties Error: numThreads property must be in the range of 1 to " + MAX_NUM_THREADS);
        }
        if (numThreads == 0) {
            numThreads = (Runtime.getRuntime().availableProcessors() * 2) + 1;
        }
        return Math.min(numThreads, MAX_NUM_THREADS);
    }

    Integer validateWaitTimeMSec(String waitTimeString) {
        Integer waitTime = 0;
        if (waitTimeString != null) {
            waitTime = Integer.valueOf(waitTimeString);
        }
        if (waitTime < 0 || waitTime > MAX_WAIT_TIME_SECONDS) {
            throw new IllegalArgumentException(
                "DataLoader Properties Error: waitTimeMSecBetweenFilesInDirectory property must be in the range of 0 to "
                    + MAX_WAIT_TIME_SECONDS);
        }
        return waitTime;
    }

    private String validateStringField(String name, String value) {
        if (value == null) {
            throw new IllegalArgumentException("DataLoader Properties Error: missing '" + name + "' property");
        }
        String trimmedUsername = value.trim();
        if (trimmedUsername.isEmpty()) {
            throw new IllegalArgumentException("DataLoader Properties Error: '" + name + "' property must not be blank");
        }
        return value;
    }
}

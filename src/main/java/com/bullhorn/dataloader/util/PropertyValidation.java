package com.bullhorn.dataloader.util;

import com.bullhorn.dataloader.enums.EntityInfo;
import org.apache.commons.lang.WordUtils;

import java.util.List;
import java.util.Map;

// TODO: pull this out into the ValidationUtil and simplify down to a single class
public class PropertyValidation {

    public PropertyValidation() {
    }

    public String validateUsername(String username) {
        String trimmedUsername = username.trim();
        if (trimmedUsername.isEmpty()) {
            throw new IllegalArgumentException("DataLoader Properties Error: username property must not be blank");
        }
        return username;
    }

    public String validatePassword(String password) {
        String trimmedPassword = password.trim();
        if (trimmedPassword.isEmpty()) {
            throw new IllegalArgumentException("DataLoader Properties Error: password property must not be blank");
        }
        return password;
    }

    public String validateClientId(String clientId) {
        String trimmedClientId = clientId.trim();
        if (trimmedClientId.isEmpty()) {
            throw new IllegalArgumentException("DataLoader Properties Error: clientId property must not be blank");
        }
        return clientId;
    }

    public String validateClientSecret(String clientSecret) {
        String trimmedClientSecret = clientSecret.trim();
        if (trimmedClientSecret.isEmpty()) {
            throw new IllegalArgumentException("DataLoader Properties Error: clientSecret property must not be blank");
        }
        return clientSecret;
    }

    public String validateAuthorizeUrl(String authorizeUrl) {
        String trimmedAuthorizeUrl = authorizeUrl.trim();
        if (trimmedAuthorizeUrl.isEmpty()) {
            throw new IllegalArgumentException("DataLoader Properties Error: authorizeUrl property must not be blank");
        }
        return authorizeUrl;
    }

    public String validateTokenUrl(String tokenUrl) {
        String trimmedTokenUrl = tokenUrl.trim();
        if (trimmedTokenUrl.isEmpty()) {
            throw new IllegalArgumentException("DataLoader Properties Error: tokenUrl property must not be blank");
        }
        return tokenUrl;
    }

    public String validateLoginUrl(String loginUrl) {
        String trimmedLoginUrl = loginUrl.trim();
        if (trimmedLoginUrl.isEmpty()) {
            throw new IllegalArgumentException("DataLoader Properties Error: loginUrl property must not be blank");
        }
        return loginUrl;
    }

    public void validateEntityExistFields(Map<String, List<String>> entityExistFieldsMap) {
        for (Map.Entry<String, List<String>> entityEntry : entityExistFieldsMap.entrySet()) {
            // Clean up fields by trimming whitespace
            for (String value : entityEntry.getValue()) {
                String trimmed = value.trim();
                entityEntry.getValue().set(entityEntry.getValue().indexOf(value), trimmed);
            }

            // Check that the exist field matches a real entity
            if (EntityInfo.fromString(entityEntry.getKey()) == null) {
                throw new IllegalArgumentException("DataLoader Properties Error: " + WordUtils.uncapitalize(entityEntry.getKey()) + "ExistField property does not match a supported entity - unrecognized entity: '" + entityEntry.getKey() + "'");
            }
        }
    }

    public String validateListDelimiter(String listDelimiter) {
        String trimmedListDelimiter = listDelimiter.trim();
        if (trimmedListDelimiter.isEmpty()) {
            throw new IllegalArgumentException("DataLoader Properties Error: listDelimiter property must not be blank");
        }
        return listDelimiter;
    }

    public Integer validateNumThreads(Integer numThreads) {
        final Integer MAX_NUM_THREADS = 15;

        if (numThreads < 0 || numThreads > MAX_NUM_THREADS) {
            throw new IllegalArgumentException("DataLoader Properties Error: numThreads property must be in the range of 1 to " + MAX_NUM_THREADS);
        }
        if (numThreads == 0) {
            numThreads = (Runtime.getRuntime().availableProcessors() * 2) + 1;
        }
        return Math.min(numThreads, MAX_NUM_THREADS);
    }

    public Integer validateWaitTimeMSec(String waitTimeString) {
        final int MAX_WAIT_TIME_SECONDS = 3600 * 1000; // 1 hour
        Integer waitTime = 0;
        if (waitTimeString != null) {
            waitTime = Integer.valueOf(waitTimeString);
        }
        if (waitTime < 0 || waitTime > MAX_WAIT_TIME_SECONDS) {
            throw new IllegalArgumentException("DataLoader Properties Error: waitTimeMSecBetweenFilesInDirectory property must be in the range of 0 to " + MAX_WAIT_TIME_SECONDS);
        }
        return waitTime;
    }
}

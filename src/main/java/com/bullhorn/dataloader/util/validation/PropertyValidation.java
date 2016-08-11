package com.bullhorn.dataloader.util.validation;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.WordUtils;

import com.bullhorn.dataloader.meta.Entity;

public class PropertyValidation {

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

    public void validateFrontLoadedEntities(Set<String> frontLoadedEntities) {
        final Set<String> entitiesFormatted = new HashSet<>();
        for (Entity entity : Entity.values()) {
            entitiesFormatted.add(entity.getEntityName().trim().replaceAll("_", ""));
        }

        for (String frontLoadedEntity : frontLoadedEntities) {
            String trimmedFrontLoadedEntity = frontLoadedEntity.trim();
            if (!entitiesFormatted.contains(trimmedFrontLoadedEntity)) {
                throw new IllegalArgumentException("DataLoader Properties Error: frontLoadedEntities property contains invalid entity name: " + frontLoadedEntity);
            }
        }
    }

    public void validateEntityExistFields(Map<String, List<String>> entityExistFieldsMap) {
        // Check that all entities have an exist field
        for (Entity entity: Entity.values()) {
            if (!entityExistFieldsMap.containsKey(entity.getEntityName())) {
                throw new IllegalArgumentException("DataLoader Properties Error: missing property: " + WordUtils.uncapitalize(entity.getEntityName()) + "ExistField");
            }
        }

        for (Map.Entry<String, List<String>> entityEntry : entityExistFieldsMap.entrySet()) {
            // Clean up fields by trimming whitespace
            for (String value : entityEntry.getValue()) {
                String trimmed = value.trim();
                entityEntry.getValue().set(entityEntry.getValue().indexOf(value), trimmed);
            }

            // Check that the exist field matches a real entity
            if (Entity.fromString(entityEntry.getKey()) == null) {
                throw new IllegalArgumentException("DataLoader Properties Error: " + WordUtils.uncapitalize(entityEntry.getKey()) + "ExistField property does not match a supported entity - unrecognized entity: '" + entityEntry.getKey() + "'");
            }

            // Check that none are empty
            if (entityEntry.getValue().get(0).equals("")) {
                throw new IllegalArgumentException("DataLoader Properties Error: " + WordUtils.uncapitalize(entityEntry.getKey()) + "ExistField property must not be blank");
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
        if (numThreads < 1 || numThreads > 20) {
            throw new IllegalArgumentException("DataLoader Properties Error: numThreads property must in the range of 1 to 20");
        }
        return numThreads;
    }

    public PropertyValidation() {
    }
}

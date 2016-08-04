package com.bullhorn.dataloader.util.validation;

import com.bullhorn.dataloader.meta.Entity;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class PropertyValidation {

	public String validateUsername(String username) {
		if(username.isEmpty()) {
			throw new IllegalArgumentException("DataLoader Properties Error: username property must not be blank");
		}
		return username;
	}

	public String validatePassword(String password) {
		if(password.isEmpty()) {
			throw new IllegalArgumentException("DataLoader Properties Error: password property must not be blank");
		}
		return password;
	}

	public String validateClientId(String clientId) {
		if(clientId.isEmpty()) {
			throw new IllegalArgumentException("DataLoader Properties Error: clientId property must not be blank");
		}
		return clientId;
	}

	public String validateClientSecret(String clientSecret) {
		if(clientSecret.isEmpty()) {
			throw new IllegalArgumentException("DataLoader Properties Error: clientSecret property must not be blank");
		}
		return clientSecret;
	}

	public String validateAuthorizeUrl(String authorizeUrl) {
		if(authorizeUrl.isEmpty()) {
			throw new IllegalArgumentException("DataLoader Properties Error: authorizeUrl property must not be blank");
		}
		return authorizeUrl;
	}

	public String validateTokenUrl(String tokenUrl) {
		if(tokenUrl.isEmpty()) {
			throw new IllegalArgumentException("DataLoader Properties Error: tokenUrl property must not be blank");
		}
		return tokenUrl;
	}

	public String validateLoginUrl(String loginUrl) {
		if(loginUrl.isEmpty()) {
			throw new IllegalArgumentException("DataLoader Properties Error: loginUrl property must not be blank");
		}
		return loginUrl;
	}

	public void validateFrontLoadedEntities(Set<String> frontLoadedEntities) {
		final Set<String> entitiesFormatted = new HashSet<>();
		for(Entity entity : Entity.values()) {
			entitiesFormatted.add(entity.getEntityName().replaceAll("_", ""));
		}

		for(String frontLoadedEntity : frontLoadedEntities) {
			if(!entitiesFormatted.contains(frontLoadedEntity)) {
				throw new IllegalArgumentException("DataLoader Properties Error: frontLoadedEntities property contains invalid entity name: " + frontLoadedEntity);
			}
		}
	}

	public void validateEntityExistFields(Map<String, List<String>> entityExistFieldsMap) {
		for(Map.Entry<String, List<String>> entity : entityExistFieldsMap.entrySet() ) {
			if(entity.getValue().get(0).equals("")) {
				throw new IllegalArgumentException("DataLoader Properties Error: " + entity.getKey() + " property must not be blank");
			}
		}
	}

	public String validateListDelimiter(String listDelimiter) {
		if(listDelimiter.isEmpty()) {
			throw new IllegalArgumentException("DataLoader Properties Error: listDelimiter property must not be blank");
		}
		return listDelimiter;
	}

	public Integer validateNumThreads(Integer numThreads) {
		if(numThreads < 1 || numThreads > 20) {
			throw new IllegalArgumentException("DataLoader Properties Error: numThreads property must in the range of 1 to 20");
		}
		return numThreads;
	}

	public PropertyValidation() {}
}

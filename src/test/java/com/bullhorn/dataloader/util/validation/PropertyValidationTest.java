package com.bullhorn.dataloader.util.validation;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

public class PropertyValidationTest {

	private PropertyValidation propertyValidation;

	@Before
	public void setup() {
		propertyValidation = new PropertyValidation();
	}

	@Test(expected=IllegalArgumentException.class)
	public void testEmptyUserName() throws IOException {
		propertyValidation.validateUsername("");
	}

	@Test(expected=IllegalArgumentException.class)
	public void testEmptyPassword() throws IOException {
		propertyValidation.validatePassword("");
	}

	@Test(expected=IllegalArgumentException.class)
	public void testEmptyClientId() throws IOException {
		propertyValidation.validateClientId("");
	}

	@Test(expected=IllegalArgumentException.class)
	public void testEmptyClientSecret() throws IOException {
		propertyValidation.validateClientSecret("");
	}

	@Test(expected=IllegalArgumentException.class)
	public void testEmptyAuthorizeUrl() throws IOException {
		propertyValidation.validateAuthorizeUrl("");
	}

	@Test(expected=IllegalArgumentException.class)
	public void testEmptyTokenUrl() throws IOException {
		propertyValidation.validateTokenUrl("");
	}

	@Test(expected=IllegalArgumentException.class)
	public void testEmptyLoginUrl() throws IOException {
		propertyValidation.validateLoginUrl("");
	}

	@Test(expected=IllegalArgumentException.class)
	public void testFrontLoadedEntities_MispelledEntity() throws IOException {
		Set<String> frontLoadedEntities = new HashSet<>();
		frontLoadedEntities.add("Candidate");
		frontLoadedEntities.add("Bogus");
		propertyValidation.validateFrontLoadedEntities(frontLoadedEntities);
	}

	@Test(expected=IllegalArgumentException.class)
	public void testEmptyEntityExistFields() throws IOException {
		Map<String, List<String>> entityExistFieldsMap = new HashMap<>();
		entityExistFieldsMap.put("Candidate", Arrays.asList(""));
		propertyValidation.validateEntityExistFields(entityExistFieldsMap);
	}

	@Test(expected=IllegalArgumentException.class)
	public void testEmptyListDelimiter() throws IOException {
		propertyValidation.validateListDelimiter("");
	}

	@Test(expected=IllegalArgumentException.class)
	public void testEmptyNumThreads() throws IOException {
		propertyValidation.validateNumThreads(Integer.valueOf(""));
	}

	@Test(expected=IllegalArgumentException.class)
	public void testOutOfBoundsNumThreads() throws IOException {
		propertyValidation.validateNumThreads(0);
	}
}

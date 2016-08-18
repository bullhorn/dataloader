package com.bullhorn.dataloader.util.validation;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PropertyValidationTest {

	private PropertyValidation propertyValidation;
	private Map<String, List<String>> entityExistFieldsMap;

	@Before
	public void setup() {
		propertyValidation = new PropertyValidation();

		entityExistFieldsMap = new HashMap<>();
		entityExistFieldsMap.put("AppointmentAttendee", Arrays.asList("externalID"));
		entityExistFieldsMap.put("Appointment", Arrays.asList("externalID"));
		entityExistFieldsMap.put("BusinessSector", Arrays.asList("name"));
		entityExistFieldsMap.put("CandidateEducation", Arrays.asList("externalID"));
		entityExistFieldsMap.put("Candidate", Arrays.asList(" id "));
		entityExistFieldsMap.put("CandidateReference", Arrays.asList("externalID"));
		entityExistFieldsMap.put("CandidateWorkHistory", Arrays.asList("externalID"));
		entityExistFieldsMap.put("Category", Arrays.asList("occupation"));
		entityExistFieldsMap.put("Certification", Arrays.asList("externalID"));
		entityExistFieldsMap.put("ClientContact", Arrays.asList("id"));
		entityExistFieldsMap.put("ClientCorporation", Arrays.asList("id"));
		entityExistFieldsMap.put("CorporateUser", Arrays.asList("externalID"));
		entityExistFieldsMap.put("CorporationDepartment", Arrays.asList("externalID"));
		entityExistFieldsMap.put("Country", Arrays.asList("externalID"));
		entityExistFieldsMap.put("HousingComplex", Arrays.asList(""));
		entityExistFieldsMap.put("JobOrder", Arrays.asList("title", "name"));
		entityExistFieldsMap.put("JobSubmission", Arrays.asList("externalID"));
		entityExistFieldsMap.put("JobSubmissionHistory", Arrays.asList("externalID"));
		entityExistFieldsMap.put("Lead", Arrays.asList("id"));
		entityExistFieldsMap.put("NoteEntity", Arrays.asList("externalID"));
		entityExistFieldsMap.put("Note", Arrays.asList("externalID"));
		entityExistFieldsMap.put("Opportunity", Arrays.asList("id"));
		entityExistFieldsMap.put("PlacementChangeRequest", Arrays.asList("externalID"));
		entityExistFieldsMap.put("PlacementCommission", Arrays.asList("externalID"));
		entityExistFieldsMap.put("Placement", Arrays.asList("externalID"));
		entityExistFieldsMap.put("Sendout", Arrays.asList("externalID"));
		entityExistFieldsMap.put("Skill", Arrays.asList("name"));
		entityExistFieldsMap.put("Specialty", Arrays.asList("externalID"));
		entityExistFieldsMap.put("State", Arrays.asList("externalID"));
		entityExistFieldsMap.put("Task", Arrays.asList("externalID"));
		entityExistFieldsMap.put("Tearsheet", Arrays.asList("externalID"));
		entityExistFieldsMap.put("TimeUnit", Arrays.asList(""));
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

	@Test
	public void testEntityExistFields_TrimWhitespace() throws IOException {
        Assert.assertEquals(" id ", entityExistFieldsMap.get("Candidate").get(0));
		propertyValidation.validateEntityExistFields(entityExistFieldsMap);
        Assert.assertEquals("id", entityExistFieldsMap.get("Candidate").get(0));
	}

	@Test
	public void testEntityExistFields_Missing() throws IOException {
		entityExistFieldsMap.remove("BusinessSector");
		propertyValidation.validateEntityExistFields(entityExistFieldsMap);
	}

	@Test
	public void testEntityExistFields_Empty() throws IOException {
		entityExistFieldsMap.put("Candidate", Arrays.asList(""));
		propertyValidation.validateEntityExistFields(entityExistFieldsMap);
	}

	@Test(expected=IllegalArgumentException.class)
	public void testEntityExistFields_BadEntity() throws IOException {
		entityExistFieldsMap.put("BadEntity", Arrays.asList("externalID"));
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
	public void testLowerBoundNumThreads() throws IOException {
		propertyValidation.validateNumThreads(-1);
	}

    @Test(expected = IllegalArgumentException.class)
    public void testUpperBoundNumThreads() throws IOException {
        propertyValidation.validateNumThreads(11);
    }

    @Test
    public void testOptimalNumThreads() {
        final Integer expectedNumThreads = (Runtime.getRuntime().availableProcessors() * 2) + 1;
        Assert.assertEquals(expectedNumThreads, propertyValidation.validateNumThreads(0));
    }
}

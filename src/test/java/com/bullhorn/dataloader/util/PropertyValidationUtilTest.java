package com.bullhorn.dataloader.util;

import com.bullhorn.dataloader.enums.Property;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("ArraysAsListWithZeroOrOneArgument")
public class PropertyValidationUtilTest {

    private PropertyValidationUtil propertyValidationUtil;
    private Map<String, List<String>> entityExistFieldsMap;

    @Before
    public void setup() {
        propertyValidationUtil = new PropertyValidationUtil();

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

    @Test(expected = IllegalArgumentException.class)
    public void testEmptyUserName() {
        propertyValidationUtil.validateRequiredStringField(Property.USERNAME.getName(), "");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMissingUserName() {
        propertyValidationUtil.validateRequiredStringField(Property.USERNAME.getName(), null);
    }

    @Test
    public void testMissingOptionalStringField() {
        String value = propertyValidationUtil.validateOptionalStringField(null);
        Assert.assertEquals("", value);
    }

    @Test
    public void testEntityExistFieldsTrimWhitespace() {
        Assert.assertEquals(" id ", entityExistFieldsMap.get("Candidate").get(0));
        propertyValidationUtil.validateEntityExistFields(entityExistFieldsMap);
        Assert.assertEquals("id", entityExistFieldsMap.get("Candidate").get(0));
    }

    @Test
    public void testEntityExistFieldsMissing() {
        entityExistFieldsMap.remove("BusinessSector");
        propertyValidationUtil.validateEntityExistFields(entityExistFieldsMap);
    }

    @Test
    public void testEntityExistFieldsEmpty() {
        entityExistFieldsMap.put("Candidate", Arrays.asList(""));
        propertyValidationUtil.validateEntityExistFields(entityExistFieldsMap);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEntityExistFieldsBadEntity() {
        entityExistFieldsMap.put("BadEntity", Arrays.asList("externalID"));
        propertyValidationUtil.validateEntityExistFields(entityExistFieldsMap);
    }

    @Test
    public void testNullBooleanProperty() {
        Boolean value = propertyValidationUtil.validateBooleanProperty(null);
        Assert.assertEquals(Boolean.FALSE, value);
    }

    @Test
    public void testValidBooleanPropertyTrue() {
        Boolean value = propertyValidationUtil.validateBooleanProperty(true);
        Assert.assertEquals(Boolean.TRUE, value);
    }

    @Test
    public void testValidBooleanPropertyFalse() {
        Boolean value = propertyValidationUtil.validateBooleanProperty(false);
        Assert.assertEquals(Boolean.FALSE, value);
    }

    @Test
    public void testMissingResultsFilePath() {
        String value = propertyValidationUtil.validateResultsFilePath(null);
        Assert.assertEquals("./results.json", value);
    }

    @Test
    public void testMissingIntervalMsec() {
        Integer value = propertyValidationUtil.validateIntervalMsec(null);
        Assert.assertEquals(new Integer(500), value);
    }

    @Test
    public void testValidIntervalMsec() {
        Integer value = propertyValidationUtil.validateIntervalMsec("250");
        Assert.assertEquals(new Integer(250), value);
    }

    @Test
    public void testValidateNumThreads() {
        Integer actual = propertyValidationUtil.validateNumThreads(0);
        Assert.assertNotEquals(actual, new Integer(0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEmptyNumThreads() {
        propertyValidationUtil.validateNumThreads(Integer.valueOf(""));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testLowerBoundNumThreads() {
        propertyValidationUtil.validateNumThreads(-1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUpperBoundNumThreads() {
        propertyValidationUtil.validateNumThreads(16);
    }

    @Test
    public void testNullWaitSeconds() {
        Integer waitSeconds = propertyValidationUtil.validateWaitSeconds(null);
        Assert.assertEquals(Integer.valueOf(0), waitSeconds);
    }

    @Test
    public void testValidWaitSeconds() {
        Integer waitSeconds = propertyValidationUtil.validateWaitSeconds("3000");
        Assert.assertEquals(Integer.valueOf(3000), waitSeconds);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testLowerBoundWaitSeconds() {
        propertyValidationUtil.validateWaitSeconds("-1");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUpperBoundWaitSeconds() {
        propertyValidationUtil.validateWaitSeconds("3601");
    }
}

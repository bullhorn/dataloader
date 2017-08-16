package com.bullhorn.dataloader.rest;

import com.bullhorn.dataloader.data.Cell;
import com.bullhorn.dataloader.enums.EntityInfo;
import com.bullhornsdk.data.exception.RestApiException;
import com.bullhornsdk.data.model.entity.core.standard.Candidate;
import com.bullhornsdk.data.model.entity.core.standard.ClientContact;
import com.bullhornsdk.data.model.entity.core.standard.JobSubmission;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.text.ParseException;

public class FieldTest {

    private DateTimeFormatter dateTimeFormatter;

    @Before
    public void setup() {
        dateTimeFormatter = DateTimeFormat.forPattern("MM/dd/yyyy");
    }

    @Test
    public void testDirectStringField() throws ParseException, InvocationTargetException, IllegalAccessException {
        Cell cell = new Cell("firstName", "Jack");
        Field field = new Field(EntityInfo.CANDIDATE, cell, false, dateTimeFormatter);

        Assert.assertEquals(field.getEntityInfo(), EntityInfo.CANDIDATE);
        Assert.assertEquals(field.isExistField(), false);
        Assert.assertEquals(field.isToOne(), false);
        Assert.assertEquals(field.isToMany(), false);
        Assert.assertEquals(field.getName(), "firstName");
        Assert.assertEquals(field.getFieldEntity(), EntityInfo.CANDIDATE);
        Assert.assertEquals(field.getFieldType(), String.class);
        Assert.assertEquals(field.getValue(), "Jack");
        Assert.assertEquals(field.getStringValue(), "Jack");

        Candidate candidate = new Candidate();

        Assert.assertEquals(candidate.getFirstName(), null);

        field.populateFieldOnEntity(candidate);

        Assert.assertEquals(candidate.getFirstName(), "Jack");
    }

    @Test
    public void testDirectBooleanField() throws ParseException, InvocationTargetException, IllegalAccessException {
        Cell cell = new Cell("isDeleted", "");
        Field field = new Field(EntityInfo.JOB_SUBMISSION, cell, true, dateTimeFormatter);

        Assert.assertEquals(field.getEntityInfo(), EntityInfo.JOB_SUBMISSION);
        Assert.assertEquals(field.isExistField(), true);
        Assert.assertEquals(field.isToOne(), false);
        Assert.assertEquals(field.isToMany(), false);
        Assert.assertEquals(field.getName(), "isDeleted");
        Assert.assertEquals(field.getFieldEntity(), EntityInfo.JOB_SUBMISSION);
        Assert.assertEquals(field.getFieldType(), Boolean.class);
        Assert.assertEquals(field.getValue(), false);
        Assert.assertEquals(field.getStringValue(), "");

        JobSubmission jobSubmission = new JobSubmission();

        Assert.assertEquals(jobSubmission.getIsDeleted(), null);

        field.populateFieldOnEntity(jobSubmission);

        Assert.assertEquals(jobSubmission.getIsDeleted(), false);
    }

    @Test
    public void testDirectDateTimeField() throws ParseException, InvocationTargetException, IllegalAccessException {
        Cell cell = new Cell("dateAvailable", "02/09/2001");
        Field field = new Field(EntityInfo.CANDIDATE, cell, false, dateTimeFormatter);

        Assert.assertEquals(field.getEntityInfo(), EntityInfo.CANDIDATE);
        Assert.assertEquals(field.isExistField(), false);
        Assert.assertEquals(field.isToOne(), false);
        Assert.assertEquals(field.isToMany(), false);
        Assert.assertEquals(field.getName(), "dateAvailable");
        Assert.assertEquals(field.getFieldEntity(), EntityInfo.CANDIDATE);
        Assert.assertEquals(field.getFieldType(), DateTime.class);
        Assert.assertEquals(field.getValue(), dateTimeFormatter.parseDateTime("02/09/2001"));
        Assert.assertEquals(field.getStringValue(), "02/09/2001");

        Candidate candidate = new Candidate();

        Assert.assertEquals(candidate.getDateAvailable(), null);

        field.populateFieldOnEntity(candidate);

        Assert.assertEquals(candidate.getDateAvailable(), dateTimeFormatter.parseDateTime("02/09/2001"));
    }

    @Test
    public void testToOneBooleanField() throws ParseException, InvocationTargetException, IllegalAccessException {
        Cell cell = new Cell("candidate.isDeleted", "true");
        Field field = new Field(EntityInfo.CANDIDATE_WORK_HISTORY, cell, true, dateTimeFormatter);

        Assert.assertEquals(field.getEntityInfo(), EntityInfo.CANDIDATE_WORK_HISTORY);
        Assert.assertEquals(field.isExistField(), true);
        Assert.assertEquals(field.isToOne(), true);
        Assert.assertEquals(field.isToMany(), false);
        Assert.assertEquals(field.getName(), "isDeleted");
        Assert.assertEquals(field.getFieldEntity(), EntityInfo.CANDIDATE);
        Assert.assertEquals(field.getFieldType(), Boolean.class);
        Assert.assertEquals(field.getValue(), true);
        Assert.assertEquals(field.getStringValue(), "true");

        Candidate candidate = new Candidate();

        Assert.assertEquals(candidate.getIsDeleted(), null);

        field.populateFieldOnEntity(candidate);

        Assert.assertEquals(candidate.getIsDeleted(), true);
    }

    @Test
    public void testToOneBigDecimalField() throws ParseException, InvocationTargetException, IllegalAccessException {
        Cell cell = new Cell("candidate.salary", "123.45");
        Field field = new Field(EntityInfo.CANDIDATE_WORK_HISTORY, cell, false, dateTimeFormatter);

        Assert.assertEquals(field.getEntityInfo(), EntityInfo.CANDIDATE_WORK_HISTORY);
        Assert.assertEquals(field.isExistField(), false);
        Assert.assertEquals(field.isToOne(), true);
        Assert.assertEquals(field.isToMany(), false);
        Assert.assertEquals(field.getName(), "salary");
        Assert.assertEquals(field.getFieldEntity(), EntityInfo.CANDIDATE);
        Assert.assertEquals(field.getFieldType(), BigDecimal.class);
        BigDecimal actual = (BigDecimal) field.getValue();
        Assert.assertEquals(actual.doubleValue(), 123.45, 0.0);
        Assert.assertEquals(field.getStringValue(), "123.45");

        Candidate candidate = new Candidate();

        Assert.assertEquals(candidate.getSalary(), null);

        field.populateFieldOnEntity(candidate);

        Assert.assertEquals(candidate.getSalary().doubleValue(), 123.45, 0.1);
    }

    @Test
    public void testToManyIntegerField() throws ParseException, InvocationTargetException, IllegalAccessException {
        Cell cell = new Cell("candidates.id", "1");
        Field field = new Field(EntityInfo.NOTE, cell, false, dateTimeFormatter);

        Assert.assertEquals(field.getEntityInfo(), EntityInfo.NOTE);
        Assert.assertEquals(field.isExistField(), false);
        Assert.assertEquals(field.isToOne(), false);
        Assert.assertEquals(field.isToMany(), true);
        Assert.assertEquals(field.getName(), "id");
        Assert.assertEquals(field.getFieldEntity(), EntityInfo.CANDIDATE);
        Assert.assertEquals(field.getFieldType(), Integer.class);
        Assert.assertEquals(field.getValue(), 1);
        Assert.assertEquals(field.getStringValue(), "1");

        Candidate candidate = new Candidate();

        Assert.assertEquals(candidate.getId(), null);

        field.populateFieldOnEntity(candidate);

        Assert.assertEquals(candidate.getId(), new Integer(1));
    }

    @Test
    public void testToManyBooleanField() throws ParseException, InvocationTargetException, IllegalAccessException {
        Cell cell = new Cell("clientContacts.isDeleted", "1");
        Field field = new Field(EntityInfo.NOTE, cell, true, dateTimeFormatter);

        Assert.assertEquals(field.getEntityInfo(), EntityInfo.NOTE);
        Assert.assertEquals(field.isExistField(), true);
        Assert.assertEquals(field.isToOne(), false);
        Assert.assertEquals(field.isToMany(), true);
        Assert.assertEquals(field.getName(), "isDeleted");
        Assert.assertEquals(field.getFieldEntity(), EntityInfo.CLIENT_CONTACT);
        Assert.assertEquals(field.getFieldType(), Boolean.class);
        Assert.assertEquals(field.getValue(), true);
        Assert.assertEquals(field.getStringValue(), "1");

        ClientContact clientContact = new ClientContact();

        Assert.assertEquals(clientContact.getIsDeleted(), null);

        field.populateFieldOnEntity(clientContact);

        Assert.assertEquals(clientContact.getIsDeleted(), true);
    }

    @Test
    public void testEmptyInteger() throws ParseException, InvocationTargetException, IllegalAccessException {
        Cell cell = new Cell("id", "");
        Field field = new Field(EntityInfo.CANDIDATE, cell, false, dateTimeFormatter);

        Assert.assertEquals(field.getFieldType(), Integer.class);
        Assert.assertEquals(field.getValue(), 0);
        Assert.assertEquals(field.getStringValue(), "");
    }

    @Test
    public void testMalformedAddressField() throws ParseException, InvocationTargetException, IllegalAccessException {
        RestApiException expectedException = new RestApiException("Invalid address field format: 'countryName'"
            + " Must use 'address.countryName' in csv header");
        RestApiException actualException = null;

        try {
            Cell cell = new Cell("countryName", "Canada");
            new Field(EntityInfo.CANDIDATE, cell, false, dateTimeFormatter);
        } catch (RestApiException e) {
            actualException = e;
        }

        Assert.assertNotNull(actualException);
        Assert.assertEquals(expectedException.getMessage(), actualException.getMessage());
    }

    @Test
    public void testMalformedAddressFieldCaseInsensitive() throws ParseException, InvocationTargetException, IllegalAccessException {
        RestApiException expectedException = new RestApiException("Invalid address field format: 'countryname'"
            + " Must use 'address.countryName' in csv header");
        RestApiException actualException = null;

        try {
            Cell cell = new Cell("countryname", "Canada");
            new Field(EntityInfo.CANDIDATE, cell, false, dateTimeFormatter);
        } catch (RestApiException e) {
            actualException = e;
        }

        Assert.assertNotNull(actualException);
        Assert.assertEquals(expectedException.getMessage(), actualException.getMessage());
    }
}

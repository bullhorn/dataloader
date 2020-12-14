package com.bullhorn.dataloader.rest;

import java.math.BigDecimal;
import java.text.ParseException;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.bullhorn.dataloader.data.Cell;
import com.bullhorn.dataloader.enums.EntityInfo;
import com.bullhornsdk.data.exception.RestApiException;
import com.bullhornsdk.data.model.entity.core.standard.Candidate;
import com.bullhornsdk.data.model.entity.core.standard.CandidateEducation;
import com.bullhornsdk.data.model.entity.core.standard.CandidateWorkHistory;
import com.bullhornsdk.data.model.entity.core.standard.ClientContact;
import com.bullhornsdk.data.model.entity.core.standard.JobSubmission;
import com.bullhornsdk.data.model.entity.core.standard.Note;

public class FieldTest {

    private DateTimeFormatter dateTimeFormatter;

    @Before
    public void setup() {
        dateTimeFormatter = DateTimeFormat.forPattern("MM/dd/yyyy");
    }

    @Test
    public void testDirectStringField() throws Exception {
        Cell cell = new Cell("firstName", "Jack");
        Field field = new Field(EntityInfo.CANDIDATE, cell, false, dateTimeFormatter);

        Assert.assertEquals(field.getEntityInfo(), EntityInfo.CANDIDATE);
        Assert.assertEquals(field.isExistField(), false);
        Assert.assertEquals(field.isToOne(), false);
        Assert.assertEquals(field.isToMany(), false);
        Assert.assertEquals(field.getName(), "firstName");
        Assert.assertEquals(field.getFieldParameterName(true), "firstName");
        Assert.assertEquals(field.getFieldParameterName(false), "firstName");
        Assert.assertEquals(field.getFieldEntity(), EntityInfo.CANDIDATE);
        Assert.assertEquals(field.getFieldType(), String.class);
        Assert.assertEquals(field.getValue(), "Jack");
        Assert.assertEquals(field.getStringValue(), "Jack");

        Candidate candidate = new Candidate();

        Assert.assertNull(candidate.getFirstName());
        Assert.assertEquals(field.getStringValueFromEntity(candidate, ";"), "");

        field.populateFieldOnEntity(candidate);

        Assert.assertEquals(candidate.getFirstName(), "Jack");
        Assert.assertEquals(field.getStringValueFromEntity(candidate, ";"), "Jack");

        field.setExistField(true);
        Assert.assertEquals(field.isExistField(), true);
    }

    @Test
    public void testDirectStringFieldWithBadCapitalization() throws Exception {
        Cell cell = new Cell("FIRSTName", "Jack");
        Field field = new Field(EntityInfo.CANDIDATE, cell, false, dateTimeFormatter);

        Assert.assertEquals(field.getEntityInfo(), EntityInfo.CANDIDATE);
        Assert.assertEquals(field.isExistField(), false);
        Assert.assertEquals(field.isToOne(), false);
        Assert.assertEquals(field.isToMany(), false);
        Assert.assertEquals(field.getName(), "firstName");
        Assert.assertEquals(field.getFieldParameterName(true), "firstName");
        Assert.assertEquals(field.getFieldParameterName(false), "firstName");
        Assert.assertEquals(field.getFieldEntity(), EntityInfo.CANDIDATE);
        Assert.assertEquals(field.getFieldType(), String.class);
        Assert.assertEquals(field.getValue(), "Jack");
        Assert.assertEquals(field.getStringValue(), "Jack");

        Candidate candidate = new Candidate();

        Assert.assertNull(candidate.getFirstName());
        Assert.assertEquals(field.getStringValueFromEntity(candidate, ";"), "");

        field.populateFieldOnEntity(candidate);

        Assert.assertEquals(candidate.getFirstName(), "Jack");
        Assert.assertEquals(field.getStringValueFromEntity(candidate, ";"), "Jack");

        field.setExistField(true);
        Assert.assertEquals(field.isExistField(), true);
    }

    @Test
    public void testDirectBooleanField() throws Exception {
        Cell cell = new Cell("isDeleted", "");
        Field field = new Field(EntityInfo.JOB_SUBMISSION, cell, true, dateTimeFormatter);

        Assert.assertEquals(field.getEntityInfo(), EntityInfo.JOB_SUBMISSION);
        Assert.assertEquals(field.isExistField(), true);
        Assert.assertEquals(field.isToOne(), false);
        Assert.assertEquals(field.isToMany(), false);
        Assert.assertEquals(field.getName(), "isDeleted");
        Assert.assertEquals(field.getFieldParameterName(true), "isDeleted");
        Assert.assertEquals(field.getFieldParameterName(false), "isDeleted");
        Assert.assertEquals(field.getFieldEntity(), EntityInfo.JOB_SUBMISSION);
        Assert.assertEquals(field.getFieldType(), Boolean.class);
        Assert.assertEquals(field.getValue(), false);
        Assert.assertEquals(field.getStringValue(), "");

        JobSubmission jobSubmission = new JobSubmission();

        Assert.assertNull(jobSubmission.getIsDeleted());
        Assert.assertEquals(field.getStringValueFromEntity(jobSubmission, ";"), "");

        field.populateFieldOnEntity(jobSubmission);

        Assert.assertEquals(jobSubmission.getIsDeleted(), false);
        Assert.assertEquals(field.getStringValueFromEntity(jobSubmission, ";"), "false");
    }

    @Test
    public void testDirectDateTimeField() throws Exception {
        Cell cell = new Cell("dateAvailable", "02/09/2001");
        Field field = new Field(EntityInfo.CANDIDATE, cell, false, dateTimeFormatter);

        Assert.assertEquals(field.getEntityInfo(), EntityInfo.CANDIDATE);
        Assert.assertEquals(field.isExistField(), false);
        Assert.assertEquals(field.isToOne(), false);
        Assert.assertEquals(field.isToMany(), false);
        Assert.assertEquals(field.getName(), "dateAvailable");
        Assert.assertEquals(field.getFieldParameterName(true), "dateAvailable");
        Assert.assertEquals(field.getFieldParameterName(false), "dateAvailable");
        Assert.assertEquals(field.getFieldEntity(), EntityInfo.CANDIDATE);
        Assert.assertEquals(field.getFieldType(), DateTime.class);
        Assert.assertEquals(field.getValue(), dateTimeFormatter.parseDateTime("02/09/2001"));
        Assert.assertEquals(field.getStringValue(), "02/09/2001");

        Candidate candidate = new Candidate();

        Assert.assertNull(candidate.getDateAvailable());
        Assert.assertEquals(field.getStringValueFromEntity(candidate, ";"), "");

        field.populateFieldOnEntity(candidate);

        Assert.assertEquals(candidate.getDateAvailable(), dateTimeFormatter.parseDateTime("02/09/2001"));
        Assert.assertEquals(field.getStringValueFromEntity(candidate, ";"), "02/09/2001");
    }

    @Test
    public void testToOneBooleanField() throws Exception {
        Cell cell = new Cell("candidate.isDeleted", "true");
        Field field = new Field(EntityInfo.CANDIDATE_WORK_HISTORY, cell, true, dateTimeFormatter);

        Assert.assertEquals(field.getEntityInfo(), EntityInfo.CANDIDATE_WORK_HISTORY);
        Assert.assertEquals(field.isExistField(), true);
        Assert.assertEquals(field.isToOne(), true);
        Assert.assertEquals(field.isToMany(), false);
        Assert.assertEquals(field.getName(), "isDeleted");
        Assert.assertEquals(field.getFieldParameterName(true), "candidate(isDeleted)");
        Assert.assertEquals(field.getFieldParameterName(false), "isDeleted");
        Assert.assertEquals(field.getFieldEntity(), EntityInfo.CANDIDATE);
        Assert.assertEquals(field.getFieldType(), Boolean.class);
        Assert.assertEquals(field.getValue(), true);
        Assert.assertEquals(field.getStringValue(), "true");

        CandidateWorkHistory candidateWorkHistory = new CandidateWorkHistory();
        Candidate candidate = new Candidate();

        Assert.assertNull(candidateWorkHistory.getCandidate());
        Assert.assertEquals(field.getStringValueFromEntity(candidateWorkHistory, ";"), "");
        Assert.assertEquals(field.getStringValueFromEntity(candidate, ";"), "");

        field.populateAssociationOnEntity(candidateWorkHistory, candidate);

        Assert.assertEquals(candidateWorkHistory.getCandidate().getIsDeleted(), true);
        Assert.assertEquals(field.getStringValueFromEntity(candidateWorkHistory, ";"), "true");
        Assert.assertEquals(field.getStringValueFromEntity(candidate, ";"), "true");
    }

    @Test
    public void testToOneBooleanFieldWithBadCapitalization() throws Exception {
        Cell cell = new Cell("CANDidate.isDeleted", "true");
        Field field = new Field(EntityInfo.CANDIDATE_WORK_HISTORY, cell, true, dateTimeFormatter);

        Assert.assertEquals(field.getEntityInfo(), EntityInfo.CANDIDATE_WORK_HISTORY);
        Assert.assertEquals(field.isExistField(), true);
        Assert.assertEquals(field.isToOne(), true);
        Assert.assertEquals(field.isToMany(), false);
        Assert.assertEquals(field.getName(), "isDeleted");
        Assert.assertEquals(field.getFieldParameterName(true), "candidate(isDeleted)");
        Assert.assertEquals(field.getFieldParameterName(false), "isDeleted");
        Assert.assertEquals(field.getFieldEntity(), EntityInfo.CANDIDATE);
        Assert.assertEquals(field.getFieldType(), Boolean.class);
        Assert.assertEquals(field.getValue(), true);
        Assert.assertEquals(field.getStringValue(), "true");

        CandidateWorkHistory candidateWorkHistory = new CandidateWorkHistory();
        Candidate candidate = new Candidate();

        Assert.assertNull(candidateWorkHistory.getCandidate());
        Assert.assertEquals(field.getStringValueFromEntity(candidateWorkHistory, ";"), "");
        Assert.assertEquals(field.getStringValueFromEntity(candidate, ";"), "");

        field.populateAssociationOnEntity(candidateWorkHistory, candidate);

        Assert.assertEquals(candidateWorkHistory.getCandidate().getIsDeleted(), true);
        Assert.assertEquals(field.getStringValueFromEntity(candidateWorkHistory, ";"), "true");
        Assert.assertEquals(field.getStringValueFromEntity(candidate, ";"), "true");
    }

    @Test
    public void testToOneBigDecimalField() throws Exception {
        Cell cell = new Cell("candidate.salary", "123.45");
        Field field = new Field(EntityInfo.CANDIDATE_WORK_HISTORY, cell, false, dateTimeFormatter);

        Assert.assertEquals(field.getEntityInfo(), EntityInfo.CANDIDATE_WORK_HISTORY);
        Assert.assertEquals(field.isExistField(), false);
        Assert.assertEquals(field.isToOne(), true);
        Assert.assertEquals(field.isToMany(), false);
        Assert.assertEquals(field.getName(), "salary");
        Assert.assertEquals(field.getFieldParameterName(true), "candidate(salary)");
        Assert.assertEquals(field.getFieldParameterName(false), "salary");
        Assert.assertEquals(field.getFieldEntity(), EntityInfo.CANDIDATE);
        Assert.assertEquals(field.getFieldType(), BigDecimal.class);
        BigDecimal actual = (BigDecimal) field.getValue();
        Assert.assertEquals(actual.doubleValue(), 123.45, 0.0);
        Assert.assertEquals(field.getStringValue(), "123.45");

        CandidateWorkHistory candidateWorkHistory = new CandidateWorkHistory();
        Candidate candidate = new Candidate();

        Assert.assertNull(candidateWorkHistory.getCandidate());
        Assert.assertEquals(field.getStringValueFromEntity(candidateWorkHistory, ";"), "");
        Assert.assertEquals(field.getStringValueFromEntity(candidate, ";"), "");

        field.populateAssociationOnEntity(candidateWorkHistory, candidate);

        Assert.assertEquals(candidateWorkHistory.getCandidate().getSalary().doubleValue(), 123.45, 0.1);
        Assert.assertEquals(field.getStringValueFromEntity(candidateWorkHistory, ";"), "123.45");
        Assert.assertEquals(field.getStringValueFromEntity(candidate, ";"), "123.45");
    }

    @Test
    public void testToOneBigDecimalFieldWithBadCapitalization() throws Exception {
        Cell cell = new Cell("candidate.SALary", "123.45");
        Field field = new Field(EntityInfo.CANDIDATE_WORK_HISTORY, cell, false, dateTimeFormatter);

        Assert.assertEquals(field.getEntityInfo(), EntityInfo.CANDIDATE_WORK_HISTORY);
        Assert.assertEquals(field.isExistField(), false);
        Assert.assertEquals(field.isToOne(), true);
        Assert.assertEquals(field.isToMany(), false);
        Assert.assertEquals(field.getName(), "salary");
        Assert.assertEquals(field.getFieldParameterName(true), "candidate(salary)");
        Assert.assertEquals(field.getFieldParameterName(false), "salary");
        Assert.assertEquals(field.getFieldEntity(), EntityInfo.CANDIDATE);
        Assert.assertEquals(field.getFieldType(), BigDecimal.class);
        BigDecimal actual = (BigDecimal) field.getValue();
        Assert.assertEquals(actual.doubleValue(), 123.45, 0.0);
        Assert.assertEquals(field.getStringValue(), "123.45");

        CandidateWorkHistory candidateWorkHistory = new CandidateWorkHistory();
        Candidate candidate = new Candidate();

        Assert.assertNull(candidateWorkHistory.getCandidate());
        Assert.assertEquals(field.getStringValueFromEntity(candidateWorkHistory, ";"), "");
        Assert.assertEquals(field.getStringValueFromEntity(candidate, ";"), "");

        field.populateAssociationOnEntity(candidateWorkHistory, candidate);

        Assert.assertEquals(candidateWorkHistory.getCandidate().getSalary().doubleValue(), 123.45, 0.1);
        Assert.assertEquals(field.getStringValueFromEntity(candidateWorkHistory, ";"), "123.45");
        Assert.assertEquals(field.getStringValueFromEntity(candidate, ";"), "123.45");
    }

    @Test
    public void testToManyIntegerField() throws Exception {
        Cell cell = new Cell("candidates.id", "101");
        Field field = new Field(EntityInfo.NOTE, cell, false, dateTimeFormatter);

        Assert.assertEquals(field.getEntityInfo(), EntityInfo.NOTE);
        Assert.assertEquals(field.isExistField(), false);
        Assert.assertEquals(field.isToOne(), false);
        Assert.assertEquals(field.isToMany(), true);
        Assert.assertEquals(field.getName(), "id");
        Assert.assertEquals(field.getFieldParameterName(true), "candidates(id)");
        Assert.assertEquals(field.getFieldParameterName(false), "id");
        Assert.assertEquals(field.getFieldEntity(), EntityInfo.CANDIDATE);
        Assert.assertEquals(field.getFieldType(), Integer.class);
        Assert.assertEquals(field.getValue(), 101);
        Assert.assertEquals(field.getStringValue(), "101");

        Note note = new Note();
        Candidate candidate1 = new Candidate();
        Candidate candidate2 = new Candidate();
        Candidate candidate3 = new Candidate();

        Assert.assertNull(candidate1.getId());
        Assert.assertNull(candidate2.getId());
        Assert.assertNull(candidate3.getId());
        Assert.assertNull(note.getCandidates());
        Assert.assertEquals(field.getStringValueFromEntity(note, ";"), "");
        Assert.assertEquals(field.getStringValueFromEntity(candidate1, ";"), "");
        Assert.assertEquals(field.getStringValueFromEntity(candidate2, ";"), "");
        Assert.assertEquals(field.getStringValueFromEntity(candidate3, ";"), "");

        field.populateAssociationOnEntity(note, candidate1);
        field.populateAssociationOnEntity(note, candidate2);
        field.populateAssociationOnEntity(note, candidate3);

        Assert.assertEquals(note.getCandidates().getData().get(0).getId(), new Integer(101));
        Assert.assertEquals(note.getCandidates().getData().get(1).getId(), new Integer(101));
        Assert.assertEquals(note.getCandidates().getData().get(2).getId(), new Integer(101));

        // The single value in the field will be set for each populated To-Many object
        Assert.assertEquals(field.getStringValueFromEntity(note, ";"), "101;101;101");
        Assert.assertEquals(field.getStringValueFromEntity(candidate1, ";"), "101");
        Assert.assertEquals(field.getStringValueFromEntity(candidate2, ";"), "101");
        Assert.assertEquals(field.getStringValueFromEntity(candidate3, ";"), "101");
    }

    @Test
    public void testToManyBooleanField() throws Exception {
        Cell cell = new Cell("clientContacts.isDeleted", "1");
        Field field = new Field(EntityInfo.NOTE, cell, true, dateTimeFormatter);

        Assert.assertEquals(field.getEntityInfo(), EntityInfo.NOTE);
        Assert.assertEquals(field.isExistField(), true);
        Assert.assertEquals(field.isToOne(), false);
        Assert.assertEquals(field.isToMany(), true);
        Assert.assertEquals(field.getName(), "isDeleted");
        Assert.assertEquals(field.getFieldParameterName(true), "clientContacts(isDeleted)");
        Assert.assertEquals(field.getFieldParameterName(false), "isDeleted");
        Assert.assertEquals(field.getFieldEntity(), EntityInfo.CLIENT_CONTACT);
        Assert.assertEquals(field.getFieldType(), Boolean.class);
        Assert.assertEquals(field.getValue(), true);
        Assert.assertEquals(field.getStringValue(), "1");

        Note note = new Note();
        ClientContact contact1 = new ClientContact();
        ClientContact contact2 = new ClientContact();
        ClientContact contact3 = new ClientContact();

        Assert.assertNull(contact1.getIsDeleted());
        Assert.assertNull(contact2.getIsDeleted());
        Assert.assertNull(contact3.getIsDeleted());
        Assert.assertEquals(field.getStringValueFromEntity(note, ";"), "");
        Assert.assertEquals(field.getStringValueFromEntity(contact1, ";"), "");
        Assert.assertEquals(field.getStringValueFromEntity(contact2, ";"), "");
        Assert.assertEquals(field.getStringValueFromEntity(contact3, ";"), "");

        field.populateAssociationOnEntity(note, contact1);
        field.populateAssociationOnEntity(note, contact2);
        field.populateAssociationOnEntity(note, contact3);

        Assert.assertEquals(true, note.getClientContacts().getData().get(0).getIsDeleted());
        Assert.assertEquals(true, note.getClientContacts().getData().get(1).getIsDeleted());
        Assert.assertEquals(true, note.getClientContacts().getData().get(2).getIsDeleted());

        // The single value in the field will be set for each populated To-Many object
        Assert.assertEquals(field.getStringValueFromEntity(note, ";"), "true;true;true");
        Assert.assertEquals(field.getStringValueFromEntity(contact1, ";"), "true");
        Assert.assertEquals(field.getStringValueFromEntity(contact2, ";"), "true");
        Assert.assertEquals(field.getStringValueFromEntity(contact3, ";"), "true");
    }

    @Test
    public void testEmptyInteger() throws ParseException {
        Cell cell = new Cell("id", "");
        Field field = new Field(EntityInfo.CANDIDATE, cell, false, dateTimeFormatter);

        Assert.assertEquals(field.getFieldType(), Integer.class);
        Assert.assertEquals(field.getValue(), 0);
        Assert.assertEquals(field.getStringValue(), "");
    }

    @Test
    public void testAddressField() throws Exception {
        Cell cell = new Cell("address.address1", "100 Summer St.");
        Field field = new Field(EntityInfo.CANDIDATE, cell, true, dateTimeFormatter);

        Assert.assertEquals(field.getEntityInfo(), EntityInfo.CANDIDATE);
        Assert.assertEquals(field.isExistField(), true);
        Assert.assertEquals(field.isToOne(), false);
        Assert.assertEquals(field.isToMany(), false);
        Assert.assertEquals(field.getName(), "address1");
        Assert.assertEquals(field.getFieldParameterName(true), "address(address1)");
        Assert.assertEquals(field.getFieldParameterName(false), "address1");
        Assert.assertEquals(field.getFieldEntity(), EntityInfo.ADDRESS);
        Assert.assertEquals(field.getFieldType(), String.class);
        Assert.assertEquals(field.getValue(), "100 Summer St.");
        Assert.assertEquals(field.getStringValue(), "100 Summer St.");

        Candidate candidate = new Candidate();

        Assert.assertNull(candidate.getAddress());

        field.populateFieldOnEntity(candidate);

        Assert.assertEquals(candidate.getAddress().getAddress1(), "100 Summer St.");
    }

    @Test
    public void testAddressFieldCountryId() throws Exception {
        Cell cell = new Cell("address.countryId", "1234");
        Field field = new Field(EntityInfo.CANDIDATE, cell, true, dateTimeFormatter);

        Assert.assertEquals(field.getEntityInfo(), EntityInfo.CANDIDATE);
        Assert.assertEquals(field.isExistField(), true);
        Assert.assertEquals(field.isToOne(), false);
        Assert.assertEquals(field.isToMany(), false);
        Assert.assertEquals(field.getName(), "countryID");
        Assert.assertEquals(field.getFieldParameterName(true), "address(countryID)");
        Assert.assertEquals(field.getFieldParameterName(false), "countryID");
        Assert.assertEquals(field.getFieldEntity(), EntityInfo.ADDRESS);
        Assert.assertEquals(field.getFieldType(), Integer.class);
        Assert.assertEquals(field.getValue(), 1234);
        Assert.assertEquals(field.getStringValue(), "1234");

        Candidate candidate = new Candidate();

        Assert.assertNull(candidate.getAddress());

        field.populateFieldOnEntity(candidate);

        Assert.assertEquals(candidate.getAddress().getCountryID(), new Integer(1234));
    }

    @Test
    public void testMultipleAddressFields() throws Exception {
        Cell address1Cell = new Cell("address.address1", "100 Summer St.");
        Cell cityCell = new Cell("address.city", "Boston");
        Cell stateCell = new Cell("address.state", "MA");
        Cell secAddress1Cell = new Cell("secondaryAddress.address1", "200 S. Hanley");
        Cell secCityCell = new Cell("secondaryAddress.city", "Clayton");
        Cell secStateCell = new Cell("secondaryAddress.state", "MO");

        Field address1 = new Field(EntityInfo.CANDIDATE, address1Cell, false, dateTimeFormatter);
        Field city = new Field(EntityInfo.CANDIDATE, cityCell, false, dateTimeFormatter);
        Field state = new Field(EntityInfo.CANDIDATE, stateCell, false, dateTimeFormatter);
        Field secAddress1 = new Field(EntityInfo.CANDIDATE, secAddress1Cell, false, dateTimeFormatter);
        Field secCity = new Field(EntityInfo.CANDIDATE, secCityCell, false, dateTimeFormatter);
        Field secState = new Field(EntityInfo.CANDIDATE, secStateCell, false, dateTimeFormatter);

        Candidate candidate = new Candidate();

        Assert.assertNull(candidate.getAddress());
        Assert.assertNull(candidate.getSecondaryAddress());

        address1.populateFieldOnEntity(candidate);
        city.populateFieldOnEntity(candidate);
        state.populateFieldOnEntity(candidate);
        secAddress1.populateFieldOnEntity(candidate);
        secCity.populateFieldOnEntity(candidate);
        secState.populateFieldOnEntity(candidate);

        Assert.assertEquals(candidate.getAddress().getAddress1(), "100 Summer St.");
        Assert.assertEquals(candidate.getAddress().getCity(), "Boston");
        Assert.assertEquals(candidate.getAddress().getState(), "MA");
        Assert.assertEquals(candidate.getSecondaryAddress().getAddress1(), "200 S. Hanley");
        Assert.assertEquals(candidate.getSecondaryAddress().getCity(), "Clayton");
        Assert.assertEquals(candidate.getSecondaryAddress().getState(), "MO");
    }

    @Test
    public void testMalformedAddressField() {
        RestApiException expectedException = new RestApiException(
            "Invalid address field format: 'countryName'. Must use: 'address.countryName' to set an address field.");
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
    public void testMalformedAddressFieldCaseInsensitive() {
        RestApiException expectedException = new RestApiException(
            "Invalid address field format: 'countryname'. Must use: 'address.countryName' to set an address field.");
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

    @Test
    public void testDirectCityStateFieldsDoNotThrowException() throws Exception {
        Cell cell = new Cell("state", "MO");
        Field field = new Field(EntityInfo.CANDIDATE_EDUCATION, cell, false, dateTimeFormatter);

        Assert.assertEquals(field.getEntityInfo(), EntityInfo.CANDIDATE_EDUCATION);
        Assert.assertEquals(field.isExistField(), false);
        Assert.assertEquals(field.isToOne(), false);
        Assert.assertEquals(field.isToMany(), false);
        Assert.assertEquals(field.getName(), "state");
        Assert.assertEquals(field.getFieldParameterName(true), "state");
        Assert.assertEquals(field.getFieldParameterName(false), "state");
        Assert.assertEquals(field.getFieldEntity(), EntityInfo.CANDIDATE_EDUCATION);
        Assert.assertEquals(field.getFieldType(), String.class);
        Assert.assertEquals(field.getValue(), "MO");
        Assert.assertEquals(field.getStringValue(), "MO");

        CandidateEducation candidateEducation = new CandidateEducation();

        Assert.assertNull(candidateEducation.getState());

        field.populateFieldOnEntity(candidateEducation);

        Assert.assertEquals(candidateEducation.getState(), "MO");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWrongEntityTypeException() throws Exception {
        Cell cell = new Cell("firstName", "Jack");
        Field field = new Field(EntityInfo.CANDIDATE, cell, false, dateTimeFormatter);

        ClientContact clientContact = new ClientContact();

        Assert.assertEquals(field.getStringValueFromEntity(clientContact, ";"), "");
    }
}

package com.bullhorn.dataloader.service;

import com.bullhorn.dataloader.TestUtils;
import com.bullhorn.dataloader.enums.Command;
import com.bullhorn.dataloader.enums.EntityInfo;
import com.bullhorn.dataloader.rest.CompleteUtil;
import com.bullhorn.dataloader.rest.RestApi;
import com.bullhorn.dataloader.rest.RestSession;
import com.bullhorn.dataloader.util.PrintUtil;
import com.bullhorn.dataloader.util.PropertyFileUtil;
import com.bullhorn.dataloader.util.Timer;
import com.bullhorn.dataloader.util.ValidationUtil;
import com.bullhornsdk.data.exception.RestApiException;
import com.bullhornsdk.data.model.entity.core.standard.Candidate;
import com.bullhornsdk.data.model.entity.core.standard.Lead;
import com.bullhornsdk.data.model.entity.meta.Field;
import com.bullhornsdk.data.model.entity.meta.StandardMetaData;
import com.bullhornsdk.data.model.enums.MetaParameter;
import com.csvreader.CsvReader;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TemplateServiceTest {

    private PrintUtil printUtilMock;
    private RestApi restApiMock;
    private RestSession restSessionMock;
    private TemplateService templateService;

    @Before
    public void setup() throws IOException {
        CompleteUtil completeUtilMock = mock(CompleteUtil.class);
        InputStream inputStreamMock = mock(InputStream.class);
        printUtilMock = mock(PrintUtil.class);
        ProcessRunner processRunnerMock = mock(ProcessRunner.class);
        PropertyFileUtil propertyFileUtilMock = mock(PropertyFileUtil.class);
        restApiMock = mock(RestApi.class);
        restSessionMock = mock(RestSession.class);
        Timer timerMock = mock(Timer.class);
        ValidationUtil validationUtil = new ValidationUtil(printUtilMock);

        templateService = new TemplateService(printUtilMock, propertyFileUtilMock, validationUtil,
            completeUtilMock, restSessionMock, processRunnerMock, inputStreamMock, timerMock);

        // Mock out meta
        Field idField = new Field();
        idField.setName("id");
        idField.setDataType("Integer");
        idField.setType("SCALAR");

        Field emailField = new Field();
        emailField.setName("email");
        emailField.setDataType("String");
        emailField.setType("SCALAR");

        Field externalIdField = new Field();
        externalIdField.setName("externalID");
        externalIdField.setDataType("String");
        externalIdField.setType("SCALAR");

        Field commentsField = new Field();
        commentsField.setName("comments");
        commentsField.setDataType("String");
        commentsField.setType("SCALAR");

        // Mock out Candidate meta data
        StandardMetaData<Candidate> candidateMeta = new StandardMetaData<>();
        candidateMeta.setEntity("Candidate");
        candidateMeta.setFields(Arrays.asList(idField, emailField, externalIdField, commentsField));

        // Mock out Lead meta data
        StandardMetaData<Lead> leadMeta = new StandardMetaData<>();
        leadMeta.setEntity("Candidate");
        leadMeta.setFields(Collections.singletonList(commentsField));

        when(restSessionMock.getRestApi()).thenReturn(restApiMock);
        when(restApiMock.getMetaData(Candidate.class, MetaParameter.FULL, null)).thenReturn(candidateMeta);
        when(restApiMock.getMetaData(Lead.class, MetaParameter.FULL, null)).thenReturn(leadMeta);
    }

    @Test
    public void testRunCandidate() throws IOException {
        String[] testArgs = {Command.TEMPLATE.getMethodName(), EntityInfo.CANDIDATE.getEntityName()};

        templateService.run(testArgs);

        verify(printUtilMock, times(2)).printAndLog(anyString());

        String fileName = EntityInfo.CANDIDATE.getEntityName() + "Example.csv";
        File outputFile = new File(fileName);
        Assert.assertTrue(outputFile.isFile());

        CsvReader csvReader = new CsvReader(fileName);
        csvReader.readHeaders();
        csvReader.readRecord();
        Assert.assertEquals(3, csvReader.getHeaders().length);
        Assert.assertEquals(3, csvReader.getValues().length);
    }

    @Test
    public void testRunLead() throws IOException {
        String[] testArgs = {Command.TEMPLATE.getMethodName(), EntityInfo.LEAD.getEntityName()};

        templateService.run(testArgs);

        verify(printUtilMock, times(2)).printAndLog(anyString());

        String fileName = EntityInfo.LEAD.getEntityName() + "Example.csv";
        File outputFile = new File(fileName);
        Assert.assertTrue(outputFile.isFile());

        CsvReader csvReader = new CsvReader(fileName);
        csvReader.readHeaders();
        csvReader.readRecord();
        Assert.assertEquals(1, csvReader.getHeaders().length);
        Assert.assertEquals(1, csvReader.getValues().length);
        Assert.assertEquals("comments", csvReader.getHeaders()[0]);
        Assert.assertEquals("String", csvReader.getValues()[0]);
    }

    @Test
    public void testRunCandidateExampleFileCompare() {
        String filePath = TestUtils.getResourceFilePath("Candidate.csv");
        templateService.run(new String[]{Command.TEMPLATE.getMethodName(), filePath});

        ArgumentCaptor<String> stringArgumentCaptor = ArgumentCaptor.forClass(String.class);
        verify(printUtilMock, times(8)).printAndLog(stringArgumentCaptor.capture());
        List<String> lines = stringArgumentCaptor.getAllValues();

        Assert.assertTrue(lines.get(0).startsWith("Comparing latest Candidate meta against example file: "));
        Assert.assertTrue(lines.get(1).contains("Headers in Rest that are not in example file:"));
        Assert.assertTrue(lines.get(2).contains("comments"));
        Assert.assertTrue(lines.get(3).contains("externalID"));
        Assert.assertTrue(lines.get(4).contains("Headers in example file that are not in Rest:"));
        Assert.assertTrue(lines.get(5).contains("firstName"));
        Assert.assertTrue(lines.get(6).contains("id"));
        Assert.assertTrue(lines.get(7).contains("lastName"));
    }

    @Test
    public void testRunBadConnection() {
        when(restSessionMock.getRestApi()).thenThrow(new RestApiException());

        templateService.run(new String[]{Command.TEMPLATE.getMethodName(), "Candidate"});

        verify(printUtilMock, times(1)).printAndLog("Failed to create REST session.");
    }

    @Test
    public void testRunBadEntity() {
        IllegalArgumentException expectedException = new IllegalArgumentException("invalid command line arguments");
        Exception actualException = null;

        try {
            templateService.run(new String[]{Command.TEMPLATE.getMethodName(), "Cornidate"});
        } catch (Exception e) {
            actualException = e;
        }

        Assert.assertNotNull(actualException);
        Assert.assertEquals(expectedException.getMessage(), actualException.getMessage());
        verify(printUtilMock, times(1)).printAndLog("ERROR: Template requested is not valid: \"Cornidate\" is not a valid entity.");
    }

    @Test
    public void testRunMetaCallException() {
        when(restApiMock.getMetaData(Candidate.class, MetaParameter.FULL, null))
            .thenThrow(new RestApiException("Meta Error"));

        templateService.run(new String[]{Command.TEMPLATE.getMethodName(), "Candidate"});

        verify(printUtilMock, times(1)).printAndLog("Creating Template for Candidate...");
        verify(printUtilMock, times(1)).printAndLog("ERROR: Failed to create template for Candidate");
    }

    @Test
    public void testRunMetaCallExceptionExampleFile() {
        when(restApiMock.getMetaData(Candidate.class, MetaParameter.FULL, null))
            .thenThrow(new RestApiException("Meta Error"));

        String filePath = TestUtils.getResourceFilePath("Candidate.csv");
        templateService.run(new String[]{Command.TEMPLATE.getMethodName(), filePath});

        verify(printUtilMock, times(1)).printAndLog("Comparing latest Candidate meta against example file: " + filePath + "...");
        verify(printUtilMock, times(1)).printAndLog("ERROR: Failed to compare meta to example file: " + filePath);
    }

    @Test
    public void testIsValidArguments() {
        String[] testArgs = {Command.TEMPLATE.getMethodName(), "Candidate"};

        boolean actualResult = templateService.isValidArguments(testArgs);

        Assert.assertTrue(actualResult);
        verify(printUtilMock, never()).printAndLog(anyString());
    }

    @Test
    public void testIsValidArgumentsMissingArgument() {
        String[] testArgs = {Command.TEMPLATE.getMethodName()};

        boolean actualResult = templateService.isValidArguments(testArgs);

        Assert.assertFalse(actualResult);
        verify(printUtilMock, times(1)).printAndLog(anyString());
    }

    @Test
    public void testIsValidArgumentsTooManyArguments() {
        String[] testArgs = {Command.TEMPLATE.getMethodName(), "Candidate", "tooMany"};

        boolean actualResult = templateService.isValidArguments(testArgs);

        Assert.assertFalse(actualResult);
        verify(printUtilMock, times(1)).printAndLog(anyString());
    }

    @Test
    public void testIsValidArgumentsBadEntity() {
        String[] testArgs = {Command.TEMPLATE.getMethodName(), "BadActors"};

        boolean actualResult = templateService.isValidArguments(testArgs);

        Assert.assertFalse(actualResult);
        verify(printUtilMock, times(1)).printAndLog(anyString());
    }

    @Test
    public void testIsValidArgumentsEmptyEntity() {
        String[] testArgs = {Command.TEMPLATE.getMethodName(), ""};

        boolean actualResult = templateService.isValidArguments(testArgs);

        Assert.assertFalse(actualResult);
        verify(printUtilMock, times(1)).printAndLog(anyString());
    }
}

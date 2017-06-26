package com.bullhorn.dataloader.service;

import com.bullhorn.dataloader.enums.Command;
import com.bullhorn.dataloader.rest.CompleteCall;
import com.bullhorn.dataloader.rest.RestApi;
import com.bullhorn.dataloader.rest.RestSession;
import com.bullhorn.dataloader.util.PrintUtil;
import com.bullhorn.dataloader.util.PropertyFileUtil;
import com.bullhorn.dataloader.util.Timer;
import com.bullhorn.dataloader.util.ValidationUtil;
import com.bullhornsdk.data.exception.RestApiException;
import com.bullhornsdk.data.model.entity.core.standard.Candidate;
import com.bullhornsdk.data.model.entity.meta.Field;
import com.bullhornsdk.data.model.entity.meta.StandardMetaData;
import com.bullhornsdk.data.model.enums.MetaParameter;
import com.csvreader.CsvReader;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.InputStream;
import java.util.Collections;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TemplateServiceTest {

    private RestSession restSessionMock;
    private PrintUtil printUtilMock;
    private TemplateService templateService;

    @Before
    public void setup() throws Exception {
        RestApi restApiMock = mock(RestApi.class);
        CompleteCall completeCallMock = mock(CompleteCall.class);
        restSessionMock = mock(RestSession.class);
        InputStream inputStreamMock = mock(InputStream.class);
        printUtilMock = mock(PrintUtil.class);
        ProcessRunner processRunnerMock = mock(ProcessRunner.class);
        PropertyFileUtil propertyFileUtilMock = mock(PropertyFileUtil.class);
        Timer timerMock = mock(Timer.class);
        ValidationUtil validationUtil = new ValidationUtil(printUtilMock);

        templateService = new TemplateService(printUtilMock, propertyFileUtilMock, validationUtil, completeCallMock, restSessionMock, processRunnerMock, inputStreamMock, timerMock);

        // Mock out candidate meta data
        StandardMetaData<Candidate> metaData = new StandardMetaData<>();
        metaData.setEntity("Candidate");
        Field field = new Field();
        field.setName("comments");
        field.setDataType("String");
        field.setType("SCALAR");
        metaData.setFields(Collections.singletonList(field));

        when(restSessionMock.getRestApi()).thenReturn(restApiMock);
        when(restApiMock.getMetaData(Candidate.class, MetaParameter.FULL, null)).thenReturn(metaData);
    }

    @Test
    public void testRun() throws Exception {
        String entity = "Candidate";
        String dataType = "String";
        String[] testArgs = {Command.TEMPLATE.getMethodName(), entity};

        templateService.run(testArgs);

        verify(printUtilMock, times(2)).printAndLog(anyString());

        String fileName = entity + "Example.csv";
        File outputFile = new File(fileName);
        Assert.assertTrue(outputFile.isFile());

        CsvReader csvReader = new CsvReader(fileName);
        csvReader.readHeaders();
        csvReader.readRecord();
        Assert.assertEquals(dataType, csvReader.getValues()[0]);

        // Cleanup test files
        Boolean isDeleted = outputFile.delete();
        Assert.assertTrue(isDeleted);
    }

    @Test
    public void testRun_BadConnection() throws Exception {
        when(restSessionMock.getRestApi()).thenThrow(new RestApiException());

        templateService.run(new String[] {Command.TEMPLATE.getMethodName(), "Candidate"});

        verify(printUtilMock, times(1)).printAndLog("Failed to create REST session.");
    }

    @Test
    public void testIsValidArguments() throws Exception {
        String[] testArgs = {Command.TEMPLATE.getMethodName(), "Candidate"};

        Boolean actualResult = templateService.isValidArguments(testArgs);

        Assert.assertTrue(actualResult);
        verify(printUtilMock, never()).printAndLog(anyString());
    }

    @Test
    public void testIsValidArgumentsMissingArgument() throws Exception {
        String[] testArgs = {Command.TEMPLATE.getMethodName()};

        Boolean actualResult = templateService.isValidArguments(testArgs);

        Assert.assertFalse(actualResult);
        verify(printUtilMock, times(1)).printAndLog(anyString());
    }

    @Test
    public void testIsValidArgumentsTooManyArgments() throws Exception {
        String[] testArgs = {Command.TEMPLATE.getMethodName(), "Candidate", "tooMany"};

        Boolean actualResult = templateService.isValidArguments(testArgs);

        Assert.assertFalse(actualResult);
        verify(printUtilMock, times(1)).printAndLog(anyString());
    }

    @Test
    public void testIsValidArgumentsBadEntity() throws Exception {
        String[] testArgs = {Command.TEMPLATE.getMethodName(), "BadActors"};

        Boolean actualResult = templateService.isValidArguments(testArgs);

        Assert.assertFalse(actualResult);
        verify(printUtilMock, times(1)).printAndLog(anyString());
    }

    @Test
    public void testIsValidArgumentsEmptyEntity() throws Exception {
        String[] testArgs = {Command.TEMPLATE.getMethodName(), ""};

        Boolean actualResult = templateService.isValidArguments(testArgs);

        Assert.assertFalse(actualResult);
        verify(printUtilMock, times(1)).printAndLog(anyString());
    }
}

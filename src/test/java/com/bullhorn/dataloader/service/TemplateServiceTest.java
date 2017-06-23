package com.bullhorn.dataloader.service;

import com.bullhorn.dataloader.enums.Command;
import com.bullhorn.dataloader.rest.BullhornRestApi;
import com.bullhorn.dataloader.util.CompleteUtil;
import com.bullhorn.dataloader.util.ConnectionUtil;
import com.bullhorn.dataloader.util.PrintUtil;
import com.bullhorn.dataloader.util.ProcessRunnerUtil;
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
import org.mockito.Mockito;

import java.io.File;
import java.io.InputStream;
import java.util.Collections;

public class TemplateServiceTest {

    private ConnectionUtil connectionUtilMock;
    private PrintUtil printUtilMock;
    private TemplateService templateService;

    @Before
    public void setup() throws Exception {
        BullhornRestApi bullhornRestApiMock = Mockito.mock(BullhornRestApi.class);
        CompleteUtil completeUtilMock = Mockito.mock(CompleteUtil.class);
        connectionUtilMock = Mockito.mock(ConnectionUtil.class);
        InputStream inputStreamMock = Mockito.mock(InputStream.class);
        printUtilMock = Mockito.mock(PrintUtil.class);
        ProcessRunnerUtil processRunnerUtilMock = Mockito.mock(ProcessRunnerUtil.class);
        PropertyFileUtil propertyFileUtilMock = Mockito.mock(PropertyFileUtil.class);
        Timer timerMock = Mockito.mock(Timer.class);
        ValidationUtil validationUtil = new ValidationUtil(printUtilMock);

        templateService = new TemplateService(printUtilMock, propertyFileUtilMock, validationUtil, completeUtilMock, connectionUtilMock, processRunnerUtilMock, inputStreamMock, timerMock);

        // Mock out candidate meta data
        StandardMetaData<Candidate> metaData = new StandardMetaData<>();
        metaData.setEntity("Candidate");
        Field field = new Field();
        field.setName("comments");
        field.setDataType("String");
        field.setType("SCALAR");
        metaData.setFields(Collections.singletonList(field));

        Mockito.when(connectionUtilMock.getSession()).thenReturn(bullhornRestApiMock);
        Mockito.when(bullhornRestApiMock.getMetaData(Candidate.class, MetaParameter.FULL, null)).thenReturn(metaData);
    }

    @Test
    public void testRun() throws Exception {
        String entity = "Candidate";
        String dataType = "String";
        String[] testArgs = {Command.TEMPLATE.getMethodName(), entity};

        templateService.run(testArgs);

        Mockito.verify(printUtilMock, Mockito.times(2)).printAndLog(Mockito.anyString());

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
        Mockito.when(connectionUtilMock.getSession()).thenThrow(new RestApiException());

        templateService.run(new String[] {Command.TEMPLATE.getMethodName(), "Candidate"});

        Mockito.verify(printUtilMock, Mockito.times(1)).printAndLog("Failed to create REST session.");
    }

    @Test
    public void testIsValidArguments() throws Exception {
        String[] testArgs = {Command.TEMPLATE.getMethodName(), "Candidate"};

        Boolean actualResult = templateService.isValidArguments(testArgs);

        Assert.assertTrue(actualResult);
        Mockito.verify(printUtilMock, Mockito.never()).printAndLog(Mockito.anyString());
    }

    @Test
    public void testIsValidArgumentsMissingArgument() throws Exception {
        String[] testArgs = {Command.TEMPLATE.getMethodName()};

        Boolean actualResult = templateService.isValidArguments(testArgs);

        Assert.assertFalse(actualResult);
        Mockito.verify(printUtilMock, Mockito.times(1)).printAndLog(Mockito.anyString());
    }

    @Test
    public void testIsValidArgumentsTooManyArgments() throws Exception {
        String[] testArgs = {Command.TEMPLATE.getMethodName(), "Candidate", "tooMany"};

        Boolean actualResult = templateService.isValidArguments(testArgs);

        Assert.assertFalse(actualResult);
        Mockito.verify(printUtilMock, Mockito.times(1)).printAndLog(Mockito.anyString());
    }

    @Test
    public void testIsValidArgumentsBadEntity() throws Exception {
        String[] testArgs = {Command.TEMPLATE.getMethodName(), "BadActors"};

        Boolean actualResult = templateService.isValidArguments(testArgs);

        Assert.assertFalse(actualResult);
        Mockito.verify(printUtilMock, Mockito.times(1)).printAndLog(Mockito.anyString());
    }

    @Test
    public void testIsValidArgumentsEmptyEntity() throws Exception {
        String[] testArgs = {Command.TEMPLATE.getMethodName(), ""};

        Boolean actualResult = templateService.isValidArguments(testArgs);

        Assert.assertFalse(actualResult);
        Mockito.verify(printUtilMock, Mockito.times(1)).printAndLog(Mockito.anyString());
    }
}

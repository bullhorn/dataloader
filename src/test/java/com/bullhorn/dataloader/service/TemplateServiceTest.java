package com.bullhorn.dataloader.service;

import com.bullhorn.dataloader.enums.Command;
import com.bullhorn.dataloader.service.executor.BullhornRestApi;
import com.bullhorn.dataloader.util.CompleteUtil;
import com.bullhorn.dataloader.util.ConnectionUtil;
import com.bullhorn.dataloader.util.PrintUtil;
import com.bullhorn.dataloader.util.PropertyFileUtil;
import com.bullhorn.dataloader.util.Timer;
import com.bullhorn.dataloader.util.validation.ValidationUtil;
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

import static org.mockito.Mockito.when;

public class TemplateServiceTest {

    private CompleteUtil completeUtilMock;
    private ConnectionUtil connectionUtilMock;
    private InputStream inputStreamMock;
    private PrintUtil printUtilMock;
    private PropertyFileUtil propertyFileUtilMock;
    private Timer timerMock;
    private ValidationUtil validationUtil;

    @Before
    public void setup() throws Exception {
        completeUtilMock = Mockito.mock(CompleteUtil.class);
        connectionUtilMock = Mockito.mock(ConnectionUtil.class);
        inputStreamMock = Mockito.mock(InputStream.class);
        printUtilMock = Mockito.mock(PrintUtil.class);
        propertyFileUtilMock = Mockito.mock(PropertyFileUtil.class);
        timerMock = Mockito.mock(Timer.class);
        validationUtil = new ValidationUtil(printUtilMock);

        StandardMetaData<Candidate> metaData = new StandardMetaData<>();
        metaData.setEntity("Candidate");
        Field field = new Field();
        field.setName("comments");
        field.setDataType("String");
        field.setType("SCALAR");
        metaData.setFields(Collections.singletonList(field));

        BullhornRestApi bullhornRestApiMock = Mockito.mock(BullhornRestApi.class);
        when(connectionUtilMock.connect()).thenReturn(bullhornRestApiMock);
        when(bullhornRestApiMock.getMetaData(Candidate.class, MetaParameter.FULL, null)).thenReturn(metaData);
    }

    @Test
    public void testRun() throws Exception {
        final String entity = "Candidate";
        final String dataType = "String";
        final String[] testArgs = {Command.TEMPLATE.getMethodName(), entity};

        TemplateService templateService = new TemplateService(printUtilMock, propertyFileUtilMock, validationUtil, completeUtilMock, connectionUtilMock, inputStreamMock, timerMock);
        templateService.run(testArgs);

        Mockito.verify(printUtilMock, Mockito.times(2)).printAndLog(Mockito.anyString());
        final String fileName = entity + "Example.csv";
        final File outputFile = new File(fileName);

        Assert.assertTrue(outputFile.isFile());

        final CsvReader csvReader = new CsvReader(fileName);
        csvReader.readHeaders();
        csvReader.readRecord();
        Assert.assertEquals(dataType, csvReader.getValues()[0]);

        // Cleanup test files
        outputFile.delete();
    }

    @Test
    public void testIsValidArguments() throws Exception {
        final String[] testArgs = {Command.TEMPLATE.getMethodName(), "Candidate"};

        TemplateService templateService = new TemplateService(printUtilMock, propertyFileUtilMock, validationUtil, completeUtilMock, connectionUtilMock, inputStreamMock, timerMock);
        final boolean actualResult = templateService.isValidArguments(testArgs);

        Assert.assertTrue(actualResult);
        Mockito.verify(printUtilMock, Mockito.never()).printAndLog(Mockito.anyString());
    }

    @Test
    public void testIsValidArgumentsMissingArgument() throws Exception {
        final String[] testArgs = {Command.TEMPLATE.getMethodName()};

        TemplateService templateService = new TemplateService(printUtilMock, propertyFileUtilMock, validationUtil, completeUtilMock, connectionUtilMock, inputStreamMock, timerMock);
        final boolean actualResult = templateService.isValidArguments(testArgs);

        Assert.assertFalse(actualResult);
        Mockito.verify(printUtilMock, Mockito.times(1)).printAndLog(Mockito.anyString());
    }

    @Test
    public void testIsValidArgumentsTooManyArgments() throws Exception {
        final String[] testArgs = {Command.TEMPLATE.getMethodName(), "Candidate", "tooMany"};

        TemplateService templateService = new TemplateService(printUtilMock, propertyFileUtilMock, validationUtil, completeUtilMock, connectionUtilMock, inputStreamMock, timerMock);
        final boolean actualResult = templateService.isValidArguments(testArgs);

        Assert.assertFalse(actualResult);
        Mockito.verify(printUtilMock, Mockito.times(1)).printAndLog(Mockito.anyString());
    }

    @Test
    public void testIsValidArgumentsBadEntity() throws Exception {
        final String[] testArgs = {Command.TEMPLATE.getMethodName(), "BadActors"};

        TemplateService templateService = new TemplateService(printUtilMock, propertyFileUtilMock, validationUtil, completeUtilMock, connectionUtilMock, inputStreamMock, timerMock);
        final boolean actualResult = templateService.isValidArguments(testArgs);

        Assert.assertFalse(actualResult);
        Mockito.verify(printUtilMock, Mockito.times(1)).printAndLog(Mockito.anyString());
    }

    @Test
    public void testIsValidArgumentsEmptyEntity() throws Exception {
        final String[] testArgs = {Command.TEMPLATE.getMethodName(), ""};

        TemplateService templateService = new TemplateService(printUtilMock, propertyFileUtilMock, validationUtil, completeUtilMock, connectionUtilMock, inputStreamMock, timerMock);
        final boolean actualResult = templateService.isValidArguments(testArgs);

        Assert.assertFalse(actualResult);
        Mockito.verify(printUtilMock, Mockito.times(1)).printAndLog(Mockito.anyString());
    }
}

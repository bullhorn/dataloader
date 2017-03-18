package com.bullhorn.dataloader.service;

import com.bullhorn.dataloader.enums.Command;
import com.bullhorn.dataloader.util.CompleteUtil;
import com.bullhorn.dataloader.util.PrintUtil;
import com.bullhorn.dataloader.util.PropertyFileUtil;
import com.bullhorn.dataloader.util.Timer;
import com.bullhorn.dataloader.util.validation.ValidationUtil;
import com.bullhornsdk.data.api.BullhornData;
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
import java.util.Arrays;

import static org.mockito.Mockito.when;

public class TemplateServiceTest {

    private PrintUtil printUtilMock;
    private PropertyFileUtil propertyFileUtilMock;
    private ValidationUtil validationUtil;
    private CompleteUtil completeUtilMock;
    private InputStream inputStreamMock;
    private Timer timerMock;
    private BullhornData bullhornDataMock;

    @Before
    public void setup() throws Exception {
        printUtilMock = Mockito.mock(PrintUtil.class);
        propertyFileUtilMock = Mockito.mock(PropertyFileUtil.class);
        validationUtil = new ValidationUtil(printUtilMock);
        completeUtilMock = Mockito.mock(CompleteUtil.class);
        inputStreamMock = Mockito.mock(InputStream.class);
        timerMock = Mockito.mock(Timer.class);
        bullhornDataMock = Mockito.mock(BullhornData.class);

        StandardMetaData<Candidate> metaData = new StandardMetaData<>();
        metaData.setEntity("Candidate");
        Field field = new Field();
        field.setName("comments");
        field.setDataType("String");
        field.setType("SCALAR");
        metaData.setFields(Arrays.asList(field));

        when(bullhornDataMock.getMetaData(Candidate.class, MetaParameter.FULL, null)).thenReturn(metaData);
    }

    // TODO: After Injecting BullhornData, make this work correctly
    @Test
    public void testRun() throws Exception {
        final String entity = "Candidate";
        final String dataType = "String";
        final String[] testArgs = {Command.TEMPLATE.getMethodName(), entity};

        TemplateService templateService = Mockito.spy(new TemplateService(printUtilMock, propertyFileUtilMock, validationUtil, completeUtilMock, inputStreamMock, timerMock));
        String entityName = templateService.validateArguments(testArgs);
        templateService.createTemplate(entityName, bullhornDataMock);

        Mockito.verify(printUtilMock, Mockito.times(2)).printAndLog(Mockito.anyString());
        final String fileName = entity + "Example.csv";
        final File outputFile = new File(fileName);

        Assert.assertTrue(outputFile.isFile());

        final CsvReader csvReader = new CsvReader(fileName);
        csvReader.readHeaders();
        csvReader.readRecord();
        Assert.assertEquals(dataType, csvReader.getValues()[0]);

        outputFile.delete();
    }

    @Test
    public void testIsValidArguments() throws Exception {
        final String entityName = "Candidate";
        final String[] testArgs = {Command.TEMPLATE.getMethodName(), entityName};

        TemplateService templateService = Mockito.spy(new TemplateService(printUtilMock, propertyFileUtilMock, validationUtil, completeUtilMock, inputStreamMock, timerMock));
        final boolean actualResult = templateService.isValidArguments(testArgs);

        Assert.assertTrue(actualResult);
        Mockito.verify(printUtilMock, Mockito.never()).printAndLog(Mockito.anyString());
    }

    @Test
    public void testIsValidArgumentsMissingArgument() throws Exception {
        final String[] testArgs = {Command.TEMPLATE.getMethodName()};

        TemplateService templateService = Mockito.spy(new TemplateService(printUtilMock, propertyFileUtilMock, validationUtil, completeUtilMock, inputStreamMock, timerMock));
        final boolean actualResult = templateService.isValidArguments(testArgs);

        Assert.assertFalse(actualResult);
        Mockito.verify(printUtilMock, Mockito.times(1)).printAndLog(Mockito.anyString());
    }

    @Test
    public void testIsValidArgumentsTooManyArgments() throws Exception {
        final String entityName = "Candidate.csv";
        final String[] testArgs = {Command.TEMPLATE.getMethodName(), entityName, "tooMany"};

        TemplateService templateService = Mockito.spy(new TemplateService(printUtilMock, propertyFileUtilMock, validationUtil, completeUtilMock, inputStreamMock, timerMock));
        final boolean actualResult = templateService.isValidArguments(testArgs);

        Assert.assertFalse(actualResult);
        Mockito.verify(printUtilMock, Mockito.times(1)).printAndLog(Mockito.anyString());
    }

    @Test
    public void testIsValidArgumentsBadEntity() throws Exception {
        final String filePath = "filePath";
        final String[] testArgs = {Command.TEMPLATE.getMethodName(), filePath};

        TemplateService templateService = Mockito.spy(new TemplateService(printUtilMock, propertyFileUtilMock, validationUtil, completeUtilMock, inputStreamMock, timerMock));
        final boolean actualResult = templateService.isValidArguments(testArgs);

        Assert.assertFalse(actualResult);
        Mockito.verify(printUtilMock, Mockito.times(1)).printAndLog(Mockito.anyString());
    }

    @Test
    public void testIsValidArgumentsEmptyFile() throws Exception {
        final String filePath = "";
        final String[] testArgs = {Command.TEMPLATE.getMethodName(), filePath};

        TemplateService templateService = Mockito.spy(new TemplateService(printUtilMock, propertyFileUtilMock, validationUtil, completeUtilMock, inputStreamMock, timerMock));
        final boolean actualResult = templateService.isValidArguments(testArgs);

        Assert.assertFalse(actualResult);
        Mockito.verify(printUtilMock, Mockito.times(1)).printAndLog(Mockito.anyString());
    }
}

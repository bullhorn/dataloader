package com.bullhorn.dataloader.service;

import java.io.File;
import java.text.SimpleDateFormat;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.bullhorn.dataloader.meta.MetaMap;
import com.bullhorn.dataloader.service.api.BullhornAPI;
import com.bullhorn.dataloader.service.csv.CsvFileReader;
import com.bullhorn.dataloader.service.csv.JsonRow;
import com.bullhorn.dataloader.util.PrintUtil;

public class TemplateServiceTest {

	private PrintUtil printUtil;
	private TemplateService templateService;
	private BullhornAPI bullhornAPIMock;

	@Before
	public void setup() throws Exception {
		printUtil = Mockito.mock(PrintUtil.class);
		templateService = Mockito.spy(new TemplateService(printUtil));
		bullhornAPIMock = Mockito.mock(BullhornAPI.class);

		// mock out AbstractService Methods that call class outside of this test scope
		Mockito.doReturn(bullhornAPIMock).when(templateService).createSession();

		Mockito.doThrow(new RuntimeException("should not be called")).when(templateService).createEntityAttachmentConcurrencyService(Mockito.any(), Mockito.anyString(), Mockito.anyString());
		Mockito.doThrow(new RuntimeException("should not be called")).when(templateService).createEntityConcurrencyService(Mockito.any(), Mockito.anyString(), Mockito.anyString());
		Mockito.doThrow(new RuntimeException("should not be called")).when(templateService).getExecutorService(Mockito.any());

		// track this call
		Mockito.doNothing().when(printUtil).printAndLog(Mockito.anyString());
	}

	@Test
	public void testRun() throws Exception {
		final String entity = "Candidate";
        final String fieldName = "fieldName";
        final String dataType = "String";

		final MetaMap metaMap = new MetaMap(new SimpleDateFormat(), ",");
		metaMap.setFieldNameToDataType(fieldName, dataType);
		Mockito.doReturn(metaMap).when(bullhornAPIMock).getRootMetaDataTypes(entity);

		final String[] testArgs = {Command.TEMPLATE.getMethodName(), entity};
		templateService.run(testArgs);

		Mockito.verify(printUtil, Mockito.times(2)).printAndLog(Mockito.anyString());
		final String fileName = entity + "Example.csv";
		final File outputFile = new File(fileName);

		Assert.assertTrue(outputFile.isFile());

		final CsvFileReader csvFileReader = new CsvFileReader(fileName, metaMap);
		final JsonRow actualJson = csvFileReader.next();

		Assert.assertEquals(dataType, actualJson.getValues()[0]);

		outputFile.delete();
	}

	@Test
	public void testIsValidArguments() throws Exception {
		final String entityName = "Candidate";
		final String[] testArgs = {Command.TEMPLATE.getMethodName(), entityName};

		final boolean actualResult = templateService.isValidArguments(testArgs);

		Assert.assertTrue(actualResult);
		Mockito.verify(printUtil, Mockito.never()).printAndLog(Mockito.anyString());
	}

	@Test
	public void testIsValidArgumentsMissingArgument() throws Exception {
		final String[] testArgs = {Command.TEMPLATE.getMethodName()};
		Mockito.doThrow(new RuntimeException("should not be called")).when(templateService).createSession();

		final boolean actualResult = templateService.isValidArguments(testArgs);

		Assert.assertFalse(actualResult);
		Mockito.verify(printUtil, Mockito.times(1)).printAndLog(Mockito.anyString());
	}

	@Test
	public void testIsValidArgumentsTooManyArgments() throws Exception {
		final String entityName = "Candidate.csv";
		final String[] testArgs = {Command.TEMPLATE.getMethodName(), entityName, "tooMany"};
		Mockito.doThrow(new RuntimeException("should not be called")).when(templateService).createSession();

		final boolean actualResult = templateService.isValidArguments(testArgs);

		Assert.assertFalse(actualResult);
		Mockito.verify(printUtil, Mockito.times(1)).printAndLog(Mockito.anyString());
	}

	@Test
	public void testIsValidArgumentsBadEntity() throws Exception {
		final String filePath = "filePath";
		final String[] testArgs = {Command.TEMPLATE.getMethodName(), filePath};
		Mockito.doThrow(new RuntimeException("should not be called")).when(templateService).createSession();

		final boolean actualResult = templateService.isValidArguments(testArgs);

		Assert.assertFalse(actualResult);
		Mockito.verify(printUtil, Mockito.times(1)).printAndLog(Mockito.anyString());
	}

	@Test
	public void testIsValidArgumentsEmptyFile() throws Exception {
		final String filePath = "";
		final String[] testArgs = {Command.TEMPLATE.getMethodName(), filePath};
		Mockito.doThrow(new RuntimeException("should not be called")).when(templateService).createSession();

		final boolean actualResult = templateService.isValidArguments(testArgs);
		
		Assert.assertFalse(actualResult);
		Mockito.verify(printUtil, Mockito.times(1)).printAndLog(Mockito.anyString());
	}
}

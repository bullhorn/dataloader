package com.bullhorn.dataloader.service;

import com.bullhorn.dataloader.util.PrintUtil;
import com.bullhorn.dataloader.util.TemplateUtil;
import com.bullhornsdk.data.api.BullhornData;
import com.csvreader.CsvReader;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.File;

public class TemplateServiceTest {

	private PrintUtil printUtil;
	private TemplateService templateService;
	private BullhornData bullhornDataMock;
	private TemplateUtil templateUtil;

	@Before
	public void setup() throws Exception {
		printUtil = Mockito.mock(PrintUtil.class);
		templateService = Mockito.spy(new TemplateService(printUtil));
		bullhornDataMock = Mockito.mock(BullhornData.class);
		templateUtil = Mockito.mock(TemplateUtil.class);

		Mockito.when(templateService.getTemplateUtil()).thenReturn(templateUtil);

		// track this call
		Mockito.doNothing().when(printUtil).printAndLog(Mockito.anyString());
	}

	@Test
	public void testRun() throws Exception {
		final String entity = "Candidate";
        final String fieldName = "fieldName";
        final String dataType = "String";
		final String[] testArgs = {Command.TEMPLATE.getMethodName(), entity};
		templateService.run(testArgs);

		Mockito.verify(printUtil, Mockito.times(2)).printAndLog(Mockito.anyString());
		final String fileName = entity + "Example.csv";
		final File outputFile = new File(fileName);

		Assert.assertTrue(outputFile.isFile());

		final CsvReader csvReader = new CsvReader(fileName);

		Assert.assertEquals(dataType, csvReader.getValues()[0]);

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

		final boolean actualResult = templateService.isValidArguments(testArgs);

		Assert.assertFalse(actualResult);
		Mockito.verify(printUtil, Mockito.times(1)).printAndLog(Mockito.anyString());
	}

	@Test
	public void testIsValidArgumentsTooManyArgments() throws Exception {
		final String entityName = "Candidate.csv";
		final String[] testArgs = {Command.TEMPLATE.getMethodName(), entityName, "tooMany"};

		final boolean actualResult = templateService.isValidArguments(testArgs);

		Assert.assertFalse(actualResult);
		Mockito.verify(printUtil, Mockito.times(1)).printAndLog(Mockito.anyString());
	}

	@Test
	public void testIsValidArgumentsBadEntity() throws Exception {
		final String filePath = "filePath";
		final String[] testArgs = {Command.TEMPLATE.getMethodName(), filePath};

		final boolean actualResult = templateService.isValidArguments(testArgs);

		Assert.assertFalse(actualResult);
		Mockito.verify(printUtil, Mockito.times(1)).printAndLog(Mockito.anyString());
	}

	@Test
	public void testIsValidArgumentsEmptyFile() throws Exception {
		final String filePath = "";
		final String[] testArgs = {Command.TEMPLATE.getMethodName(), filePath};

		final boolean actualResult = templateService.isValidArguments(testArgs);
		
		Assert.assertFalse(actualResult);
		Mockito.verify(printUtil, Mockito.times(1)).printAndLog(Mockito.anyString());
	}
}

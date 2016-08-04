package com.bullhorn.dataloader.service;

import static org.mockito.Mockito.when;

import java.io.File;
import java.util.Arrays;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.bullhorn.dataloader.util.PrintUtil;
import com.bullhornsdk.data.api.BullhornData;
import com.bullhornsdk.data.model.entity.core.standard.Candidate;
import com.bullhornsdk.data.model.entity.meta.Field;
import com.bullhornsdk.data.model.entity.meta.StandardMetaData;
import com.bullhornsdk.data.model.enums.MetaParameter;
import com.csvreader.CsvReader;

public class TemplateServiceTest {

	private PrintUtil printUtil;
	private TemplateService templateService;
	private BullhornData bullhornData;

	@Before
	public void setup() throws Exception {
		printUtil = Mockito.mock(PrintUtil.class);
		templateService = Mockito.spy(new TemplateService(printUtil, getFilePath("dataloader.properties")));
		bullhornData = Mockito.mock(BullhornData.class);

		StandardMetaData<Candidate> metaData = new StandardMetaData<>();
		metaData.setEntity("Candidate");
		Field field = new Field();
		field.setName("fieldName");
		field.setDataType("String");
		metaData.setFields(Arrays.asList(field));
		when(bullhornData.getMetaData(Candidate.class, MetaParameter.FULL, null)).thenReturn(metaData);

		// track this call
		Mockito.doNothing().when(printUtil).printAndLog(Mockito.anyString());
	}

	@Test
	public void testRun() throws Exception {
		final String entity = "Candidate";
        final String fieldName = "fieldName";
        final String dataType = "String";
		final String[] testArgs = {Command.TEMPLATE.getMethodName(), entity};

		String entityName = templateService.validateArguments(testArgs);
		templateService.createTemplate(entityName, bullhornData);

		Mockito.verify(printUtil, Mockito.times(2)).printAndLog(Mockito.anyString());
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

	private String getFilePath(String filename) {
		final ClassLoader classLoader = getClass().getClassLoader();
		return new File(classLoader.getResource(filename).getFile()).getAbsolutePath();
	}
}

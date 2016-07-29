package com.bullhorn.dataloader.service;

import java.io.File;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import com.bullhorn.dataloader.meta.MetaMap;
import com.bullhorn.dataloader.service.api.BullhornAPI;
import com.bullhorn.dataloader.service.csv.CsvFileReader;
import com.bullhorn.dataloader.service.csv.JsonRow;
import com.bullhorn.dataloader.util.PrintUtil;

public class TemplateServiceTest {

	@Test
	public void testRun() throws Exception {
		final String entity = "Candidate";
        final String fieldName = "fieldName";
        final String dataType = "String";
		final BullhornAPI bullhornAPIMock = Mockito.mock(BullhornAPI.class);

		final MetaMap metaMap = new MetaMap(new SimpleDateFormat(), ",");
		metaMap.setFieldNameToDataType(fieldName, dataType);
				
		Mockito.doReturn(metaMap).when(bullhornAPIMock).getRootMetaDataTypes(entity);

		final TemplateService loadService = Mockito.spy(new TemplateService());

		Mockito.doThrow(new RuntimeException("should not be called")).when(loadService).createEntityAttachmentConcurrencyService(Mockito.any(), Mockito.anyString(), Mockito.anyString());
		Mockito.doThrow(new RuntimeException("should not be called")).when(loadService).createEntityConcurrencyService(Mockito.any(), Mockito.anyString(), Mockito.anyString());
		Mockito.doThrow(new RuntimeException("should not be called")).when(loadService).createSession();
		Mockito.doThrow(new RuntimeException("should not be called")).when(loadService).getExecutorService(Mockito.any());
		
		// mock out AbstractService Methods that call class outside of this test scope
		Mockito.doReturn(bullhornAPIMock).when(loadService).createSession();
		
		// track this call
		Mockito.doNothing().when(loadService).printAndLog(Mockito.anyString());

		final String[] testArgs = {Command.TEMPLATE.getMethodName(), entity};
		loadService.run(testArgs);

		Mockito.verify(loadService, Mockito.times(2)).printAndLog(Mockito.anyString());
		final String fileName = entity + "Example.csv";
		final File outputFile = new File(fileName);

		Assert.assertTrue(outputFile.isFile());

		final CsvFileReader csvFileReader = new CsvFileReader(fileName, metaMap);
		final JsonRow actualJson = csvFileReader.next();
		
		Assert.assertEquals(dataType, actualJson.getValues()[0]);
		
		outputFile.delete();
		
	}
	
	@Test
	public void testIsValidArgments() throws Exception {
		final String filePath = "Candidate.csv";
		final LoadService loadService = Mockito.spy(new LoadService());

		Mockito.doThrow(new RuntimeException("should not be called")).when(loadService).createEntityAttachmentConcurrencyService(Mockito.any(), Mockito.anyString(), Mockito.anyString());
		Mockito.doThrow(new RuntimeException("should not be called")).when(loadService).createEntityConcurrencyService(Mockito.any(), Mockito.anyString(), Mockito.anyString());
		Mockito.doThrow(new RuntimeException("should not be called")).when(loadService).createSession();
		Mockito.doThrow(new RuntimeException("should not be called")).when(loadService).getExecutorService(Mockito.any());
		
		// track this call
		Mockito.doNothing().when(loadService).printAndLog(Mockito.anyString());

		final String[] testArgs = {Command.LOAD.getMethodName(), filePath};
		final boolean actualResult = loadService.isValidArguments(testArgs);
		
		Assert.assertTrue(actualResult);
		
		Mockito.verify(loadService, Mockito.never()).printAndLog(Mockito.anyString());
		
	}
	
	@Test
	public void testIsValidArgumentsMissingArgment() throws Exception {
		final LoadService loadService = Mockito.spy(new LoadService());

		Mockito.doThrow(new RuntimeException("should not be called")).when(loadService).createEntityAttachmentConcurrencyService(Mockito.any(), Mockito.anyString(), Mockito.anyString());
		Mockito.doThrow(new RuntimeException("should not be called")).when(loadService).createEntityConcurrencyService(Mockito.any(), Mockito.anyString(), Mockito.anyString());
		Mockito.doThrow(new RuntimeException("should not be called")).when(loadService).createSession();
		Mockito.doThrow(new RuntimeException("should not be called")).when(loadService).getExecutorService(Mockito.any());
		
		// track this call
		Mockito.doNothing().when(loadService).printAndLog(Mockito.anyString());

		final String[] testArgs = {Command.LOAD.getMethodName()};
		final boolean actualResult = loadService.isValidArguments(testArgs);
		
		Assert.assertFalse(actualResult);
		
		Mockito.verify(loadService, Mockito.times(1)).printAndLog(Mockito.anyString());
		
	}
	
	@Test
	public void testIsValidArgumentsTooManyArgments() throws Exception {
		final String filePath = "Candidate.csv";
		final LoadService loadService = Mockito.spy(new LoadService());

		Mockito.doThrow(new RuntimeException("should not be called")).when(loadService).createEntityAttachmentConcurrencyService(Mockito.any(), Mockito.anyString(), Mockito.anyString());
		Mockito.doThrow(new RuntimeException("should not be called")).when(loadService).createEntityConcurrencyService(Mockito.any(), Mockito.anyString(), Mockito.anyString());
		Mockito.doThrow(new RuntimeException("should not be called")).when(loadService).createSession();
		Mockito.doThrow(new RuntimeException("should not be called")).when(loadService).getExecutorService(Mockito.any());
		
		// track this call
		Mockito.doNothing().when(loadService).printAndLog(Mockito.anyString());

		final String[] testArgs = {Command.LOAD.getMethodName(), filePath, "tooMany"};
		final boolean actualResult = loadService.isValidArguments(testArgs);
		
		Assert.assertFalse(actualResult);
		
		Mockito.verify(loadService, Mockito.times(1)).printAndLog(Mockito.anyString());
		
	}
	
	@Test
	public void testIsValidArgumentsBadEntity() throws Exception {
		final String filePath = "filePath";
		final LoadService loadService = Mockito.spy(new LoadService());

		Mockito.doThrow(new RuntimeException("should not be called")).when(loadService).createEntityAttachmentConcurrencyService(Mockito.any(), Mockito.anyString(), Mockito.anyString());
		Mockito.doThrow(new RuntimeException("should not be called")).when(loadService).createEntityConcurrencyService(Mockito.any(), Mockito.anyString(), Mockito.anyString());
		Mockito.doThrow(new RuntimeException("should not be called")).when(loadService).createSession();
		Mockito.doThrow(new RuntimeException("should not be called")).when(loadService).getExecutorService(Mockito.any());
		
		// track this call
		Mockito.doNothing().when(loadService).printAndLog(Mockito.anyString());

		final String[] testArgs = {Command.LOAD.getMethodName(), filePath};
		final boolean actualResult = loadService.isValidArguments(testArgs);
		
		Assert.assertFalse(actualResult);
		
		Mockito.verify(loadService, Mockito.times(1)).printAndLog(Mockito.anyString());
		
	}
	
	@Test
	public void testIsValidArgumentsEmptyFile() throws Exception {
		final String filePath = "";
		final LoadService loadService = Mockito.spy(new LoadService());

		Mockito.doThrow(new RuntimeException("should not be called")).when(loadService).createEntityAttachmentConcurrencyService(Mockito.any(), Mockito.anyString(), Mockito.anyString());
		Mockito.doThrow(new RuntimeException("should not be called")).when(loadService).createEntityConcurrencyService(Mockito.any(), Mockito.anyString(), Mockito.anyString());
		Mockito.doThrow(new RuntimeException("should not be called")).when(loadService).createSession();
		Mockito.doThrow(new RuntimeException("should not be called")).when(loadService).getExecutorService(Mockito.any());
		
		// track this call
		Mockito.doNothing().when(loadService).printAndLog(Mockito.anyString());

		final String[] testArgs = {Command.LOAD.getMethodName(), filePath};
		final boolean actualResult = loadService.isValidArguments(testArgs);
		
		Assert.assertFalse(actualResult);
		
		Mockito.verify(loadService, Mockito.times(1)).printAndLog(Mockito.anyString());
		
	}
	
	@Test
	public void testUsage() throws Exception {
		final PrintUtil printUtilMock = Mockito.mock(PrintUtil.class);
		
		Mockito.doNothing().when(printUtilMock).printUsage();

		final LoadService loadService = Mockito.spy(new LoadService());

		final Field prinUtilField = AbstractService.class.getDeclaredField("printUtil");
		prinUtilField.setAccessible(true);
		prinUtilField.set(loadService, printUtilMock);

		Mockito.doThrow(new RuntimeException("should not be called")).when(loadService).createEntityAttachmentConcurrencyService(Mockito.any(), Mockito.anyString(), Mockito.anyString());
		Mockito.doThrow(new RuntimeException("should not be called")).when(loadService).createEntityConcurrencyService(Mockito.any(), Mockito.anyString(), Mockito.anyString());
		Mockito.doThrow(new RuntimeException("should not be called")).when(loadService).createSession();
		Mockito.doThrow(new RuntimeException("should not be called")).when(loadService).getExecutorService(Mockito.any());
		
		// track this call
		Mockito.doNothing().when(loadService).printAndLog(Mockito.anyString());

		loadService.printUsage();
		
		Mockito.verify(printUtilMock, Mockito.times(1)).printUsage();
		
	}

}

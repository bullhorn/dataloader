package com.bullhorn.dataloader.service;

import java.lang.reflect.Field;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import com.bullhorn.dataloader.service.executor.EntityConcurrencyService;
import com.bullhorn.dataloader.util.PrintUtil;

public class LoadServiceTest {

	@Test
	public void testRun() throws Exception {
		String entity = "Candidate";
		String filePath = "Candidate.csv";
		
		//arrange
		EntityConcurrencyService entityConcurrencyServiceMock = Mockito.mock(EntityConcurrencyService.class);
		
		Mockito.doNothing().when(entityConcurrencyServiceMock).runLoadProcess();
		
		LoadService loadService = Mockito.spy(new LoadService());
		
		// mock out AbstractService Methods that should not be called
		Mockito.doThrow(new RuntimeException("should not be called")).when(loadService).createEntityAttachmentConcurrencyService(Mockito.any(), Mockito.anyString(), Mockito.anyString());
		Mockito.doThrow(new RuntimeException("should not be called")).when(loadService).createSession();
		Mockito.doThrow(new RuntimeException("should not be called")).when(loadService).getExecutorService(Mockito.any());
		
		// mock out AbstractService Methods that call class outside of this test scope
		Mockito.doReturn(entityConcurrencyServiceMock).when(loadService).createEntityConcurrencyService(Command.LOAD, entity, filePath);
		
		// track this call
		Mockito.doNothing().when(loadService).printAndLog(Mockito.anyString());
		
		//act
		String[] testArgs = {Command.LOAD.getMethodName(), filePath};
		loadService.run(testArgs);
		
		//assert
		Mockito.verify(entityConcurrencyServiceMock, Mockito.times(1)).runLoadProcess();
		Mockito.verify(loadService, Mockito.times(2)).printAndLog(Mockito.anyString());
		
	}
	
	@Test
	public void testIsValidArgments() throws Exception {
		String filePath = "Candidate.csv";

		//arrange
		LoadService loadService = Mockito.spy(new LoadService());
		
		// mock out AbstractService Methods that should not be called
		Mockito.doThrow(new RuntimeException("should not be called")).when(loadService).createEntityAttachmentConcurrencyService(Mockito.any(), Mockito.anyString(), Mockito.anyString());
		Mockito.doThrow(new RuntimeException("should not be called")).when(loadService).createEntityConcurrencyService(Mockito.any(), Mockito.anyString(), Mockito.anyString());
		Mockito.doThrow(new RuntimeException("should not be called")).when(loadService).createSession();
		Mockito.doThrow(new RuntimeException("should not be called")).when(loadService).getExecutorService(Mockito.any());
		
		// track this call
		Mockito.doNothing().when(loadService).printAndLog(Mockito.anyString());
		
		//act
		String[] testArgs = {Command.LOAD.getMethodName(), filePath};
		boolean actualResult = loadService.isValidArguments(testArgs);
		
		Assert.assertTrue(actualResult);
		
		Mockito.verify(loadService, Mockito.never()).printAndLog(Mockito.anyString());
		
	}
	
	@Test
	public void testIsValidArgumentsMissingArgment() throws Exception {
		//arrange
		LoadService loadService = Mockito.spy(new LoadService());
		
		// mock out AbstractService Methods that should not be called
		Mockito.doThrow(new RuntimeException("should not be called")).when(loadService).createEntityAttachmentConcurrencyService(Mockito.any(), Mockito.anyString(), Mockito.anyString());
		Mockito.doThrow(new RuntimeException("should not be called")).when(loadService).createEntityConcurrencyService(Mockito.any(), Mockito.anyString(), Mockito.anyString());
		Mockito.doThrow(new RuntimeException("should not be called")).when(loadService).createSession();
		Mockito.doThrow(new RuntimeException("should not be called")).when(loadService).getExecutorService(Mockito.any());
		
		// track this call
		Mockito.doNothing().when(loadService).printAndLog(Mockito.anyString());
		
		//act
		String[] testArgs = {Command.LOAD.getMethodName()};
		boolean actualResult = loadService.isValidArguments(testArgs);
		
		Assert.assertFalse(actualResult);
		
		Mockito.verify(loadService, Mockito.times(1)).printAndLog(Mockito.anyString());
		
	}
	
	@Test
	public void testIsValidArgumentsTooManyArgments() throws Exception {
		String filePath = "Candidate.csv";

		//arrange
		LoadService loadService = Mockito.spy(new LoadService());
		
		// mock out AbstractService Methods that should not be called
		Mockito.doThrow(new RuntimeException("should not be called")).when(loadService).createEntityAttachmentConcurrencyService(Mockito.any(), Mockito.anyString(), Mockito.anyString());
		Mockito.doThrow(new RuntimeException("should not be called")).when(loadService).createEntityConcurrencyService(Mockito.any(), Mockito.anyString(), Mockito.anyString());
		Mockito.doThrow(new RuntimeException("should not be called")).when(loadService).createSession();
		Mockito.doThrow(new RuntimeException("should not be called")).when(loadService).getExecutorService(Mockito.any());
		
		// track this call
		Mockito.doNothing().when(loadService).printAndLog(Mockito.anyString());
		
		//act
		String[] testArgs = {Command.LOAD.getMethodName(), filePath, "tooMany"};
		boolean actualResult = loadService.isValidArguments(testArgs);
		
		Assert.assertFalse(actualResult);
		
		Mockito.verify(loadService, Mockito.times(1)).printAndLog(Mockito.anyString());
		
	}
	
	@Test
	public void testIsValidArgumentsBadEntity() throws Exception {
		String filePath = "filePath";

		//arrange
		LoadService loadService = Mockito.spy(new LoadService());
		
		// mock out AbstractService Methods that should not be called
		Mockito.doThrow(new RuntimeException("should not be called")).when(loadService).createEntityAttachmentConcurrencyService(Mockito.any(), Mockito.anyString(), Mockito.anyString());
		Mockito.doThrow(new RuntimeException("should not be called")).when(loadService).createEntityConcurrencyService(Mockito.any(), Mockito.anyString(), Mockito.anyString());
		Mockito.doThrow(new RuntimeException("should not be called")).when(loadService).createSession();
		Mockito.doThrow(new RuntimeException("should not be called")).when(loadService).getExecutorService(Mockito.any());
		
		// track this call
		Mockito.doNothing().when(loadService).printAndLog(Mockito.anyString());
		
		//act
		String[] testArgs = {Command.LOAD.getMethodName(), filePath};
		boolean actualResult = loadService.isValidArguments(testArgs);
		
		Assert.assertFalse(actualResult);
		
		Mockito.verify(loadService, Mockito.times(1)).printAndLog(Mockito.anyString());
		
	}
	
	@Test
	public void testIsValidArgumentsEmptyFile() throws Exception {
		String filePath = "";

		//arrange
		LoadService loadService = Mockito.spy(new LoadService());
		
		// mock out AbstractService Methods that should not be called
		Mockito.doThrow(new RuntimeException("should not be called")).when(loadService).createEntityAttachmentConcurrencyService(Mockito.any(), Mockito.anyString(), Mockito.anyString());
		Mockito.doThrow(new RuntimeException("should not be called")).when(loadService).createEntityConcurrencyService(Mockito.any(), Mockito.anyString(), Mockito.anyString());
		Mockito.doThrow(new RuntimeException("should not be called")).when(loadService).createSession();
		Mockito.doThrow(new RuntimeException("should not be called")).when(loadService).getExecutorService(Mockito.any());
		
		// track this call
		Mockito.doNothing().when(loadService).printAndLog(Mockito.anyString());
		
		//act
		String[] testArgs = {Command.LOAD.getMethodName(), filePath};
		boolean actualResult = loadService.isValidArguments(testArgs);
		
		Assert.assertFalse(actualResult);
		
		Mockito.verify(loadService, Mockito.times(1)).printAndLog(Mockito.anyString());
		
	}
	
	@Test
	public void testUsage() throws Exception {
		//arrange
		PrintUtil printUtilMock = Mockito.mock(PrintUtil.class);
		
		Mockito.doNothing().when(printUtilMock).printUsage();
		
		LoadService loadService = Mockito.spy(new LoadService());
		
		Field prinUtilField = AbstractService.class.getDeclaredField("printUtil");
		prinUtilField.setAccessible(true);
		prinUtilField.set(loadService, printUtilMock);
		
		// mock out AbstractService Methods that should not be called
		Mockito.doThrow(new RuntimeException("should not be called")).when(loadService).createEntityAttachmentConcurrencyService(Mockito.any(), Mockito.anyString(), Mockito.anyString());
		Mockito.doThrow(new RuntimeException("should not be called")).when(loadService).createEntityConcurrencyService(Mockito.any(), Mockito.anyString(), Mockito.anyString());
		Mockito.doThrow(new RuntimeException("should not be called")).when(loadService).createSession();
		Mockito.doThrow(new RuntimeException("should not be called")).when(loadService).getExecutorService(Mockito.any());
		
		// track this call
		Mockito.doNothing().when(loadService).printAndLog(Mockito.anyString());
		
		//act
		loadService.printUsage();
		
		Mockito.verify(printUtilMock, Mockito.times(1)).printUsage();
		
	}
	
}

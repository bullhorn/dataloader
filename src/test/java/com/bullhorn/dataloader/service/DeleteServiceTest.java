package com.bullhorn.dataloader.service;

import java.lang.reflect.Field;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import com.bullhorn.dataloader.service.executor.EntityConcurrencyService;
import com.bullhorn.dataloader.util.PrintUtil;

public class DeleteServiceTest {

	@Test
	public void testRun() throws Exception {
		String entity = "Candidate";
		String filePath = "filePath";
		
		//arrange
		EntityConcurrencyService entityConcurrencyServiceMock = Mockito.mock(EntityConcurrencyService.class);
		
		Mockito.doNothing().when(entityConcurrencyServiceMock).runDeleteProcess();
		
		DeleteService deleteService = Mockito.spy(new DeleteService());
		
		// mock out AbstractService Methods that should not be called
		Mockito.doThrow(new RuntimeException("should not be called")).when(deleteService).createEntityAttachmentConcurrencyService(Mockito.any(), Mockito.anyString(), Mockito.anyString());
		Mockito.doThrow(new RuntimeException("should not be called")).when(deleteService).createSession();
		Mockito.doThrow(new RuntimeException("should not be called")).when(deleteService).getExecutorService(Mockito.any());
		
		// mock out AbstractService Methods that call class outside of this test scope
		Mockito.doReturn(entityConcurrencyServiceMock).when(deleteService).createEntityConcurrencyService(Command.DELETE, entity, filePath);
		
		// track this call
		Mockito.doNothing().when(deleteService).printAndLog(Mockito.anyString());
		
		//act
		String[] testArgs = {Command.DELETE.getMethodName(), entity, filePath};
		deleteService.run(testArgs);
		
		//assert
		Mockito.verify(entityConcurrencyServiceMock, Mockito.times(1)).runDeleteProcess();
		Mockito.verify(deleteService, Mockito.times(2)).printAndLog(Mockito.anyString());
		
	}
	
	@Test
	public void testIsValidArgments() throws Exception {
		String entity = "Candidate";
		String filePath = "filePath";

		//arrange
		DeleteService deleteService = Mockito.spy(new DeleteService());
		
		// mock out AbstractService Methods that should not be called
		Mockito.doThrow(new RuntimeException("should not be called")).when(deleteService).createEntityAttachmentConcurrencyService(Mockito.any(), Mockito.anyString(), Mockito.anyString());
		Mockito.doThrow(new RuntimeException("should not be called")).when(deleteService).createEntityConcurrencyService(Mockito.any(), Mockito.anyString(), Mockito.anyString());
		Mockito.doThrow(new RuntimeException("should not be called")).when(deleteService).createSession();
		Mockito.doThrow(new RuntimeException("should not be called")).when(deleteService).getExecutorService(Mockito.any());
		
		// track this call
		Mockito.doNothing().when(deleteService).printAndLog(Mockito.anyString());
		
		//act
		String[] testArgs = {Command.DELETE.getMethodName(), entity, filePath};
		boolean actualResult = deleteService.isValidArguments(testArgs);
		
		Assert.assertTrue(actualResult);
		
		Mockito.verify(deleteService, Mockito.never()).printAndLog(Mockito.anyString());
		
	}
	
	@Test
	public void testIsValidArgumentsMissingArgment() throws Exception {
		//arrange
		DeleteService deleteService = Mockito.spy(new DeleteService());
		
		// mock out AbstractService Methods that should not be called
		Mockito.doThrow(new RuntimeException("should not be called")).when(deleteService).createEntityAttachmentConcurrencyService(Mockito.any(), Mockito.anyString(), Mockito.anyString());
		Mockito.doThrow(new RuntimeException("should not be called")).when(deleteService).createEntityConcurrencyService(Mockito.any(), Mockito.anyString(), Mockito.anyString());
		Mockito.doThrow(new RuntimeException("should not be called")).when(deleteService).createSession();
		Mockito.doThrow(new RuntimeException("should not be called")).when(deleteService).getExecutorService(Mockito.any());
		
		// track this call
		Mockito.doNothing().when(deleteService).printAndLog(Mockito.anyString());
		
		//act
		String[] testArgs = {Command.DELETE.getMethodName()};
		boolean actualResult = deleteService.isValidArguments(testArgs);
		
		Assert.assertFalse(actualResult);
		
		Mockito.verify(deleteService, Mockito.times(1)).printAndLog(Mockito.anyString());
		
	}
	
	@Test
	public void testIsValidArgumentsTooManyArgments() throws Exception {
		String entity = "Candidate";
		String filePath = "filePath";

		//arrange
		DeleteService deleteService = Mockito.spy(new DeleteService());
		
		// mock out AbstractService Methods that should not be called
		Mockito.doThrow(new RuntimeException("should not be called")).when(deleteService).createEntityAttachmentConcurrencyService(Mockito.any(), Mockito.anyString(), Mockito.anyString());
		Mockito.doThrow(new RuntimeException("should not be called")).when(deleteService).createEntityConcurrencyService(Mockito.any(), Mockito.anyString(), Mockito.anyString());
		Mockito.doThrow(new RuntimeException("should not be called")).when(deleteService).createSession();
		Mockito.doThrow(new RuntimeException("should not be called")).when(deleteService).getExecutorService(Mockito.any());
		
		// track this call
		Mockito.doNothing().when(deleteService).printAndLog(Mockito.anyString());
		
		//act
		String[] testArgs = {Command.DELETE.getMethodName(), entity, filePath, "tooMany"};
		boolean actualResult = deleteService.isValidArguments(testArgs);
		
		Assert.assertFalse(actualResult);
		
		Mockito.verify(deleteService, Mockito.times(1)).printAndLog(Mockito.anyString());
		
	}
	
	@Test
	public void testIsValidArgumentsBadEntity() throws Exception {
		String entity = "Candidates";
		String filePath = "filePath";

		//arrange
		DeleteService deleteService = Mockito.spy(new DeleteService());
		
		// mock out AbstractService Methods that should not be called
		Mockito.doThrow(new RuntimeException("should not be called")).when(deleteService).createEntityAttachmentConcurrencyService(Mockito.any(), Mockito.anyString(), Mockito.anyString());
		Mockito.doThrow(new RuntimeException("should not be called")).when(deleteService).createEntityConcurrencyService(Mockito.any(), Mockito.anyString(), Mockito.anyString());
		Mockito.doThrow(new RuntimeException("should not be called")).when(deleteService).createSession();
		Mockito.doThrow(new RuntimeException("should not be called")).when(deleteService).getExecutorService(Mockito.any());
		
		// track this call
		Mockito.doNothing().when(deleteService).printAndLog(Mockito.anyString());
		
		//act
		String[] testArgs = {Command.DELETE.getMethodName(), entity, filePath};
		boolean actualResult = deleteService.isValidArguments(testArgs);
		
		Assert.assertFalse(actualResult);
		
		Mockito.verify(deleteService, Mockito.times(1)).printAndLog(Mockito.anyString());
		
	}
	
	@Test
	public void testIsValidArgumentsEmptyFile() throws Exception {
		String entity = "Candidates";
		String filePath = "";

		//arrange
		DeleteService deleteService = Mockito.spy(new DeleteService());
		
		// mock out AbstractService Methods that should not be called
		Mockito.doThrow(new RuntimeException("should not be called")).when(deleteService).createEntityAttachmentConcurrencyService(Mockito.any(), Mockito.anyString(), Mockito.anyString());
		Mockito.doThrow(new RuntimeException("should not be called")).when(deleteService).createEntityConcurrencyService(Mockito.any(), Mockito.anyString(), Mockito.anyString());
		Mockito.doThrow(new RuntimeException("should not be called")).when(deleteService).createSession();
		Mockito.doThrow(new RuntimeException("should not be called")).when(deleteService).getExecutorService(Mockito.any());
		
		// track this call
		Mockito.doNothing().when(deleteService).printAndLog(Mockito.anyString());
		
		//act
		String[] testArgs = {Command.DELETE.getMethodName(), entity, filePath};
		boolean actualResult = deleteService.isValidArguments(testArgs);
		
		Assert.assertFalse(actualResult);
		
		Mockito.verify(deleteService, Mockito.times(1)).printAndLog(Mockito.anyString());
		
	}
	
	@Test
	public void testUsage() throws Exception {
		//arrange
		PrintUtil printUtilMock = Mockito.mock(PrintUtil.class);
		
		Mockito.doNothing().when(printUtilMock).printUsage();
		
		DeleteService deleteService = Mockito.spy(new DeleteService());
		
		Field prinUtilField = AbstractService.class.getDeclaredField("printUtil");
		prinUtilField.setAccessible(true);
		prinUtilField.set(deleteService, printUtilMock);
		
		// mock out AbstractService Methods that should not be called
		Mockito.doThrow(new RuntimeException("should not be called")).when(deleteService).createEntityAttachmentConcurrencyService(Mockito.any(), Mockito.anyString(), Mockito.anyString());
		Mockito.doThrow(new RuntimeException("should not be called")).when(deleteService).createEntityConcurrencyService(Mockito.any(), Mockito.anyString(), Mockito.anyString());
		Mockito.doThrow(new RuntimeException("should not be called")).when(deleteService).createSession();
		Mockito.doThrow(new RuntimeException("should not be called")).when(deleteService).getExecutorService(Mockito.any());
		
		// track this call
		Mockito.doNothing().when(deleteService).printAndLog(Mockito.anyString());
		
		//act
		deleteService.printUsage();
		
		Mockito.verify(printUtilMock, Mockito.times(1)).printUsage();
		
	}
	
}

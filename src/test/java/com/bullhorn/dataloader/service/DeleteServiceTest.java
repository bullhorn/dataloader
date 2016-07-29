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
		final String entity = "Candidate";
		final String filePath = "filePath";
		final EntityConcurrencyService entityConcurrencyServiceMock = Mockito.mock(EntityConcurrencyService.class);
		
		Mockito.doNothing().when(entityConcurrencyServiceMock).runDeleteProcess();

		final DeleteService deleteService = Mockito.spy(new DeleteService());

		Mockito.doThrow(new RuntimeException("should not be called")).when(deleteService).createEntityAttachmentConcurrencyService(Mockito.any(), Mockito.anyString(), Mockito.anyString());
		Mockito.doThrow(new RuntimeException("should not be called")).when(deleteService).createSession();
		Mockito.doThrow(new RuntimeException("should not be called")).when(deleteService).getExecutorService(Mockito.any());
		
		// mock out AbstractService Methods that call class outside of this test scope
		Mockito.doReturn(entityConcurrencyServiceMock).when(deleteService).createEntityConcurrencyService(Command.DELETE, entity, filePath);
		
		// track this call
		Mockito.doNothing().when(deleteService).printAndLog(Mockito.anyString());

		final String[] testArgs = {Command.DELETE.getMethodName(), entity, filePath};
		deleteService.run(testArgs);

		Mockito.verify(entityConcurrencyServiceMock, Mockito.times(1)).runDeleteProcess();
		Mockito.verify(deleteService, Mockito.times(2)).printAndLog(Mockito.anyString());
		
	}
	
	@Test
	public void testIsValidArgments() throws Exception {
		final String entity = "Candidate";
		final String filePath = "filePath";
		final DeleteService deleteService = Mockito.spy(new DeleteService());

		Mockito.doThrow(new RuntimeException("should not be called")).when(deleteService).createEntityAttachmentConcurrencyService(Mockito.any(), Mockito.anyString(), Mockito.anyString());
		Mockito.doThrow(new RuntimeException("should not be called")).when(deleteService).createEntityConcurrencyService(Mockito.any(), Mockito.anyString(), Mockito.anyString());
		Mockito.doThrow(new RuntimeException("should not be called")).when(deleteService).createSession();
		Mockito.doThrow(new RuntimeException("should not be called")).when(deleteService).getExecutorService(Mockito.any());
		
		// track this call
		Mockito.doNothing().when(deleteService).printAndLog(Mockito.anyString());

		final String[] testArgs = {Command.DELETE.getMethodName(), entity, filePath};
		final boolean actualResult = deleteService.isValidArguments(testArgs);
		
		Assert.assertTrue(actualResult);
		
		Mockito.verify(deleteService, Mockito.never()).printAndLog(Mockito.anyString());
		
	}
	
	@Test
	public void testIsValidArgumentsMissingArgment() throws Exception {
		final DeleteService deleteService = Mockito.spy(new DeleteService());

		Mockito.doThrow(new RuntimeException("should not be called")).when(deleteService).createEntityAttachmentConcurrencyService(Mockito.any(), Mockito.anyString(), Mockito.anyString());
		Mockito.doThrow(new RuntimeException("should not be called")).when(deleteService).createEntityConcurrencyService(Mockito.any(), Mockito.anyString(), Mockito.anyString());
		Mockito.doThrow(new RuntimeException("should not be called")).when(deleteService).createSession();
		Mockito.doThrow(new RuntimeException("should not be called")).when(deleteService).getExecutorService(Mockito.any());
		
		// track this call
		Mockito.doNothing().when(deleteService).printAndLog(Mockito.anyString());

		final String[] testArgs = {Command.DELETE.getMethodName()};
		final boolean actualResult = deleteService.isValidArguments(testArgs);
		
		Assert.assertFalse(actualResult);
		
		Mockito.verify(deleteService, Mockito.times(1)).printAndLog(Mockito.anyString());
		
	}
	
	@Test
	public void testIsValidArgumentsTooManyArgments() throws Exception {
		final String entity = "Candidate";
		final String filePath = "filePath";
		final DeleteService deleteService = Mockito.spy(new DeleteService());

		Mockito.doThrow(new RuntimeException("should not be called")).when(deleteService).createEntityAttachmentConcurrencyService(Mockito.any(), Mockito.anyString(), Mockito.anyString());
		Mockito.doThrow(new RuntimeException("should not be called")).when(deleteService).createEntityConcurrencyService(Mockito.any(), Mockito.anyString(), Mockito.anyString());
		Mockito.doThrow(new RuntimeException("should not be called")).when(deleteService).createSession();
		Mockito.doThrow(new RuntimeException("should not be called")).when(deleteService).getExecutorService(Mockito.any());
		
		// track this call
		Mockito.doNothing().when(deleteService).printAndLog(Mockito.anyString());

		final String[] testArgs = {Command.DELETE.getMethodName(), entity, filePath, "tooMany"};
		boolean actualResult = deleteService.isValidArguments(testArgs);
		
		Assert.assertFalse(actualResult);
		
		Mockito.verify(deleteService, Mockito.times(1)).printAndLog(Mockito.anyString());
		
	}
	
	@Test
	public void testIsValidArgumentsBadEntity() throws Exception {
		final String entity = "Candidates";
		final String filePath = "filePath";
		final DeleteService deleteService = Mockito.spy(new DeleteService());

		Mockito.doThrow(new RuntimeException("should not be called")).when(deleteService).createEntityAttachmentConcurrencyService(Mockito.any(), Mockito.anyString(), Mockito.anyString());
		Mockito.doThrow(new RuntimeException("should not be called")).when(deleteService).createEntityConcurrencyService(Mockito.any(), Mockito.anyString(), Mockito.anyString());
		Mockito.doThrow(new RuntimeException("should not be called")).when(deleteService).createSession();
		Mockito.doThrow(new RuntimeException("should not be called")).when(deleteService).getExecutorService(Mockito.any());
		
		// track this call
		Mockito.doNothing().when(deleteService).printAndLog(Mockito.anyString());

		final String[] testArgs = {Command.DELETE.getMethodName(), entity, filePath};
		boolean actualResult = deleteService.isValidArguments(testArgs);
		
		Assert.assertFalse(actualResult);
		
		Mockito.verify(deleteService, Mockito.times(1)).printAndLog(Mockito.anyString());
		
	}
	
	@Test
	public void testIsValidArgumentsEmptyFile() throws Exception {
		final String entity = "Candidates";
		final String filePath = "";
		final DeleteService deleteService = Mockito.spy(new DeleteService());

		Mockito.doThrow(new RuntimeException("should not be called")).when(deleteService).createEntityAttachmentConcurrencyService(Mockito.any(), Mockito.anyString(), Mockito.anyString());
		Mockito.doThrow(new RuntimeException("should not be called")).when(deleteService).createEntityConcurrencyService(Mockito.any(), Mockito.anyString(), Mockito.anyString());
		Mockito.doThrow(new RuntimeException("should not be called")).when(deleteService).createSession();
		Mockito.doThrow(new RuntimeException("should not be called")).when(deleteService).getExecutorService(Mockito.any());
		
		// track this call
		Mockito.doNothing().when(deleteService).printAndLog(Mockito.anyString());

		final String[] testArgs = {Command.DELETE.getMethodName(), entity, filePath};
		final boolean actualResult = deleteService.isValidArguments(testArgs);
		
		Assert.assertFalse(actualResult);
		
		Mockito.verify(deleteService, Mockito.times(1)).printAndLog(Mockito.anyString());
		
	}
	
	@Test
	public void testUsage() throws Exception {
		final PrintUtil printUtilMock = Mockito.mock(PrintUtil.class);
		
		Mockito.doNothing().when(printUtilMock).printUsage();

		final DeleteService deleteService = Mockito.spy(new DeleteService());

		final Field prinUtilField = AbstractService.class.getDeclaredField("printUtil");
		prinUtilField.setAccessible(true);
		prinUtilField.set(deleteService, printUtilMock);

		Mockito.doThrow(new RuntimeException("should not be called")).when(deleteService).createEntityAttachmentConcurrencyService(Mockito.any(), Mockito.anyString(), Mockito.anyString());
		Mockito.doThrow(new RuntimeException("should not be called")).when(deleteService).createEntityConcurrencyService(Mockito.any(), Mockito.anyString(), Mockito.anyString());
		Mockito.doThrow(new RuntimeException("should not be called")).when(deleteService).createSession();
		Mockito.doThrow(new RuntimeException("should not be called")).when(deleteService).getExecutorService(Mockito.any());
		
		// track this call
		Mockito.doNothing().when(deleteService).printAndLog(Mockito.anyString());

		deleteService.printUsage();
		
		Mockito.verify(printUtilMock, Mockito.times(1)).printUsage();
		
	}
	
}

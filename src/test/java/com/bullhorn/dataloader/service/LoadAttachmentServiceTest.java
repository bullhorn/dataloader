package com.bullhorn.dataloader.service;

import java.lang.reflect.Field;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import com.bullhorn.dataloader.service.executor.EntityAttachmentConcurrencyService;
import com.bullhorn.dataloader.util.PrintUtil;

public class LoadAttachmentServiceTest {
	@Test
	public void testRun() throws Exception {
		String entity = "Candidate";
		String filePath = "filePath";
		
		//arrange
		EntityAttachmentConcurrencyService entityAttachmentConcurrencyServiceMock = Mockito.mock(EntityAttachmentConcurrencyService.class);
		
		Mockito.doNothing().when(entityAttachmentConcurrencyServiceMock).runLoadAttachmentProcess();
		
		LoadAttachmentsService loadAttachmentService = Mockito.spy(new LoadAttachmentsService());
		
		// mock out AbstractService Methods that should not be called
		Mockito.doThrow(new RuntimeException("should not be called")).when(loadAttachmentService).createEntityConcurrencyService(Mockito.any(), Mockito.anyString(), Mockito.anyString());
		Mockito.doThrow(new RuntimeException("should not be called")).when(loadAttachmentService).createSession();
		Mockito.doThrow(new RuntimeException("should not be called")).when(loadAttachmentService).getExecutorService(Mockito.any());
		
		// mock out AbstractService Methods that call class outside of this test scope createEntityAttachmentConcurrencyService
		Mockito.doReturn(entityAttachmentConcurrencyServiceMock).when(loadAttachmentService).createEntityAttachmentConcurrencyService(Mockito.eq(Command.LOAD_ATTACHMENTS), Mockito.eq(entity), Mockito.eq(filePath));
		
		// track this call
		Mockito.doNothing().when(loadAttachmentService).printAndLog(Mockito.anyString());
		
		//act
		String[] testArgs = {Command.LOAD_ATTACHMENTS.getMethodName(), entity, filePath};
		loadAttachmentService.run(testArgs);
		
		//assert
		Mockito.verify(entityAttachmentConcurrencyServiceMock, Mockito.times(1)).runLoadAttachmentProcess();
		Mockito.verify(loadAttachmentService, Mockito.times(2)).printAndLog(Mockito.anyString());
		
	}
	
	@Test
	public void testIsValidArgments() throws Exception {
		String entity = "Candidate";
		String filePath = "filePath";

		//arrange
		LoadAttachmentsService loadAttachmentService = Mockito.spy(new LoadAttachmentsService());
		
		// mock out AbstractService Methods that should not be called
		Mockito.doThrow(new RuntimeException("should not be called")).when(loadAttachmentService).createEntityAttachmentConcurrencyService(Mockito.any(), Mockito.anyString(), Mockito.anyString());
		Mockito.doThrow(new RuntimeException("should not be called")).when(loadAttachmentService).createEntityConcurrencyService(Mockito.any(), Mockito.anyString(), Mockito.anyString());
		Mockito.doThrow(new RuntimeException("should not be called")).when(loadAttachmentService).createSession();
		Mockito.doThrow(new RuntimeException("should not be called")).when(loadAttachmentService).getExecutorService(Mockito.any());
		
		// track this call
		Mockito.doNothing().when(loadAttachmentService).printAndLog(Mockito.anyString());
		
		//act
		String[] testArgs = {Command.LOAD_ATTACHMENTS.getMethodName(), entity, filePath};
		boolean actualResult = loadAttachmentService.isValidArguments(testArgs);
		
		Assert.assertTrue(actualResult);
		
		Mockito.verify(loadAttachmentService, Mockito.never()).printAndLog(Mockito.anyString());
		
	}
	
	@Test
	public void testIsValidArgumentsMissingArgment() throws Exception {
		//arrange
		LoadAttachmentsService loadAttachmentService = Mockito.spy(new LoadAttachmentsService());
		
		// mock out AbstractService Methods that should not be called
		Mockito.doThrow(new RuntimeException("should not be called")).when(loadAttachmentService).createEntityAttachmentConcurrencyService(Mockito.any(), Mockito.anyString(), Mockito.anyString());
		Mockito.doThrow(new RuntimeException("should not be called")).when(loadAttachmentService).createEntityConcurrencyService(Mockito.any(), Mockito.anyString(), Mockito.anyString());
		Mockito.doThrow(new RuntimeException("should not be called")).when(loadAttachmentService).createSession();
		Mockito.doThrow(new RuntimeException("should not be called")).when(loadAttachmentService).getExecutorService(Mockito.any());
		
		// track this call
		Mockito.doNothing().when(loadAttachmentService).printAndLog(Mockito.anyString());
		
		//act
		String[] testArgs = {Command.LOAD_ATTACHMENTS.getMethodName()};
		boolean actualResult = loadAttachmentService.isValidArguments(testArgs);
		
		Assert.assertFalse(actualResult);
		
		Mockito.verify(loadAttachmentService, Mockito.times(1)).printAndLog(Mockito.anyString());
		
	}
	
	@Test
	public void testIsValidArgumentsTooManyArgments() throws Exception {
		String entity = "Candidate";
		String filePath = "filePath";

		//arrange
		LoadAttachmentsService loadAttachmentService = Mockito.spy(new LoadAttachmentsService());
		
		// mock out AbstractService Methods that should not be called
		Mockito.doThrow(new RuntimeException("should not be called")).when(loadAttachmentService).createEntityAttachmentConcurrencyService(Mockito.any(), Mockito.anyString(), Mockito.anyString());
		Mockito.doThrow(new RuntimeException("should not be called")).when(loadAttachmentService).createEntityConcurrencyService(Mockito.any(), Mockito.anyString(), Mockito.anyString());
		Mockito.doThrow(new RuntimeException("should not be called")).when(loadAttachmentService).createSession();
		Mockito.doThrow(new RuntimeException("should not be called")).when(loadAttachmentService).getExecutorService(Mockito.any());
		
		// track this call
		Mockito.doNothing().when(loadAttachmentService).printAndLog(Mockito.anyString());
		
		//act
		String[] testArgs = {Command.LOAD_ATTACHMENTS.getMethodName(), entity, filePath, "tooMany"};
		boolean actualResult = loadAttachmentService.isValidArguments(testArgs);
		
		Assert.assertFalse(actualResult);
		
		Mockito.verify(loadAttachmentService, Mockito.times(1)).printAndLog(Mockito.anyString());
		
	}
	
	@Test
	public void testIsValidArgumentsBadEntity() throws Exception {
		String entity = "Candidates";
		String filePath = "filePath";

		//arrange
		LoadAttachmentsService loadAttachmentService = Mockito.spy(new LoadAttachmentsService());
		
		// mock out AbstractService Methods that should not be called
		Mockito.doThrow(new RuntimeException("should not be called")).when(loadAttachmentService).createEntityAttachmentConcurrencyService(Mockito.any(), Mockito.anyString(), Mockito.anyString());
		Mockito.doThrow(new RuntimeException("should not be called")).when(loadAttachmentService).createEntityConcurrencyService(Mockito.any(), Mockito.anyString(), Mockito.anyString());
		Mockito.doThrow(new RuntimeException("should not be called")).when(loadAttachmentService).createSession();
		Mockito.doThrow(new RuntimeException("should not be called")).when(loadAttachmentService).getExecutorService(Mockito.any());
		
		// track this call
		Mockito.doNothing().when(loadAttachmentService).printAndLog(Mockito.anyString());
		
		//act
		String[] testArgs = {Command.LOAD_ATTACHMENTS.getMethodName(), entity, filePath};
		boolean actualResult = loadAttachmentService.isValidArguments(testArgs);
		
		Assert.assertFalse(actualResult);
		
		Mockito.verify(loadAttachmentService, Mockito.times(1)).printAndLog(Mockito.anyString());
		
	}
	
	@Test
	public void testIsValidArgumentsEmptyFile() throws Exception {
		String entity = "Candidates";
		String filePath = "";

		//arrange
		LoadAttachmentsService loadAttachmentService = Mockito.spy(new LoadAttachmentsService());
		
		// mock out AbstractService Methods that should not be called
		Mockito.doThrow(new RuntimeException("should not be called")).when(loadAttachmentService).createEntityAttachmentConcurrencyService(Mockito.any(), Mockito.anyString(), Mockito.anyString());
		Mockito.doThrow(new RuntimeException("should not be called")).when(loadAttachmentService).createEntityConcurrencyService(Mockito.any(), Mockito.anyString(), Mockito.anyString());
		Mockito.doThrow(new RuntimeException("should not be called")).when(loadAttachmentService).createSession();
		Mockito.doThrow(new RuntimeException("should not be called")).when(loadAttachmentService).getExecutorService(Mockito.any());
		
		// track this call
		Mockito.doNothing().when(loadAttachmentService).printAndLog(Mockito.anyString());
		
		//act
		String[] testArgs = {Command.LOAD_ATTACHMENTS.getMethodName(), entity, filePath};
		boolean actualResult = loadAttachmentService.isValidArguments(testArgs);
		
		Assert.assertFalse(actualResult);
		
		Mockito.verify(loadAttachmentService, Mockito.times(1)).printAndLog(Mockito.anyString());
		
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

package com.bullhorn.dataloader.service;

import java.io.File;
import java.io.InputStream;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.bullhorn.dataloader.service.executor.ConcurrencyService;
import com.bullhorn.dataloader.util.PrintUtil;
import com.bullhorn.dataloader.util.PropertyFileUtil;
import com.bullhorn.dataloader.util.validation.ValidationUtil;

public class DeleteAttachmentsServiceTest {

	private PrintUtil printUtilMock;
	private PropertyFileUtil propertyFileUtilMock;
	private ValidationUtil validationUtil;
	private InputStream inputStreamMock;
	private DeleteAttachmentsService deleteAttachmentsService;
	private ConcurrencyService concurrencyServiceMock;

	@Before
	public void setup() throws Exception {
		printUtilMock = Mockito.mock(PrintUtil.class);
		propertyFileUtilMock = Mockito.mock(PropertyFileUtil.class);
		validationUtil = new ValidationUtil(printUtilMock);
		inputStreamMock = Mockito.mock(InputStream.class);
		deleteAttachmentsService = Mockito.spy(new DeleteAttachmentsService(printUtilMock, propertyFileUtilMock, validationUtil, inputStreamMock));

		// mock out AbstractService Methods that call class outside of this test scope
		concurrencyServiceMock = Mockito.mock(ConcurrencyService.class);
		Mockito.doReturn(concurrencyServiceMock).when(deleteAttachmentsService).createConcurrencyService(Mockito.any(), Mockito.anyString(), Mockito.anyString());
		Mockito.doNothing().when(concurrencyServiceMock).runDeleteAttachmentsProcess();
	}

	@Test
	public void testRun() throws Exception {
		final String filePath = getFilePath("Candidate_Valid_File.csv");
		final String[] testArgs = {Command.DELETE_ATTACHMENTS.getMethodName(), filePath};

		deleteAttachmentsService.run(testArgs);

		Mockito.verify(concurrencyServiceMock, Mockito.times(1)).runDeleteAttachmentsProcess();
		Mockito.verify(printUtilMock, Mockito.times(2)).printAndLog(Mockito.anyString());
	}

	@Test
	public void testIsValidArguments() throws Exception {
		final String filePath = getFilePath("Candidate_Valid_File.csv");
		final String[] testArgs = {Command.DELETE_ATTACHMENTS.getMethodName(), filePath};

		final boolean actualResult = deleteAttachmentsService.isValidArguments(testArgs);

		Assert.assertTrue(actualResult);
		Mockito.verify(printUtilMock, Mockito.never()).printAndLog(Mockito.anyString());
	}

	@Test
	public void testIsValidArguments_BadEntity() throws Exception {
		final String filePath = getFilePath("Invalid_Candidate_File.csv");
		final String[] testArgs = {Command.DELETE_ATTACHMENTS.getMethodName(), filePath};

		final boolean actualResult = deleteAttachmentsService.isValidArguments(testArgs);

		Assert.assertFalse(actualResult);
		Mockito.verify(printUtilMock, Mockito.times(1)).printAndLog(Mockito.anyString());
	}

	@Test
	public void testIsValidArguments_MissingArgument() throws Exception {
		final String[] testArgs = {Command.DELETE_ATTACHMENTS.getMethodName()};

		final boolean actualResult = deleteAttachmentsService.isValidArguments(testArgs);

		Assert.assertFalse(actualResult);
		Mockito.verify(printUtilMock, Mockito.times(1)).printAndLog(Mockito.anyString());
	}

	@Test
	public void testIsValidArguments_TooManyArgments() throws Exception {
		final String filePath = "Candidate.csv";
		final String[] testArgs = {Command.DELETE_ATTACHMENTS.getMethodName(), filePath, "tooMany"};

		final boolean actualResult = deleteAttachmentsService.isValidArguments(testArgs);

		Assert.assertFalse(actualResult);
		Mockito.verify(printUtilMock, Mockito.times(1)).printAndLog(Mockito.anyString());
	}

	@Test
	public void testIsValidArguments_InvalidFile() throws Exception {
		final String filePath = "filePath";
		final String[] testArgs = {Command.DELETE_ATTACHMENTS.getMethodName(), filePath};

		final boolean actualResult = deleteAttachmentsService.isValidArguments(testArgs);

		Assert.assertFalse(actualResult);
		Mockito.verify(printUtilMock, Mockito.times(2)).printAndLog(Mockito.anyString());
	}

	@Test
	public void testIsValidArguments_EmptyFile() throws Exception {
		final String filePath = "";
		final String[] testArgs = {Command.DELETE_ATTACHMENTS.getMethodName(), filePath};

		final boolean actualResult = deleteAttachmentsService.isValidArguments(testArgs);

		Assert.assertFalse(actualResult);
		Mockito.verify(printUtilMock, Mockito.times(2)).printAndLog(Mockito.anyString());
	}

    @Test
    public void testIsValidArguments_InvalidParentEntity() throws Exception {
        final String filePath = "AppointmentAttachments.csv";
        final String[] testArgs = {Command.LOAD_ATTACHMENTS.getMethodName(), filePath};

        final boolean actualResult = deleteAttachmentsService.isValidArguments(testArgs);

        Assert.assertFalse(actualResult);
        Mockito.verify(printUtilMock, Mockito.times(2)).printAndLog(Mockito.anyString());
    }

	private String getFilePath(String filename) {
		final ClassLoader classLoader = getClass().getClassLoader();
		return new File(classLoader.getResource(filename).getFile()).getAbsolutePath();
	}
}

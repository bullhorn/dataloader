package com.bullhorn.dataloader.service;

import java.lang.reflect.Field;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.bullhorn.dataloader.util.PrintUtil;

public class CommandLineInterfaceTest {

    @Before
    public void setUp(){
    	DeleteAttachmentsService mockDeleteAttachmentsService = Mockito.mock(DeleteAttachmentsService.class);
    	DeleteService mokcDeleteService = Mockito.mock(DeleteService.class);
       	LoadAttachmentsService mockLoadAttachmentsService = Mockito.mock(LoadAttachmentsService.class);
       	LoadService mockLoadService = Mockito.mock(LoadService.class);
       	TemplateService mockTemplateService = Mockito.mock(TemplateService.class);
       	
       	Command.DELETE.setAction(mokcDeleteService);
       	Command.DELETE_ATTACHMENTS.setAction(mockDeleteAttachmentsService);
       	Command.LOAD.setAction(mockLoadService);
       	Command.LOAD_ATTACHMENTS.setAction(mockLoadAttachmentsService);
       	Command.TEMPLATE.setAction(mockTemplateService);
    }

    @Test
    public void deleteTest() throws Exception {
        String[] testArgs = {Command.DELETE.getMethodName(), "Candidate", "Candidate.csv"};
    	
        //arrange
    	Mockito.when(Command.DELETE.getAction().isValidArguments(testArgs)).thenReturn(true);
    	
        //act
        CommandLineInterface commandLineInterface = new CommandLineInterface();
        commandLineInterface.start(testArgs);

        //assert
        Mockito.verify(Command.DELETE.getAction(), Mockito.times(1)).isValidArguments(testArgs);
        Mockito.verify(Command.DELETE.getAction(), Mockito.times(1)).run(testArgs);
        
        Mockito.verify(Command.DELETE_ATTACHMENTS.getAction(), Mockito.never()).isValidArguments(Mockito.any());
        Mockito.verify(Command.DELETE_ATTACHMENTS.getAction(), Mockito.never()).run(Mockito.any());
        
        Mockito.verify(Command.LOAD.getAction(), Mockito.never()).isValidArguments(Mockito.any());
        Mockito.verify(Command.LOAD.getAction(), Mockito.never()).run(Mockito.any());

        Mockito.verify(Command.LOAD_ATTACHMENTS.getAction(), Mockito.never()).isValidArguments(Mockito.any());
        Mockito.verify(Command.LOAD_ATTACHMENTS.getAction(), Mockito.never()).run(Mockito.any());

        Mockito.verify(Command.TEMPLATE.getAction(), Mockito.never()).isValidArguments(Mockito.any());
        Mockito.verify(Command.TEMPLATE.getAction(), Mockito.never()).run(Mockito.any());
        
    }

    @Test
    public void deleteInvalidArgsTest() throws Exception {
        String[] testArgs = {Command.DELETE.getMethodName()};
    	
        //arrange
    	Mockito.when(Command.DELETE.getAction().isValidArguments(testArgs)).thenReturn(false);
    	
        //act
        CommandLineInterface commandLineInterface = new CommandLineInterface();
        commandLineInterface.start(testArgs);

        //assert
        Mockito.verify(Command.DELETE.getAction(), Mockito.times(1)).isValidArguments(testArgs);
        Mockito.verify(Command.DELETE.getAction(), Mockito.never()).run(Mockito.any());
        
        Mockito.verify(Command.DELETE_ATTACHMENTS.getAction(), Mockito.never()).isValidArguments(Mockito.any());
        Mockito.verify(Command.DELETE_ATTACHMENTS.getAction(), Mockito.never()).run(Mockito.any());
        
        Mockito.verify(Command.LOAD.getAction(), Mockito.never()).isValidArguments(Mockito.any());
        Mockito.verify(Command.LOAD.getAction(), Mockito.never()).run(Mockito.any());

        Mockito.verify(Command.LOAD_ATTACHMENTS.getAction(), Mockito.never()).isValidArguments(Mockito.any());
        Mockito.verify(Command.LOAD_ATTACHMENTS.getAction(), Mockito.never()).run(Mockito.any());

        Mockito.verify(Command.TEMPLATE.getAction(), Mockito.never()).isValidArguments(Mockito.any());
        Mockito.verify(Command.TEMPLATE.getAction(), Mockito.never()).run(Mockito.any());
        
    }

    @Test
    public void deleteAttachmentsTest() throws Exception {
        String[] testArgs = {Command.DELETE_ATTACHMENTS.getMethodName(), "Candidate", "Attachements.csv"};
    	
        //arrange
    	Mockito.when(Command.DELETE_ATTACHMENTS.getAction().isValidArguments(testArgs)).thenReturn(true);
    	
        //act
        CommandLineInterface commandLineInterface = new CommandLineInterface();
        commandLineInterface.start(testArgs);

        //assert
        Mockito.verify(Command.DELETE.getAction(), Mockito.never()).isValidArguments(Mockito.any());
        Mockito.verify(Command.DELETE.getAction(), Mockito.never()).run(Mockito.any());
        
        Mockito.verify(Command.DELETE_ATTACHMENTS.getAction(), Mockito.times(1)).isValidArguments(testArgs);
        Mockito.verify(Command.DELETE_ATTACHMENTS.getAction(), Mockito.times(1)).run(testArgs);
        
        Mockito.verify(Command.LOAD.getAction(), Mockito.never()).isValidArguments(Mockito.any());
        Mockito.verify(Command.LOAD.getAction(), Mockito.never()).run(Mockito.any());

        Mockito.verify(Command.LOAD_ATTACHMENTS.getAction(), Mockito.never()).isValidArguments(Mockito.any());
        Mockito.verify(Command.LOAD_ATTACHMENTS.getAction(), Mockito.never()).run(Mockito.any());

        Mockito.verify(Command.TEMPLATE.getAction(), Mockito.never()).isValidArguments(Mockito.any());
        Mockito.verify(Command.TEMPLATE.getAction(), Mockito.never()).run(Mockito.any());
        
    }

    @Test
    public void deleteAttachmentsInvalidArgsTest() throws Exception {
        String[] testArgs = {Command.DELETE_ATTACHMENTS.getMethodName(), "Candidate", "Attachements.csv"};
    	
        //arrange
    	Mockito.when(Command.DELETE_ATTACHMENTS.getAction().isValidArguments(testArgs)).thenReturn(false);
    	
        //act
        CommandLineInterface commandLineInterface = new CommandLineInterface();
        commandLineInterface.start(testArgs);

        //assert
        Mockito.verify(Command.DELETE.getAction(), Mockito.never()).isValidArguments(Mockito.any());
        Mockito.verify(Command.DELETE.getAction(), Mockito.never()).run(Mockito.any());
        
        Mockito.verify(Command.DELETE_ATTACHMENTS.getAction(), Mockito.times(1)).isValidArguments(testArgs);
        Mockito.verify(Command.DELETE_ATTACHMENTS.getAction(), Mockito.never()).run(Mockito.any());
        
        Mockito.verify(Command.LOAD.getAction(), Mockito.never()).isValidArguments(Mockito.any());
        Mockito.verify(Command.LOAD.getAction(), Mockito.never()).run(Mockito.any());

        Mockito.verify(Command.LOAD_ATTACHMENTS.getAction(), Mockito.never()).isValidArguments(Mockito.any());
        Mockito.verify(Command.LOAD_ATTACHMENTS.getAction(), Mockito.never()).run(Mockito.any());

        Mockito.verify(Command.TEMPLATE.getAction(), Mockito.never()).isValidArguments(Mockito.any());
        Mockito.verify(Command.TEMPLATE.getAction(), Mockito.never()).run(Mockito.any());
        
     }

    @Test
    public void loadTest() throws Exception {
        String[] testArgs = {Command.LOAD.getMethodName(), "Candidate.csv"};
    	
        //arrange
    	Mockito.when(Command.LOAD.getAction().isValidArguments(testArgs)).thenReturn(true);
    	
        //act
        CommandLineInterface commandLineInterface = new CommandLineInterface();
        commandLineInterface.start(testArgs);

        //assert
        Mockito.verify(Command.DELETE.getAction(), Mockito.never()).isValidArguments(Mockito.any());
        Mockito.verify(Command.DELETE.getAction(), Mockito.never()).run(Mockito.any());
        
        Mockito.verify(Command.DELETE_ATTACHMENTS.getAction(), Mockito.never()).isValidArguments(Mockito.any());
        Mockito.verify(Command.DELETE_ATTACHMENTS.getAction(), Mockito.never()).run(Mockito.any());
        
        Mockito.verify(Command.LOAD.getAction(), Mockito.times(1)).isValidArguments(testArgs);
        Mockito.verify(Command.LOAD.getAction(), Mockito.times(1)).run(testArgs);

        Mockito.verify(Command.LOAD_ATTACHMENTS.getAction(), Mockito.never()).isValidArguments(Mockito.any());
        Mockito.verify(Command.LOAD_ATTACHMENTS.getAction(), Mockito.never()).run(Mockito.any());

        Mockito.verify(Command.TEMPLATE.getAction(), Mockito.never()).isValidArguments(Mockito.any());
        Mockito.verify(Command.TEMPLATE.getAction(), Mockito.never()).run(Mockito.any());
        
    }

    @Test
    public void loadInvalidArgsTest() throws Exception {
        String[] testArgs = {Command.LOAD.getMethodName(), "Candidate.csv"};
    	
        //arrange
    	Mockito.when(Command.LOAD.getAction().isValidArguments(testArgs)).thenReturn(false);
    	
        //act
        CommandLineInterface commandLineInterface = new CommandLineInterface();
        commandLineInterface.start(testArgs);

        //assert
        Mockito.verify(Command.DELETE.getAction(), Mockito.never()).isValidArguments(Mockito.any());
        Mockito.verify(Command.DELETE.getAction(), Mockito.never()).run(Mockito.any());
        
        Mockito.verify(Command.DELETE_ATTACHMENTS.getAction(), Mockito.never()).isValidArguments(Mockito.any());
        Mockito.verify(Command.DELETE_ATTACHMENTS.getAction(), Mockito.never()).run(Mockito.any());
        
        Mockito.verify(Command.LOAD.getAction(), Mockito.times(1)).isValidArguments(testArgs);
        Mockito.verify(Command.LOAD.getAction(), Mockito.never()).run(Mockito.any());

        Mockito.verify(Command.LOAD_ATTACHMENTS.getAction(), Mockito.never()).isValidArguments(Mockito.any());
        Mockito.verify(Command.LOAD_ATTACHMENTS.getAction(), Mockito.never()).run(Mockito.any());

        Mockito.verify(Command.TEMPLATE.getAction(), Mockito.never()).isValidArguments(Mockito.any());
        Mockito.verify(Command.TEMPLATE.getAction(), Mockito.never()).run(Mockito.any());
        
    }

    @Test
    public void loadAttachmentTest() throws Exception {
        String[] testArgs = {Command.LOAD_ATTACHMENTS.getMethodName(), "Candidate", "Attachements.csv"};
    	
        //arrange
    	Mockito.when(Command.LOAD_ATTACHMENTS.getAction().isValidArguments(testArgs)).thenReturn(true);
    	
        //act
        CommandLineInterface commandLineInterface = new CommandLineInterface();
        commandLineInterface.start(testArgs);

        //assert
        Mockito.verify(Command.DELETE.getAction(), Mockito.never()).isValidArguments(Mockito.any());
        Mockito.verify(Command.DELETE.getAction(), Mockito.never()).run(Mockito.any());
        
        Mockito.verify(Command.DELETE_ATTACHMENTS.getAction(), Mockito.never()).isValidArguments(Mockito.any());
        Mockito.verify(Command.DELETE_ATTACHMENTS.getAction(), Mockito.never()).run(Mockito.any());
        
        Mockito.verify(Command.LOAD.getAction(), Mockito.never()).isValidArguments(Mockito.any());
        Mockito.verify(Command.LOAD.getAction(), Mockito.never()).run(Mockito.any());

        Mockito.verify(Command.LOAD_ATTACHMENTS.getAction(), Mockito.times(1)).isValidArguments(testArgs);
        Mockito.verify(Command.LOAD_ATTACHMENTS.getAction(), Mockito.times(1)).run(testArgs);

        Mockito.verify(Command.TEMPLATE.getAction(), Mockito.never()).isValidArguments(Mockito.any());
        Mockito.verify(Command.TEMPLATE.getAction(), Mockito.never()).run(Mockito.any());
        
    }

    @Test
    public void loadAttachmentInvalidArgsTest() throws Exception {
        String[] testArgs = {Command.LOAD_ATTACHMENTS.getMethodName(), "Candidate"};
    	
        //arrange
    	Mockito.when(Command.LOAD_ATTACHMENTS.getAction().isValidArguments(testArgs)).thenReturn(false);
    	
        //act
        CommandLineInterface commandLineInterface = new CommandLineInterface();
        commandLineInterface.start(testArgs);

        //assert
        Mockito.verify(Command.DELETE.getAction(), Mockito.never()).isValidArguments(Mockito.any());
        Mockito.verify(Command.DELETE.getAction(), Mockito.never()).run(Mockito.any());
        
        Mockito.verify(Command.DELETE_ATTACHMENTS.getAction(), Mockito.never()).isValidArguments(Mockito.any());
        Mockito.verify(Command.DELETE_ATTACHMENTS.getAction(), Mockito.never()).run(Mockito.any());
        
        Mockito.verify(Command.LOAD.getAction(), Mockito.never()).isValidArguments(Mockito.any());
        Mockito.verify(Command.LOAD.getAction(), Mockito.never()).run(Mockito.any());

        Mockito.verify(Command.LOAD_ATTACHMENTS.getAction(), Mockito.times(1)).isValidArguments(testArgs);
        Mockito.verify(Command.LOAD_ATTACHMENTS.getAction(), Mockito.never()).run(Mockito.any());

        Mockito.verify(Command.TEMPLATE.getAction(), Mockito.never()).isValidArguments(Mockito.any());
        Mockito.verify(Command.TEMPLATE.getAction(), Mockito.never()).run(Mockito.any());
        
    }

    @Test
    public void templateTest() throws Exception {
        String[] testArgs = {Command.TEMPLATE.getMethodName(), "Candidate"};
    	
        //arrange
    	Mockito.when(Command.TEMPLATE.getAction().isValidArguments(testArgs)).thenReturn(true);
    	
        //act
        CommandLineInterface commandLineInterface = new CommandLineInterface();
        commandLineInterface.start(testArgs);

        //assert
        Mockito.verify(Command.DELETE.getAction(), Mockito.never()).isValidArguments(Mockito.any());
        Mockito.verify(Command.DELETE.getAction(), Mockito.never()).run(Mockito.any());
        
        Mockito.verify(Command.DELETE_ATTACHMENTS.getAction(), Mockito.never()).isValidArguments(Mockito.any());
        Mockito.verify(Command.DELETE_ATTACHMENTS.getAction(), Mockito.never()).run(Mockito.any());
        
        Mockito.verify(Command.LOAD.getAction(), Mockito.never()).isValidArguments(Mockito.any());
        Mockito.verify(Command.LOAD.getAction(), Mockito.never()).run(Mockito.any());

        Mockito.verify(Command.LOAD_ATTACHMENTS.getAction(), Mockito.never()).isValidArguments(Mockito.any());
        Mockito.verify(Command.LOAD_ATTACHMENTS.getAction(), Mockito.never()).run(Mockito.any());

        Mockito.verify(Command.TEMPLATE.getAction(), Mockito.times(1)).isValidArguments(testArgs);
        Mockito.verify(Command.TEMPLATE.getAction(), Mockito.times(1)).run(testArgs);
        
    }

    @Test
    public void templateInvalidArgsTest() throws Exception {
        String[] testArgs = {Command.TEMPLATE.getMethodName(), "Candidates"};
    	
        // arrange
		PrintUtil printUtilMock = Mockito.mock(PrintUtil.class);
		
		Mockito.doNothing().when(printUtilMock).printUsage();

		Mockito.when(Command.TEMPLATE.getAction().isValidArguments(testArgs)).thenReturn(false);
    	
		CommandLineInterface commandLineInterface = Mockito.spy(new CommandLineInterface());

		// mock out AbstractService Methods that should not be called
		Mockito.doThrow(new RuntimeException("should not be called")).when(commandLineInterface).createEntityAttachmentConcurrencyService(Mockito.any(), Mockito.anyString(), Mockito.anyString());
		Mockito.doThrow(new RuntimeException("should not be called")).when(commandLineInterface).createEntityConcurrencyService(Mockito.any(), Mockito.anyString(), Mockito.anyString());
		Mockito.doThrow(new RuntimeException("should not be called")).when(commandLineInterface).createSession();
		Mockito.doThrow(new RuntimeException("should not be called")).when(commandLineInterface).getExecutorService(Mockito.any());

		// track this call
		Mockito.doNothing().when(commandLineInterface).printAndLog(Mockito.anyString());
		
		// act
        commandLineInterface.start(testArgs);

        // assert
        Mockito.verify(Command.DELETE.getAction(), Mockito.never()).isValidArguments(Mockito.any());
        Mockito.verify(Command.DELETE.getAction(), Mockito.never()).run(Mockito.any());
        
        Mockito.verify(Command.DELETE_ATTACHMENTS.getAction(), Mockito.never()).isValidArguments(Mockito.any());
        Mockito.verify(Command.DELETE_ATTACHMENTS.getAction(), Mockito.never()).run(Mockito.any());
        
        Mockito.verify(Command.LOAD.getAction(), Mockito.never()).isValidArguments(Mockito.any());
        Mockito.verify(Command.LOAD.getAction(), Mockito.never()).run(Mockito.any());

        Mockito.verify(Command.LOAD_ATTACHMENTS.getAction(), Mockito.never()).isValidArguments(Mockito.any());
        Mockito.verify(Command.LOAD_ATTACHMENTS.getAction(), Mockito.never()).run(Mockito.any());

        Mockito.verify(Command.TEMPLATE.getAction(), Mockito.times(1)).isValidArguments(testArgs);
        Mockito.verify(Command.TEMPLATE.getAction(), Mockito.never()).run(Mockito.any());
        
        Mockito.verify(printUtilMock, Mockito.never()).printUsage();
        Mockito.verify(commandLineInterface, Mockito.never()).printAndLog(Mockito.any());
    }
    
    @Test
    public void invalidCommandTest() throws Exception {
        String[] testArgs = {"bad", "Candidate"};
    	
        //arrange
		PrintUtil printUtilMock = Mockito.mock(PrintUtil.class);
		
		Mockito.doNothing().when(printUtilMock).printUsage();

		CommandLineInterface commandLineInterface = Mockito.spy(new CommandLineInterface());
		Field prinUtilField = AbstractService.class.getDeclaredField("printUtil");
		prinUtilField.setAccessible(true);
		prinUtilField.set(commandLineInterface, printUtilMock);

		// mock out AbstractService Methods that should not be called
		Mockito.doThrow(new RuntimeException("should not be called")).when(commandLineInterface).createEntityAttachmentConcurrencyService(Mockito.any(), Mockito.anyString(), Mockito.anyString());
		Mockito.doThrow(new RuntimeException("should not be called")).when(commandLineInterface).createEntityConcurrencyService(Mockito.any(), Mockito.anyString(), Mockito.anyString());
		Mockito.doThrow(new RuntimeException("should not be called")).when(commandLineInterface).createSession();
		Mockito.doThrow(new RuntimeException("should not be called")).when(commandLineInterface).getExecutorService(Mockito.any());

		// track this call
		Mockito.doNothing().when(commandLineInterface).printAndLog(Mockito.anyString());

		//act
        commandLineInterface.start(testArgs);
        
        //assert
        Mockito.verify(Command.DELETE.getAction(), Mockito.never()).isValidArguments(Mockito.any());
        Mockito.verify(Command.DELETE.getAction(), Mockito.never()).run(Mockito.any());
        
        Mockito.verify(Command.DELETE_ATTACHMENTS.getAction(), Mockito.never()).isValidArguments(Mockito.any());
        Mockito.verify(Command.DELETE_ATTACHMENTS.getAction(), Mockito.never()).run(Mockito.any());
        
        Mockito.verify(Command.LOAD.getAction(), Mockito.never()).isValidArguments(Mockito.any());
        Mockito.verify(Command.LOAD.getAction(), Mockito.never()).run(Mockito.any());

        Mockito.verify(Command.LOAD_ATTACHMENTS.getAction(), Mockito.never()).isValidArguments(Mockito.any());
        Mockito.verify(Command.LOAD_ATTACHMENTS.getAction(), Mockito.never()).run(Mockito.any());

        Mockito.verify(Command.TEMPLATE.getAction(), Mockito.never()).isValidArguments(Mockito.any());
        Mockito.verify(Command.TEMPLATE.getAction(), Mockito.never()).run(Mockito.any());

        Mockito.verify(printUtilMock, Mockito.times(1)).printUsage();
        Mockito.verify(commandLineInterface, Mockito.times(1)).printAndLog(Mockito.any());
    }

}

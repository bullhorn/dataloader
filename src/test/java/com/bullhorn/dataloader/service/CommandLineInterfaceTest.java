package com.bullhorn.dataloader.service;

import com.bullhorn.dataloader.service.consts.Method;
import com.bullhorn.dataloader.util.PrintUtil;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;

public class CommandLineInterfaceTest {

    public CommandLineInterface commandLineInterface;

    @Before
    public void setUp(){
        commandLineInterface = Mockito.spy(new CommandLineInterface());
    }

    @Test
    public void templateTest() throws Exception {
        //arrange
        String[] testArgs = {Method.TEMPLATES.getMethodName(), "Candidate"};
        doNothing().when(commandLineInterface).template(anyString());

        //act
        commandLineInterface.start(testArgs);

        //assert
        verify(commandLineInterface).template("Candidate");
    }

    @Test
    public void loadTest() throws Exception {
        //arrange
        String[] testArgs = {Method.LOAD.getMethodName(), "Candidate", "Test.csv"};
        doNothing().when(commandLineInterface).load(any(), anyString(), anyString());

        //act
        commandLineInterface.start(testArgs);

        //assert
        verify(commandLineInterface).load(Method.LOAD, "Candidate", "Test.csv");
    }

    @Test
    public void deleteTest() throws Exception {
        //arrange
        String[] testArgs = {Method.DELETE.getMethodName(), "Candidate", "Test.csv"};
        doNothing().when(commandLineInterface).delete(any(), anyString(), anyString());

        //act
        commandLineInterface.start(testArgs);

        //assert
        verify(commandLineInterface).delete(Method.DELETE, "Candidate", "Test.csv");
    }

    @Test
    public void loadAttachmentTest() throws Exception {
        //arrange
        String[] testArgs = {Method.LOADATTACHMENTS.getMethodName(), "Candidate", "src/test/resources/CandidateAttachments.csv"};
        doNothing().when(commandLineInterface).loadAttachments(any(), anyString(), anyString());

        //act
        commandLineInterface.start(testArgs);

        //assert
        verify(commandLineInterface).loadAttachments(Method.LOADATTACHMENTS, "Candidate", "src/test/resources/CandidateAttachments.csv");
    }

    @Test
    public void noValidMethodTest() throws Exception {
        //arrange
        commandLineInterface.printUtil = Mockito.mock(PrintUtil.class);
        String[] testArgs = {"Insanity", "Candidate", "Nothing"};

        //act
        commandLineInterface.start(testArgs);

        //assert
        verify(commandLineInterface.printUtil).printUsage();
    }

}

package com.bullhorn.dataloader.service;

import com.bullhorn.dataloader.enums.Command;
import com.bullhorn.dataloader.util.PrintUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class HelpServiceTest {

    private PrintUtil printUtilMock;
    private HelpService helpService;

    @Before
    public void setup() throws Exception {
        printUtilMock = Mockito.mock(PrintUtil.class);
        helpService = Mockito.spy(new HelpService(printUtilMock));
    }

    @Test
    public void runTest() throws Exception {
        final String[] testArgs = {Command.HELP.getMethodName()};

        helpService.run(testArgs);

        Mockito.verify(printUtilMock, Mockito.times(1)).printUsage();
    }

    @Test
    public void isValidArgumentsTest() throws Exception {
        final String[] testArgs = {Command.HELP.getMethodName()};

        boolean result = helpService.isValidArguments(testArgs);

        Assert.assertTrue(result);
    }

}

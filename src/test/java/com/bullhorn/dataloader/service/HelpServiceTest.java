package com.bullhorn.dataloader.service;

import com.bullhorn.dataloader.enums.Command;
import com.bullhorn.dataloader.util.PrintUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class HelpServiceTest {

    private PrintUtil printUtilMock;
    private HelpService helpService;

    @Before
    public void setup() {
        printUtilMock = mock(PrintUtil.class);
        helpService = new HelpService(printUtilMock);
    }

    @Test
    public void testRun() {
        final String[] testArgs = {Command.HELP.getMethodName()};

        helpService.run(testArgs);

        verify(printUtilMock, times(1)).printUsage();
    }

    @Test
    public void testIsValidArguments() {
        final String[] testArgs = {Command.HELP.getMethodName()};

        boolean result = helpService.isValidArguments(testArgs);

        Assert.assertTrue(result);
    }

}

package com.bullhorn.dataloader.service;

import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.bullhorn.dataloader.enums.Command;
import com.bullhorn.dataloader.rest.RestSession;
import com.bullhorn.dataloader.util.PrintUtil;
import com.bullhornsdk.data.exception.RestApiException;

public class LoginServiceTest {

    private RestSession restSessionMock;
    private PrintUtil printUtilMock;
    private LoginService loginService;

    @Before
    public void setup() {
        restSessionMock = mock(RestSession.class);
        printUtilMock = mock(PrintUtil.class);
        loginService = new LoginService(restSessionMock, printUtilMock);
    }

    @Test
    public void testRunLoginSuccessful() {
        final String[] testArgs = {Command.LOGIN.getMethodName()};

        loginService.run(testArgs);

        verify(printUtilMock, times(1)).printAndLog(eq("Login Successful"));
    }

    @Test
    public void testRunLoginFailed() {
        final String[] testArgs = {Command.LOGIN.getMethodName()};
        when(restSessionMock.getRestApi()).thenThrow(new RestApiException("fail"));

        loginService.run(testArgs);

        verify(printUtilMock, times(1)).printAndLog(eq("Login Failed"));
    }


    @Test
    public void testIsValidArguments() {
        final String[] testArgs = {Command.LOGIN.getMethodName()};

        boolean result = loginService.isValidArguments(testArgs);

        Assert.assertTrue(result);
    }
}

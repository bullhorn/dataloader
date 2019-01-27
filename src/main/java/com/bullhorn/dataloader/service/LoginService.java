package com.bullhorn.dataloader.service;

import com.bullhorn.dataloader.rest.RestSession;
import com.bullhorn.dataloader.util.PrintUtil;

/**
 * Test user provided credentials to see if login is successful
 */
public class LoginService implements Action {

    private final RestSession restSession;
    private final PrintUtil printUtil;

    LoginService(RestSession restSession, PrintUtil printUtil) {
        this.restSession = restSession;
        this.printUtil = printUtil;
    }

    @Override
    public void run(String[] args) {
        try {
            restSession.getRestApi();
            printUtil.printAndLog("Login Successful");
        } catch (Exception e) {
            printUtil.printAndLog("Login Failed");
        }
    }

    @Override
    public boolean isValidArguments(String[] args) {
        return true;
    }
}

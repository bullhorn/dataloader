package com.bullhorn.dataloader.service;

import com.bullhorn.dataloader.rest.RestSession;
import com.bullhorn.dataloader.util.FileUtil;
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
        String output = isLoginSuccessful() ? "Login Successful" : "Login Failed";
        printUtil.printAndLog(output);
        FileUtil.writeStringToFileAndLogException("login.txt", output, printUtil);
    }

    @Override
    public boolean isValidArguments(String[] args) {
        return true;
    }

    private boolean isLoginSuccessful() {
        try {
            restSession.getRestApi();
        } catch (Exception e) {
            return false;
        }
        return true;
    }
}

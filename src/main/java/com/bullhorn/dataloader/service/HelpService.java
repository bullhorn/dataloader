package com.bullhorn.dataloader.service;

import com.bullhorn.dataloader.util.PrintUtil;
import com.bullhorn.dataloader.util.PropertyFileUtil;
import com.bullhorn.dataloader.util.validation.ValidationUtil;

import java.io.IOException;
import java.io.InputStream;

public class HelpService extends AbstractService implements Action {

    public HelpService(PrintUtil printUtil,
                           PropertyFileUtil propertyFileUtil,
                           ValidationUtil validationUtil, InputStream inputStream) throws IOException {
        super(printUtil, propertyFileUtil, validationUtil, inputStream);
    }

    @Override
    public void run(String[] args) {
        try {
            printUtil.printUsage();
        } catch (Exception e) {
            printUtil.printAndLog(e);
        }
    }

    @Override
    public boolean isValidArguments(String[] args) {
        return true;
    }
}

package com.bullhorn.dataloader.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;


public class FileUtil {

    private static InputStream getPropertiesInputStream(String fileName) throws FileNotFoundException {

        if (null != System.getProperty(StringConsts.PROPERTYFILE_ARG)) {
            fileName = System.getProperty(StringConsts.PROPERTYFILE_ARG);
        }

        return new FileInputStream(fileName);
    }

    public Properties getProps(String fileName) throws IOException {
        InputStream inputStream = getPropertiesInputStream(fileName);
        Properties properties = new Properties();
        properties.load(inputStream);
        inputStream.close();

        return properties;
    }
}

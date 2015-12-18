package com.bullhorn.dataloader.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;


public class FileUtil {

    private static InputStream getPropertiesInputStream(String fileName) throws FileNotFoundException {
        String propertyFilePath;

        if (null != System.getProperty(StringConsts.PROPERTYFILE_ARG)) {
            propertyFilePath = System.getProperty(StringConsts.PROPERTYFILE_ARG);
        } else {
            String configurationPath;
            String os = System.getProperty("os.name").toLowerCase();
            if (os.contains("windows")) {
                configurationPath = "C:\\bullhorn\\conf\\";
            } else {
                configurationPath = "/usr/local/bullhorn/conf/";
            }
            propertyFilePath = configurationPath + fileName;
        }

        return new FileInputStream(propertyFilePath);
    }

    public Properties getProps(String fileName) throws IOException {
        InputStream inputStream = getPropertiesInputStream(fileName);
        Properties properties = new Properties();
        properties.load(inputStream);
        inputStream.close();

        return properties;
    }
}

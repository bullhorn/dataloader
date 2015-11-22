package com.bullhorn.dataloader.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;


public class FileUtil {

    private static InputStream getPropertiesInputStream(String fileName) throws FileNotFoundException {

        String configurationPath;
        if (null != System.getProperty(StringConsts.CONF_PATH_ARG)) {
            configurationPath = System.getProperty(StringConsts.CONF_PATH_ARG);
        } else {
            String os = System.getProperty("os.name").toLowerCase();
            if (os.contains("windows")) {
                configurationPath = "C:\\bullhorn\\conf\\";
            } else {
                configurationPath = "/usr/local/bullhorn/conf/";
            }
        }

        return new FileInputStream(configurationPath + fileName);
    }

    public Properties getProps(String fileName) throws IOException {
        InputStream inputStream = getPropertiesInputStream(fileName);
        Properties properties = new Properties();
        properties.load(inputStream);
        inputStream.close();

        return properties;
    }
}

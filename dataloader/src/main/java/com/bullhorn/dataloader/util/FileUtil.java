package com.bullhorn.dataloader.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;


public class FileUtil {

    private InputStream getPropertiesInputStream(String fileName) throws FileNotFoundException {

        String os = System.getProperty("os.name").toLowerCase();
        String path = "/usr/local/bullhorn/conf/";
        if (os.contains("windows")) {
            path = "C:\\bullhorn\\conf\\";
        }
        InputStream inputStream = new FileInputStream(path + fileName);

        return inputStream;
    }

    public Properties getProps(String fileName) throws IOException {
        InputStream inputStream = getPropertiesInputStream(fileName);
        Properties properties = new Properties();
        properties.load(inputStream);
        inputStream.close();

        return properties;
    }
}

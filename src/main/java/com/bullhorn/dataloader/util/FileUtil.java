package com.bullhorn.dataloader.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FileUtil {

    private static Logger log = LogManager.getLogger(FileUtil.class);

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

        logProperties(fileName, properties);

        return properties;
    }

    private void logProperties(String fileName, Properties properties) {
        if (null != System.getProperty(StringConsts.PROPERTYFILE_ARG)) {
            fileName = System.getProperty(StringConsts.PROPERTYFILE_ARG);
        }
        
        log.info("Using properties file: " + fileName);

        log.info("# Section 2");
        logPropertiesEndingWith(properties, "Url");

        log.info("# Section 3");
        logPropertiesEndingWith(properties, "ExistField");
        logPropertyIfExists(properties, "frontLoadedEntities");

        log.info("# Section 4");
        logPropertyIfExists(properties, "listDelimiter");
        logPropertyIfExists(properties, "dateFormat");

        log.info("# Section 5");
        logPropertyIfExists(properties, "numThreads");
        logPropertyIfExists(properties, "pageSize");
        logPropertyIfExists(properties, "cacheSize");
    }

    private void logPropertiesEndingWith(Properties properties, String endingText) {
        List<String> propertyNames = new ArrayList<String>(properties.stringPropertyNames());
        Collections.sort(propertyNames);
        for (String property : propertyNames) {
            if (property.endsWith(endingText)) {
                logPropertyIfExists(properties, property);
            }
        }
    }

    private void logPropertyIfExists(Properties properties, String property) {
        if (properties.containsKey(property)) {
            log.info("   " + property + "=" + properties.getProperty(property));
        }
    }
}

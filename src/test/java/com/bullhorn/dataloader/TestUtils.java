package com.bullhorn.dataloader;

import java.io.File;

/**
 * Utilities used in tests
 */
public class TestUtils {
    public static String getEnvironmentVariable(String name) {
        String envVar = System.getenv(name);
        if (envVar == null) {
            throw new IllegalArgumentException("Test Setup Error: Missing Environment Variable: '" + name + "'");
        }
        return envVar;
    }

    public static String getFilePath(String filename) throws NullPointerException {
        final ClassLoader classLoader = TestUtils.class.getClassLoader();
        return new File(classLoader.getResource(filename).getFile()).getAbsolutePath();
    }
}

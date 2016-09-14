package com.bullhorn.dataloader.integration;

import org.junit.Before;
import org.junit.Test;

import java.io.File;

public class IntegrationTest {

    @Before
    public void setup() {
        // Use the properties file from the test/resources directory
        System.setProperty("propertyfile", getFilePath("integrationTest.properties"));
    }

    @Test
    public void testLoadAndDeleteFromFolder() {
        System.out.println("\n\nTest Load and Delete From Folder\n\n");

        // TODO: 1. Allow for session creation from here, then TravisCI
        // TODO: 2. Allow for changing of `-ext-1` to something unique
        // TODO: 3. Enable upload of entire examples/load/ folder
        // TODO: 4. Enable delete from entire results/ folder
        // TODO: 5. Allow for success/failure assertions
    }

    private String getFilePath(String filename) {
        final ClassLoader classLoader = getClass().getClassLoader();
        return new File(classLoader.getResource(filename).getFile()).getAbsolutePath();
    }
}

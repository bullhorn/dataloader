package com.bullhorn.dataloader;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;

public class MainTest {

    private PrintStream originalSystemOut;
    private ByteArrayOutputStream outputStream;

    @Before
    public void setup() {
        originalSystemOut = System.out;

        // Replace System.out with our own stream for testing
        outputStream = new ByteArrayOutputStream();
        PrintStream printStream = new PrintStream(outputStream);
        System.setOut(printStream);

        // Use the dataloader.properties file from the test/resources directory
        System.setProperty("propertyfile", getFilePath("dataloader.properties"));
    }

    @After
    public void teardown() {
        // Set System.out back when we're done
        System.setOut(originalSystemOut);
    }

    @Test
    public void testMain_BadFileInput() {
        String[] args = {"load", "file.bad"};

        Main main = new Main();
        main.main(args);

        Assert.assertTrue(outputStream.toString().contains("ERROR: Cannot access: file.bad"));
    }

    private String getFilePath(String filename) {
        final ClassLoader classLoader = getClass().getClassLoader();
        return new File(classLoader.getResource(filename).getFile()).getAbsolutePath();
    }
}

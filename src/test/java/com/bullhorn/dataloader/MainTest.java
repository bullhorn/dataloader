package com.bullhorn.dataloader;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
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

        // Use the properties file from the test/resources directory
        System.setProperty("propertyfile", TestUtils.getResourceFilePath("unitTest.properties"));
    }

    @After
    public void teardown() {
        // Set System.out back when we're done
        System.setOut(originalSystemOut);
    }

    @Test
    public void testMain_BadFileInput() {
        String[] args = {"load", "file.bad"};

        Main.main(args);

        Assert.assertTrue(outputStream.toString().contains("ERROR: Cannot access: file.bad"));
    }
}

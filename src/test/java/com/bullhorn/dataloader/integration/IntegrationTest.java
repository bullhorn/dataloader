package com.bullhorn.dataloader.integration;

import com.bullhorn.dataloader.Main;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class IntegrationTest {

    private ConsoleOutputCapturer consoleOutputCapturer;

    @Before
    public void setup() throws IOException {
        // Use the properties file from the test/resources directory
        System.setProperty("propertyfile", getFilePath("integrationTest.properties"));

        // Use environment variables to drive the login DArgs from TravisCI
        System.setProperty("username", getEnvironmentVariable("USERNAME"));
        System.setProperty("password", getEnvironmentVariable("PASSWORD"));
        System.setProperty("clientId", getEnvironmentVariable("CLIENT_ID"));
        System.setProperty("clientSecret", getEnvironmentVariable("CLIENT_SECRET"));

        // Capture command line output as a string without stopping the real-time printout
        consoleOutputCapturer = new ConsoleOutputCapturer();
        consoleOutputCapturer.start();

        // Put a "yes" response into the System.in for accepting the load/delete from directory
        InputStream inputStream = IOUtils.toInputStream("yes", "UTF-8");
        System.setIn(inputStream);
    }

    // TODO: 1. Allow for session creation from here, then TravisCI
    // TODO: 2. Allow for changing of `-ext-1` to something unique
    // TODO: 3. Enable upload of entire examples/load/ folder
    // TODO: 4. Enable delete from entire results/ folder
    // TODO: 5. Allow for success/failure assertions

    @Test
    public void loadFromFolder() throws IOException {
        String[] args = {"load", "examples/load/HousingComplex.csv"};

        Main.main(args);

        String cmdLineOutput = consoleOutputCapturer.stop();
        Assert.assertTrue(cmdLineOutput.contains("ERROR"));
    }

    private String getEnvironmentVariable(String name) {
        String envVar = System.getenv(name);
        if (envVar == null) {
            throw new IllegalArgumentException("Integration Test Setup Error: Missing Environment Variable: '" + name + "'");
        }
        return envVar;
    }

    private String getFilePath(String filename) {
        final ClassLoader classLoader = getClass().getClassLoader();
        return new File(classLoader.getResource(filename).getFile()).getAbsolutePath();
    }
}

package com.bullhorn.dataloader.integration;

import com.bullhorn.dataloader.Main;
import com.bullhorn.dataloader.TestUtils;
import com.bullhorn.dataloader.util.StringConsts;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;

public class IntegrationTest {

    private ConsoleOutputCapturer consoleOutputCapturer;

    @Before
    public void setup() throws IOException {
        // Use the properties file from the test/resources directory
        System.setProperty("propertyfile", TestUtils.getFilePath("integrationTest.properties"));

        // Use environment variables to drive the login DArgs from TravisCI
        System.setProperty("username", TestUtils.getEnvironmentVariable("USERNAME"));
        System.setProperty("password", TestUtils.getEnvironmentVariable("PASSWORD"));
        System.setProperty("clientId", TestUtils.getEnvironmentVariable("CLIENT_ID"));
        System.setProperty("clientSecret", TestUtils.getEnvironmentVariable("CLIENT_SECRET"));

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
        String examplesDir = "examples/load_" + StringConsts.TIMESTAMP;
        String externalID = "-ext-" + StringConsts.TIMESTAMP;
        createExampleDirectory("examples/load", examplesDir, "-ext-1", externalID);
        String[] args = {"load", examplesDir};

        Main.main(args);

        String cmdLineOutput = consoleOutputCapturer.stop();
        Assert.assertTrue(cmdLineOutput.contains("ERROR"));
    }

    /**
     * Given a directory with CSV files in it, this method will move all of those files to a new directory
     * and will string replace all instances in all files of the given text.
     *
     * @param originalDir The source directory path
     * @param newDir The path to the new directory to create
     * @param findText The text to find in the files
     * @param replaceText The text to replace all instances of findText with
     */
    private void createExampleDirectory(String originalDir, String newDir, String findText, String replaceText) {

    }
}

package com.bullhorn.dataloader.integration;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;

/**
 * Fun little output capture class, copied from:
 * http://stackoverflow.com/questions/8708342/redirect-console-output-to-string-in-java
 */
class ConsoleOutputCapturer {
    private ByteArrayOutputStream byteArrayOutputStream;
    private PrintStream previous;
    private boolean capturing;

    void start() {
        if (capturing) {
            return;
        }

        capturing = true;
        previous = System.out;
        byteArrayOutputStream = new ByteArrayOutputStream();

        OutputStream outputStreamCombiner =
            new OutputStreamCombiner(Arrays.asList(previous, byteArrayOutputStream));
        PrintStream custom = new PrintStream(outputStreamCombiner);

        System.setOut(custom);
    }

    String stop() {
        if (!capturing) {
            return "";
        }

        System.setOut(previous);

        String capturedValue = byteArrayOutputStream.toString();

        byteArrayOutputStream = null;
        previous = null;
        capturing = false;

        return capturedValue;
    }

    private static class OutputStreamCombiner extends OutputStream {
        private final List<OutputStream> outputStreams;

        OutputStreamCombiner(List<OutputStream> outputStreams) {
            this.outputStreams = outputStreams;
        }

        public void write(int b) throws IOException {
            for (OutputStream os : outputStreams) {
                os.write(b);
            }
        }

        public void flush() throws IOException {
            for (OutputStream os : outputStreams) {
                os.flush();
            }
        }

        public void close() throws IOException {
            for (OutputStream os : outputStreams) {
                os.close();
            }
        }
    }
}

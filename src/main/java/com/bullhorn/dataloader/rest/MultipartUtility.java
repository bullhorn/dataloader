package com.bullhorn.dataloader.rest;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

public class MultipartUtility {
    private static final Logger log = Logger.getLogger(MultipartUtility.class);

    private static final String LINE_FEED = "\r\n";
    private static final String CHARSET = "UTF-8";

    private final String boundary;
    private HttpURLConnection httpConn;
    private OutputStream outputStream;
    private PrintWriter writer;

    /**
     * This constructor initializes a new HTTP PUT request with content type
     * is set to multipart/form-data
     *
     * @param requestURL
     * @throws IOException
     */
    public MultipartUtility(String requestURL) throws IOException {
        boundary = "===" + System.currentTimeMillis() + "===";

        URL url = new URL(requestURL);
        httpConn = (HttpURLConnection) url.openConnection();
        httpConn.setUseCaches(false);
        httpConn.setRequestMethod("PUT");
        httpConn.setDoOutput(true);
        httpConn.setDoInput(true);
        httpConn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

        outputStream = httpConn.getOutputStream();
        writer = new PrintWriter(new OutputStreamWriter(outputStream, CHARSET), true);
    }


    /**
     * Adds a form field to the request
     *
     * @param name  field name
     * @param value field value
     */
    public void addFormField(String name, String value) {
        writer.append("--" + boundary).append(LINE_FEED);
        writer.append("Content-Disposition: form-data; name=\"" + name + "\"");
        writer.append(LINE_FEED);
        writer.append("Content-Type: text/plain; charset=" + CHARSET);
        writer.append(LINE_FEED);
        writer.append(LINE_FEED);
        writer.append(value).append(LINE_FEED);
        writer.flush();
    }

    
    /**
     * Adds a upload file section to the request
     *
     * @param fieldName  name attribute in <input type="file" name="..." />
     * @param uploadFile a File to be uploaded
     * @throws IOException
     */
    public void addFilePart(String fieldName, File uploadFile) throws IOException {
        addFilePart(fieldName, uploadFile, uploadFile.getName());
    }


    /**
     * Adds a upload file section to the request
     *
     * @param fieldName  name attribute in <input type="file" name="..." />
     * @param uploadFile a File to be uploaded
     * @throws IOException
     */
    public void addFilePart(String fieldName, File uploadFile, String fileName) throws IOException {
        writer.append("--" + boundary);
        writer.append(LINE_FEED);
        writer.append("Content-Disposition: form-data; name=\"" + fieldName + "\"; filename=\"" + fileName + "\"");
        writer.append(LINE_FEED);
        writer.append("Content-Type: " + URLConnection.guessContentTypeFromName(fileName));
        writer.append(LINE_FEED);
        writer.append("Content-Transfer-Encoding: binary");
        writer.append(LINE_FEED);
        writer.append(LINE_FEED);
        writer.flush();

        FileInputStream inputStream = new FileInputStream(uploadFile);
        byte[] buffer = new byte[4096];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }
        outputStream.flush();
        inputStream.close();
        writer.append(LINE_FEED);
        writer.flush();
    }

    /**
     * Adds a header field to the request.
     *
     * @param name  - name of the header field
     * @param value - value of the header field
     */
    public void addHeaderField(String name, String value) {
        writer.append(name + ": " + value).append(LINE_FEED);
        writer.flush();
    }

    /**
     * Completes the request and receives response from the server.
     *
     * @return a list of Strings as response in case the server returned
     * status OK, otherwise an exception is thrown.
     * @throws IOException
     */
    public HttpResult finish() throws IOException {
        writer.append(LINE_FEED).flush();
        writer.append("--" + boundary + "--").append(LINE_FEED);
        writer.close();

        HttpResult httpResult = readIntoHttpResult(httpConn);
        httpConn.disconnect();

        return httpResult;
    }

    public static HttpResult readIntoHttpResult(HttpURLConnection httpConn) throws IOException {
        HttpResult httpResult = new HttpResult(httpConn.getResponseCode());
        if (httpResult.status < HttpURLConnection.HTTP_BAD_REQUEST) {
            httpResult.body = readStream(httpConn.getInputStream());
        } else {
            httpResult.body = readStream(httpConn.getErrorStream());
        }
        return httpResult;
    }

    private static String readStream(InputStream input) {
        try {
            StringWriter writer = new StringWriter();
            IOUtils.copy(input, writer, CHARSET);
            return writer.toString();
        } catch (Exception ex) {
            log.error("Error reading HTML stream", ex);
        }

        return null;
    }
}



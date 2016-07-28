package com.bullhorn.dataloader.service.csv;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.commons.io.FilenameUtils;

import com.bullhorn.dataloader.consts.TaskConsts;
import com.bullhorn.dataloader.service.consts.Method;
import com.bullhorn.dataloader.util.ArrayUtil;
import com.bullhorn.dataloader.util.StringConsts;
import com.csvreader.CsvWriter;

/**
 * A thread-safe file writer for outputting results into both a success and a failure CSV file.
 */
public class CsvFileWriter {

    public static final String RESULTS_DIR = "results/";
    public static final String BULLHORN_ID_COLUMN = "id";
    public static final String ACTION_COLUMN = "action";
    public static final String REASON_COLUMN = "reason";
    public static final String SUCCESS_CSV = "_success.csv";
    public static final String FAILURE_CSV = "_failure.csv";

    private String[] headers;

    private CsvWriter successCsv;
    private CsvWriter failureCsv;

    private FileWriter successFileWriter;
    private FileWriter failureFileWriter;

    /**
     * Error/Success CSV files are placed in a results folder in the current working directory. They are named
     * based on the original filename used. Given /path/to/MyCandidates.csv, this class will set up log files in
     * the current working directory (may not be the /path/to/ directory).
     * <p>
     * Output Files:
     * - results/MyCandidates_yyyy-mm-dd_HH.MM.SS_failure.csv
     * - results/MyCandidates_yyyy-mm-dd_HH.MM.SS_success.csv
     *
     * @param method
     * @param filePath The full path to the Entity file to read in
     * @param headers The headers read in from the input CSV file
     */
    public CsvFileWriter(Method method, String filePath, String[] headers) throws IOException {
        this.headers = headers;

        String baseName = FilenameUtils.getBaseName(filePath);

        // Create files, and create directory if it does not exist
        File successFile = new File(RESULTS_DIR + baseName + "_" + method.getMethodName() + "_" + StringConsts.TIMESTAMP + SUCCESS_CSV);
        File failureFile = new File(RESULTS_DIR + baseName + "_" + method.getMethodName() + "_" + StringConsts.TIMESTAMP + FAILURE_CSV);
        successFile.getParentFile().mkdirs();
        failureFile.getParentFile().mkdirs();

        // Configure writers
        successFileWriter = new FileWriter(successFile);
        failureFileWriter = new FileWriter(failureFile);
        successCsv = new CsvWriter(successFileWriter, ',');
        failureCsv = new CsvWriter(failureFileWriter, ',');

        // Write headers to the files, adding our own custom columns, if they do not already exist.
        createCSVHeaders(headers, method);

        successCsv.flush();
        failureCsv.flush();
    }

    protected void createCSVHeaders(String[] headers, Method method) throws IOException {
            if (method.equals(Method.LOADATTACHMENTS)) {
                headers = ArrayUtil.append(TaskConsts.parentEntityID, headers);
            }
            successCsv.writeRecord(ArrayUtil.prepend(BULLHORN_ID_COLUMN,
                    ArrayUtil.prepend(ACTION_COLUMN, headers)));
            failureCsv.writeRecord(ArrayUtil.prepend(REASON_COLUMN, headers));
    }

    /**
     * Given the input for a row record and the output from REST, this method will output the results of the operation
     * to the results files.
     *
     * @param data The original CSV record
     * @param result The resulting status from REST
     * @throws IOException
     */
    public synchronized void writeRow(String[] data, Result result) throws IOException {
        if (result.isSuccess()) {
            successCsv.writeRecord(ArrayUtil.prepend(result.getBullhornId().toString(),
                    ArrayUtil.prepend(result.getAction().toString(), data)));
            successCsv.flush();
        } else {
            failureCsv.writeRecord(ArrayUtil.prepend(result.getFailureText(), data));
            failureCsv.flush();
        }
    }
}

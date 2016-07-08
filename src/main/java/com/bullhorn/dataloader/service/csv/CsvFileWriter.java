package com.bullhorn.dataloader.service.csv;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.commons.io.FilenameUtils;

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
     * @param filePath The full path to the Entity file to read in
     * @param headers The headers read in from the input CSV file
     */
    public CsvFileWriter(String filePath, String[] headers) throws IOException {
        this.headers = headers;

        String baseName = FilenameUtils.getBaseName(filePath);

        // Create files, and create directory if it does not exist
        File successFile = new File(RESULTS_DIR + baseName + "_" + StringConsts.getTimestamp() + SUCCESS_CSV);
        File failureFile = new File(RESULTS_DIR + baseName + "_" + StringConsts.getTimestamp() + FAILURE_CSV);
        successFile.getParentFile().mkdirs();
        failureFile.getParentFile().mkdirs();

        // Configure writers
        successFileWriter = new FileWriter(successFile);
        failureFileWriter = new FileWriter(failureFile);
        successCsv = new CsvWriter(successFileWriter, ',');
        failureCsv = new CsvWriter(failureFileWriter, ',');

        // Write headers to the files, adding our own custom columns, if they do not already exist.
        successCsv.writeRecord(ArrayUtil.prepend(BULLHORN_ID_COLUMN,
                ArrayUtil.prepend(ACTION_COLUMN, headers)));
        failureCsv.writeRecord(ArrayUtil.prepend(REASON_COLUMN, headers));
        successCsv.flush();
        failureCsv.flush();
    }

    /**
     * Given the input for a row record and the output from REST, this method will output the results of the operation
     * to the results files.
     *
     * @param jsonRow The original CSV record
     * @param result The resulting status from REST
     * @throws IOException
     */
    public synchronized void writeRow(JsonRow jsonRow, Result result) throws IOException {
        if (result.isSuccess()) {
            successCsv.writeRecord(ArrayUtil.prepend(result.getBullhornId().toString(),
                    ArrayUtil.prepend(result.getAction().toString(), jsonRow.getValues())));
            successCsv.flush();
        } else {
            failureCsv.writeRecord(ArrayUtil.prepend(result.getFailureText(), jsonRow.getValues()));
            failureCsv.flush();
        }
    }
}

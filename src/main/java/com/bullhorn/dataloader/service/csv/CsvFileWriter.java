package com.bullhorn.dataloader.service.csv;

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

    private String[] headers;

    private CsvWriter successCsv;
    private CsvWriter failureCsv;

    private FileWriter successFile;
    private FileWriter failureFile;

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

        // Create files
        String baseName = FilenameUtils.getBaseName(filePath);
        successFile = new FileWriter(StringConsts.RESULTS_DIR + baseName + "_" + StringConsts.getTimestamp() + StringConsts.SUCCESS_CSV);
        failureFile = new FileWriter(StringConsts.RESULTS_DIR + baseName + "_" + StringConsts.getTimestamp() + StringConsts.FAILURE_CSV);

        // Configure writers
        successCsv = new CsvWriter(successFile, ',');
        failureCsv = new CsvWriter(failureFile, ',');

        // Write headers to the files, adding our own custom columns, if they do not already exist.
        successCsv.writeRecord(ArrayUtil.prepend(StringConsts.BULLHORN_ID_COLUMN,
                ArrayUtil.prepend(StringConsts.ACTION_COLUMN, headers)));
        failureCsv.writeRecord(ArrayUtil.prepend(StringConsts.REASON_COLUMN, headers));

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

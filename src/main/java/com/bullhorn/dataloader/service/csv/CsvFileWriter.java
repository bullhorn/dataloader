package com.bullhorn.dataloader.service.csv;

import com.bullhorn.dataloader.consts.TaskConsts;
import com.bullhorn.dataloader.service.Command;
import com.bullhorn.dataloader.util.ArrayUtil;
import com.bullhorn.dataloader.util.StringConsts;
import com.csvreader.CsvWriter;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * A thread-safe file writer for outputting results into both a success and a failure CSV file.
 */
public class CsvFileWriter {

    public static final String RESULTS_DIR = "results/";
    public static final String BULLHORN_ID_COLUMN = "id";
    public static final String ACTION_COLUMN = "dataloader_action";
    public static final String REASON_COLUMN = "failure_reason";
    public static final String SUCCESS_CSV = "_success.csv";
    public static final String FAILURE_CSV = "_failure.csv";

    final private Command command;
    final private String[] headers;
    final private String successFilePath;
    final private String failureFilePath;

    private CsvWriter successCsv = null;
    private CsvWriter failureCsv = null;

    /**
     * Error/Success CSV files are placed in a results folder in the current working directory. They are named
     * based on the original filename used. Given /path/to/MyCandidates.csv, this class will set up log files in
     * the current working directory (may not be the /path/to/ directory).
     * <p>
     * Output Files:
     * - results/MyCandidates_yyyy-mm-dd_HH.MM.SS_failure.csv
     * - results/MyCandidates_yyyy-mm-dd_HH.MM.SS_success.csv
     *
     * @param command  The Command object to execute during this run
     * @param filePath The full path to the Entity file to read in
     * @param headers  The headers read in from the input CSV file
     */
    public CsvFileWriter(Command command, String filePath, String[] headers) throws IOException {
        this.command = command;
        this.headers = headers;

        String baseName = FilenameUtils.getBaseName(filePath);
        successFilePath = RESULTS_DIR + baseName + "_" + command.getMethodName() + "_" + StringConsts.TIMESTAMP + SUCCESS_CSV;
        failureFilePath = RESULTS_DIR + baseName + "_" + command.getMethodName() + "_" + StringConsts.TIMESTAMP + FAILURE_CSV;

        File resultsDir = new File(RESULTS_DIR);
        resultsDir.mkdirs();
    }

    /**
     * Given the input for a row record and the output from REST, this method will output the results of the operation
     * to the results files.
     *
     * @param data   The original CSV record
     * @param result The resulting status from REST
     * @throws IOException
     */
    public synchronized void writeRow(String[] data, Result result) throws IOException {
        if (result.isSuccess()) {
            CsvWriter csvWriter = getOrCreateSuccessCsvWriter();
            if (result.getBullhornId() > -1) {
                csvWriter.writeRecord(ArrayUtil.prepend(result.getBullhornId().toString(),
                    ArrayUtil.prepend(result.getAction().toString(), data)));
            } else {
                csvWriter.writeRecord(ArrayUtil.prepend(result.getAction().toString(), data));
            }
            csvWriter.flush();
        } else {
            CsvWriter csvWriter = getOrCreateFailureCsvWriter();
            csvWriter.writeRecord(ArrayUtil.prepend(result.getFailureText(), data));
            csvWriter.flush();
        }
    }

    private CsvWriter getOrCreateSuccessCsvWriter() throws IOException {
        if (successCsv == null) {
            FileWriter fileWriter = new FileWriter(successFilePath);
            successCsv = new CsvWriter(fileWriter, ',');

            if (command.equals(Command.LOAD_ATTACHMENTS)) {
                String[] successHeaders = ArrayUtil.append(headers, TaskConsts.PARENT_ENTITY_ID);
                successCsv.writeRecord(ArrayUtil.prepend(BULLHORN_ID_COLUMN, ArrayUtil.prepend(ACTION_COLUMN, successHeaders)));
            } else if (command.equals(Command.CONVERT_ATTACHMENTS)) {
                successCsv.writeRecord(ArrayUtil.prepend(ACTION_COLUMN, headers));
            } else {
                successCsv.writeRecord(ArrayUtil.prepend(BULLHORN_ID_COLUMN, ArrayUtil.prepend(ACTION_COLUMN, headers)));
            }
        }

        return successCsv;
    }

    private CsvWriter getOrCreateFailureCsvWriter() throws IOException {if (failureCsv == null) {
            FileWriter fileWriter = new FileWriter(failureFilePath);
            failureCsv = new CsvWriter(fileWriter, ',');
            failureCsv.writeRecord(ArrayUtil.prepend(REASON_COLUMN, headers));
        }

        return failureCsv;
    }
}

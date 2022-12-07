package com.bullhorn.dataloader.data;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FilenameUtils;

import com.bullhorn.dataloader.enums.Command;
import com.bullhorn.dataloader.util.StringConsts;
import com.csvreader.CsvWriter;

/**
 * A thread-safe file writer for outputting results into both a success and a failure CSV file.
 */
public class CsvFileWriter {

    public static final String RESULTS_DIR = "results/";
    private static final String ACTION_COLUMN = "dataloader_action";
    private static final String ERROR_CODE_COLUMN = "error_code";
    private static final String ERROR_COLUMN = "error";
    private static final String ERROR_DETAILS_COLUMN = "error_details";
    private static final String TIPS_TO_RESOLVE_COLUMN = "tips_to_resolve";
    private static final String SUCCESS_CSV = "_success.csv";
    private static final String FAILURE_CSV = "_failure.csv";
    public static String successFilePath;
    public static String failureFilePath;

    private final Command command;
    private final String[] headers;

    private CsvWriter successCsv = null;
    private CsvWriter failureCsv = null;

    /**
     * Returns the correctly formatted filePath for the results file
     *
     * @param inputFilePath The path of the input CSV to read
     * @param command       The command used to process the CSV file
     * @param status        Success or Failure
     * @return The path to the results file
     */
    public static String getResultsFilePath(String inputFilePath, Command command, Result.Status status) {
        String baseName = FilenameUtils.getBaseName(inputFilePath);
        String ending = (status == Result.Status.SUCCESS) ? SUCCESS_CSV : FAILURE_CSV;
        return RESULTS_DIR + baseName + "_" + command.getMethodName() + "_" + StringConsts.TIMESTAMP + ending;
    }

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
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public CsvFileWriter(Command command, String filePath, String[] headers) {
        this.command = command;
        this.headers = headers;

        successFilePath = getResultsFilePath(filePath, command, Result.Status.SUCCESS);
        failureFilePath = getResultsFilePath(filePath, command, Result.Status.FAILURE);

        File resultsDir = new File(RESULTS_DIR);
        resultsDir.mkdirs();
    }

    /**
     * Given the input for a row record and the output from REST, this method will output the results of the operation
     * to the results files. Prepends columns to the result files: id,action for success, failureText for failure.
     *
     * @param row    the original CSV record
     * @param result the resulting status from REST
     * @throws IOException when writing to disk
     */
    public synchronized void writeRow(Row row, Result result) throws IOException {
        CsvWriter csvWriter;
        List<String> values = row.getValues();

        if (result.isSuccess()) {
            csvWriter = getOrCreateSuccessCsvWriter();
            if (command.equals(Command.LOAD)) {
                values.add(0, result.getAction().toString());
                values.add(0, result.getBullhornId().toString());
            } else if (command.equals(Command.LOAD_ATTACHMENTS)) {
                values.add(0, result.getAction().toString());
                values.add(0, result.getBullhornParentId().toString());
                values.add(0, result.getBullhornId().toString());
            } else if (command.equals(Command.CONVERT_ATTACHMENTS)) {
                values.add(0, result.getAction().toString());
            }
        } else {
            csvWriter = getOrCreateFailureCsvWriter();
            values.add(0, result.getErrorInfo().getTipsToResolve());
            values.add(0, result.getErrorDetails());
            values.add(0, result.getErrorInfo().getTitle());
            values.add(0, result.getErrorInfo().getCode().toString());
            if (command.equals(Command.LOAD) || command.equals(Command.LOAD_ATTACHMENTS)) {
                values.add(0, result.getBullhornId().toString());
            }
        }

        csvWriter.writeRecord(values.toArray(new String[0]));
        csvWriter.flush();
    }

    private CsvWriter getOrCreateSuccessCsvWriter() throws IOException {
        if (successCsv == null) {
            FileWriter fileWriter = new FileWriter(successFilePath);
            successCsv = new CsvWriter(fileWriter, ',');

            List<String> headerList = new ArrayList<>(Arrays.asList(headers));
            if (command.equals(Command.LOAD)) {
                headerList.add(0, ACTION_COLUMN);
                headerList.add(0, StringConsts.ID);
            } else if (command.equals(Command.LOAD_ATTACHMENTS)) {
                headerList.add(0, ACTION_COLUMN);
                headerList.add(0, StringConsts.PARENT_ENTITY_ID);
                headerList.add(0, StringConsts.ID);
            } else if (command.equals(Command.CONVERT_ATTACHMENTS)) {
                headerList.add(0, ACTION_COLUMN);
            }
            successCsv.writeRecord(headerList.toArray(new String[0]));
        }
        return successCsv;
    }

    private CsvWriter getOrCreateFailureCsvWriter() throws IOException {
        if (failureCsv == null) {
            FileWriter fileWriter = new FileWriter(failureFilePath);
            failureCsv = new CsvWriter(fileWriter, ',');

            List<String> headerList = new ArrayList<>(Arrays.asList(headers));
            headerList.add(0, TIPS_TO_RESOLVE_COLUMN);
            headerList.add(0, ERROR_DETAILS_COLUMN);
            headerList.add(0, ERROR_COLUMN);
            headerList.add(0, ERROR_CODE_COLUMN);
            if (command.equals(Command.LOAD) || command.equals(Command.LOAD_ATTACHMENTS)) {
                headerList.add(0, StringConsts.ID);
            }
            failureCsv.writeRecord(headerList.toArray(new String[0]));
        }
        return failureCsv;
    }
}

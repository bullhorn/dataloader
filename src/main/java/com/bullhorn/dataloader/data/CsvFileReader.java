package com.bullhorn.dataloader.data;

import com.csvreader.CsvReader;
import com.google.common.collect.Sets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * An extension to the CsvFileReader that adds our extra functionality, like duplicate checking and returning the CSV
 * file data as a list of key-value pairs, where each row is represented by a map of header names to values.
 */
public class CsvFileReader extends CsvReader {

    private Integer rowNumber = -1; // Row 0 is the header row

    /**
     * Constructor which also reads headers and ensures there are no duplicates
     *
     * @param filePath the path to the CSV file
     */
    public CsvFileReader(String filePath) throws IOException {
        super(filePath);
        // Turn the SafetySwitch off because it limits the maximum length of any column to 100,000 characters
        setSafetySwitch(false);
        readHeaders();
        checkForDuplicateHeaders();
    }

    @Override
    public boolean readRecord() throws IOException {
        rowNumber++;
        return super.readRecord();
    }

    /**
     * Ensures that user's don't accidentally have duplicate columns
     *
     * @throws IOException if there are duplicates
     */
    private void checkForDuplicateHeaders() throws IOException {
        List<String> headers = Arrays.asList(getHeaders());
        Set<String> uniqueHeaders = Sets.newHashSet(getHeaders());

        if (headers.size() != uniqueHeaders.size()) {
            throw new IllegalStateException("Provided CSV file contains the following duplicate headers:\n"
                + printDuplicateHeaders());
        }
    }

    private String printDuplicateHeaders() throws IOException {
        String duplicateString = "";
        List<String> uniqueHeaders = new ArrayList<>();

        for (String header : getHeaders()) {
            if (uniqueHeaders.contains(header)) {
                duplicateString = duplicateString.concat("\t" + header + "\n");
            } else {
                uniqueHeaders.add(header);
            }
        }

        return duplicateString;
    }

    /**
     * Returns the data for the current row in the format of a row object.
     *
     * First, call csvFileReader.readRecord() to read the next row, and then call this method instead of getValues() to
     * return the Row object instead of the raw string array of values.
     *
     * @return the row of data from the spreadsheet
     * @throws IOException in case the column/row counts don't match
     */
    public Row getRow() throws IOException {
        if (getHeaderCount() != getValues().length) {
            throw new IOException("Header column count " + getHeaderCount()
                + " is not equal to row column count " + getValues().length);
        }

        Row row = new Row(rowNumber);
        for (int i = 0; i < getHeaderCount(); i++) {
            Cell cell = new Cell(getHeader(i), getValues()[i]);
            row.addCell(cell);
        }
        return row;
    }
}

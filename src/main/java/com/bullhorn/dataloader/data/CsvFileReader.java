package com.bullhorn.dataloader.data;

import com.csvreader.CsvReader;
import com.google.common.collect.Sets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * An extension to the CsvFileReader that adds our extra functionality, like duplicate checking and returning the CSV file
 * data as a list of key-value pairs, where each row is represented by a map of header names to values.
 */
public class CsvFileReader extends CsvReader {

    /**
     * Constructor which also reads headers and ensures there are no duplicates
     *
     * @param filePath The path to the CSV file
     */
    public CsvFileReader(String filePath) throws IOException {
        super(filePath);
        readHeaders();
        checkForDuplicateHeaders();
    }

    /**
     * Ensures that user's don't accidentally have duplicate columns
     *
     * @throws IOException If there are duplicates
     */
    private void checkForDuplicateHeaders() throws IOException {
        List<String> headers = Arrays.asList(getHeaders());
        Set<String> uniqueHeaders = Sets.newHashSet(getHeaders());

        if (headers.size() != uniqueHeaders.size()) {
            throw new IllegalStateException("Provided CSV file contains the following duplicate headers:\n" + printDuplicateHeaders());
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
     * Returns the data for the current row in the format of a data map, mapping from column name to cell value.
     *
     * First, call csvFileReader.readRecord() to read the next row, and then call this method instead of getValues() to
     * return the data map instead of the string array of values.
     *
     * @return The data map that is used to load the record values
     * @throws IOException In case the column/row counts don't match
     */
    public Map<String, String> getRecordDataMap() throws IOException {
        // TODO: Build up a Row object which has a rowNumber and cells, and return it as the data object
        if (getHeaderCount() != getValues().length) {
            throw new IOException("Header column count " + getHeaderCount() + " is not equal to row column count " + getValues().length);
        }

        Map<String, String> dataMap = new LinkedHashMap<>();
        for (int i = 0; i < getHeaderCount(); i++) {
            dataMap.put(getHeader(i), getValues()[i]);
        }

        return dataMap;
    }
}

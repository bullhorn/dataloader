package com.bullhorn.dataloader.data;

import com.bullhorn.dataloader.util.PrintUtil;
import com.bullhorn.dataloader.util.PropertyFileUtil;
import com.csvreader.CsvReader;
import com.google.common.collect.Sets;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * An extension to the CsvFileReader that adds our extra functionality, like duplicate checking and returning the CSV
 * file data as a list of key-value pairs, where each row is represented by a map of header names to values.
 */
public class CsvFileReader extends CsvReader {

    private final PropertyFileUtil propertyFileUtil;
    private final PrintUtil printUtil;

    private Integer rowNumber = -1; // Row 0 is the header row
    private List<String> mappedHeaders = new ArrayList<>(); // Headers after column name mapping

    /**
     * Constructor which also reads headers and ensures there are no duplicates
     *
     * @param filePath the path to the CSV file
     */
    public CsvFileReader(String filePath, PropertyFileUtil propertyFileUtil, PrintUtil printUtil) throws IOException {
        super(filePath, ',', propertyFileUtil.getSingleByteEncoding() ? Charset.forName("ISO-8859-1") : Charset.forName("UTF-8"));
        this.propertyFileUtil = propertyFileUtil;
        this.printUtil = printUtil;

        // Turn the SafetySwitch off because it limits the maximum length of any column to 100,000 characters
        setSafetySwitch(false);
        readHeaders();
        mapHeaders();
        checkForDuplicateHeaders();
    }

    @Override
    public boolean readRecord() throws IOException {
        rowNumber++;
        return super.readRecord();
    }

    public String[] getMappedHeaders() throws IOException {
        return mappedHeaders.toArray(new String[0]);
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
            Cell cell = new Cell(mappedHeaders.get(i), getValues()[i]);
            row.addCell(cell);
        }
        return row;
    }

    /**
     * Creates a set of mapped headers (changing the name of a column) based on column mappings setup in user properties
     */
    private void mapHeaders() throws IOException {
        for (String header : getHeaders()) {
            if (propertyFileUtil.hasColumnNameMapping(header)) {
                String mappedHeader = propertyFileUtil.getColumnNameMapping(header);
                printUtil.printAndLog("Mapping column: '" + header + "' to: '" + mappedHeader + "'");
                mappedHeaders.add(mappedHeader);
            } else {
                mappedHeaders.add(header);
            }
        }
    }

    /**
     * Ensures that user's don't accidentally have duplicate columns
     *
     * @throws IOException if there are duplicates
     */
    private void checkForDuplicateHeaders() throws IOException {
        Set<String> uniqueHeaders = Sets.newHashSet(mappedHeaders);
        if (mappedHeaders.size() != uniqueHeaders.size()) {
            throw new IllegalStateException("Provided CSV file contains the following duplicate headers:\n" + printDuplicateHeaders());
        }
    }

    private String printDuplicateHeaders() throws IOException {
        String duplicateString = "";
        List<String> uniqueHeaders = new ArrayList<>();

        for (String header : mappedHeaders) {
            if (uniqueHeaders.contains(header)) {
                duplicateString = duplicateString.concat("\t" + header + "\n");
            } else {
                uniqueHeaders.add(header);
            }
        }

        return duplicateString;
    }
}

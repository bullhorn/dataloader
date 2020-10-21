package com.bullhorn.dataloader.data;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.io.input.BOMInputStream;

import com.bullhorn.dataloader.util.ArrayUtil;
import com.bullhorn.dataloader.util.PrintUtil;
import com.bullhorn.dataloader.util.PropertyFileUtil;
import com.csvreader.CsvReader;
import com.google.common.collect.Sets;

/**
 * An extension to the CsvFileReader that adds our extra functionality, like duplicate checking and returning the CSV
 * file data as a list of key-value pairs, where each row is represented by a map of header names to values.
 */
public class CsvFileReader extends CsvReader {

    private final String filePath;
    private final PropertyFileUtil propertyFileUtil;
    private final PrintUtil printUtil;

    private Integer rowNumber = -1; // Row 0 is the header row
    private final List<String> mappedHeaders = new ArrayList<>(); // Headers after column name mapping

    /**
     * Constructor which also reads headers and ensures there are no duplicates
     *
     * @param filePath the path to the CSV file
     */
    public CsvFileReader(String filePath, PropertyFileUtil propertyFileUtil, PrintUtil printUtil) throws IOException {
        super(new BOMInputStream(new FileInputStream(filePath)), ',',
            propertyFileUtil.getSingleByteEncoding() ? StandardCharsets.ISO_8859_1 : StandardCharsets.UTF_8);
        this.filePath = filePath;
        this.propertyFileUtil = propertyFileUtil;
        this.printUtil = printUtil;

        // Turn the SafetySwitch off because it limits the maximum length of any column to 100,000 characters
        setSafetySwitch(false);
        readHeaders();
        mapHeaders();
        checkForDuplicateHeaders();
    }

    @Override
    public String[] getHeaders() {
        return mappedHeaders.toArray(new String[0]);
    }

    @Override
    public boolean readRecord() throws IOException {
        rowNumber++;
        return super.readRecord();
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
            throw new IOException("Row " + rowNumber + ": Header column count " + getHeaderCount()
                + " is not equal to row column count " + getValues().length);
        }

        Row row = new Row(filePath, rowNumber);
        for (int i = 0; i < getHeaderCount(); ++i) {
            String header = getHeader(i);
            if (propertyFileUtil.hasColumnNameMapping(header)) {
                String mappedHeader = propertyFileUtil.getColumnNameMapping(header);
                if (!mappedHeader.isEmpty()) {
                    Cell cell = new Cell(mappedHeader, getValues()[i]);
                    row.addCell(cell);
                }
            } else {
                Cell cell = new Cell(header, getValues()[i]);
                row.addCell(cell);
            }
        }
        return row;
    }

    /**
     * Creates a set of mapped headers (changing the name of a column) based on column mappings setup in user properties
     */
    private void mapHeaders() throws IOException {
        for (String header : super.getHeaders()) {
            if (propertyFileUtil.hasColumnNameMapping(header)) {
                String mappedHeader = propertyFileUtil.getColumnNameMapping(header);
                if (mappedHeader.isEmpty()) {
                    printUtil.printAndLog("Skipping column: '" + header + "' because it is mapped to an empty value");
                } else {
                    printUtil.printAndLog("Mapping column: '" + header + "' to: '" + mappedHeader + "'");
                    mappedHeaders.add(mappedHeader);
                }
            } else {
                mappedHeaders.add(header);
            }
        }
    }

    /**
     * Ensures that user's don't accidentally have duplicate columns
     */
    private void checkForDuplicateHeaders() {
        Set<String> uniqueHeaders = Sets.newHashSet(mappedHeaders);
        if (mappedHeaders.size() != uniqueHeaders.size()) {
            throw new IllegalStateException("Provided CSV file contains the following duplicate headers:\n"
                + ArrayUtil.getDuplicates(mappedHeaders).stream().map(s -> "\t" + s + "\n").collect(Collectors.joining()));
        }
    }
}

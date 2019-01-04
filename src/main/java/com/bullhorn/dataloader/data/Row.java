package com.bullhorn.dataloader.data;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents a row of data in the spreadsheet - a collection of fields to insert/update for an entity.
 */
public class Row {

    private final String filePath;
    private final Integer number;
    private final List<Cell> cells;

    /**
     * Constructor which takes the row number in the spreadsheet
     *
     * @param number this value is used for outputting messages to the user
     */
    public Row(String filePath, Integer number) {
        this.filePath = filePath;
        this.number = number;
        this.cells = new ArrayList<>();
    }

    public String getFilePath() {
        return filePath;
    }

    public Integer getNumber() {
        return number;
    }

    public List<Cell> getCells() {
        return cells;
    }

    public void addCell(Cell cell) {
        this.cells.add(cell);
    }

    /**
     * Returns true if a cell with the given name exists
     *
     * @param name the case-insensitive name of a cell header
     * @return true if the cell exists, false otherwise
     */
    public Boolean hasValue(String name) {
        return getValue(name) != null;
    }

    /**
     * Returns the value of a single cell using the cell's field name in the header (case insensitive)
     *
     * @param name case insensitive name of the field
     * @return text in the cell, null if the cell with the given name does not exist
     */
    public String getValue(String name) {
        return cells.stream().filter(cell -> cell.getName().equalsIgnoreCase(name))
            .findFirst().map(Cell::getValue).orElse(null);
    }

    /**
     * Gets just the string headers (the names of the cells) for the row.
     *
     * @return the ordered list of headers, as they appear in the spreadsheet
     */
    public List<String> getNames() {
        return cells.stream().map(Cell::getName).collect(Collectors.toList());
    }

    /**
     * Gets just the string values in the cells.
     *
     * @return the ordered list of values, as they appear in the spreadsheet
     */
    public List<String> getValues() {
        return cells.stream().map(Cell::getValue).collect(Collectors.toList());
    }
}

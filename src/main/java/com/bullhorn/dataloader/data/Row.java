package com.bullhorn.dataloader.data;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a row of data in the spreadsheet - a collection of fields to insert/update for an entity.
 */
public class Row {

    final private Integer number;
    private List<Cell> cells;

    /**
     * Constructor which takes the row number in the spreadsheet
     *
     * @param number this value is used for outputting messages to the user
     */
    public Row(Integer number) {
        this.number = number;
        this.cells = new ArrayList<>();
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
        String value = null;
        for (Cell cell : cells) {
            if (cell.getName().equalsIgnoreCase(name)) {
                value = cell.getValue();
            }
        }
        return value;
    }

    /**
     * Gets just the string headers for the row
     *
     * @return the ordered list of headers, as they appear in the spreadsheet
     */
    public List<String> getHeaders() {
        List<String> headers = new ArrayList<>();
        for (Cell cell : cells) {
            headers.add(cell.getName());
        }
        return headers;
    }

    /**
     * Gets just the string values in the cells
     *
     * @return the ordered list of values, as they appear in the spreadsheet
     */
    public List<String> getValues() {
        List<String> values = new ArrayList<>();
        for (Cell cell : cells) {
            values.add(cell.getValue());
        }
        return values;
    }

    /**
     * TODO: Remove Me
     * Temporary old style data array that is being replaced piecemeal
     */
    public Map<String, String> getDataMap() {
        Map<String, String> dataMap = new LinkedHashMap<>();
        for (Cell cell : cells) {
            dataMap.put(cell.getName(), cell.getValue());
        }
        return dataMap;
    }
}

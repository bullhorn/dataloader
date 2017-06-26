package com.bullhorn.dataloader.data;

/**
 * Represents an individual cell of data in a spreadsheet.
 *
 * Contains the contents of an individual cell and the column header for this cell.
 */
public class Cell {

    private String name;
    private String value;

    /**
     * Constructor which takes the name and value of the cell in an input spreadsheet
     *
     * @param name the string value in the header row for this column
     * @param value the string value of the cell in the spreadsheet
     */
    public Cell(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    /**
     * Returns true if the cell is any type of address field
     *
     * @return True if the cell is a field in the compound to-one address field (ex: address.zip)
     */
    public Boolean isAddress() {
        return isAssociation() && getAssociationName().toLowerCase().contains("address");
    }

    /**
     * If the field is of the format: 'entity.field', then it is an association
     *
     * @return True if the cell is an association to another entity (contains a dot)
     */
    public Boolean isAssociation() {
        return name.contains(".");
    }

    /**
     * Returns the name of the reference field, such as candidate in 'candidate.externalID'
     *
     * @return Null if the cell is not an association to another entity (no dot)
     */
    public String getAssociationName() {
        String associationName = null;
        Integer dotIndex = name.indexOf(".");
        if (dotIndex != -1) {
            associationName = name.substring(0, dotIndex);
        }
        return associationName;
    }

    /**
     * Returns the name of the reference field, such as externalID in 'candidate.externalID'
     *
     * @return Null if the cell is not an association to another entity (no dot)
     */
    public String getAssociationField() {
        String associationField = null;
        Integer dotIndex = name.indexOf(".");
        if (dotIndex != -1) {
            associationField = name.substring(dotIndex + 1, name.length());
        }
        return associationField;
    }
}

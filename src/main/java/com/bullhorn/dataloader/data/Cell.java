package com.bullhorn.dataloader.data;

/**
 * Represents the raw data from an individual cell of data in a spreadsheet.
 *
 * Contains the contents of an individual cell (value) and the column header (name) for this cell.
 */
public class Cell {

    private final String name;
    private final String value;

    /**
     * Constructor which takes the name and value of the cell in an input spreadsheet.
     *
     * @param name  the string value in the header row for this column
     * @param value the string value of the cell in the spreadsheet
     */
    public Cell(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    /**
     * If the cell is an association, but is blank, then don't try to associate to nothing, instead ignore the cell.
     *
     * This handles the case where there are associated columns in a CSV file that are not filled out for every row.
     * Does not apply to direct fields or compound fields (address is the only compound field handled).
     * See issue: https://github.com/bullhorn/dataloader/issues/186
     *
     * @return True if the column should be processed by DataLoader, false otherwise.
     */
    public Boolean isValid() {
        return isAddress() || !isAssociation() || !value.isEmpty();
    }

    /**
     * Returns true if the cell is any type of address field.
     *
     * @return true if the cell is a field in the compound to-one address field (ex: address.zip)
     */
    public Boolean isAddress() {
        return isAssociation() && getAssociationBaseName().toLowerCase().contains("address");
    }

    /**
     * If the field is of the format: 'entity.field', then it is an association.
     *
     * @return true if the cell is an association to another entity (contains a dot)
     */
    public Boolean isAssociation() {
        return name.contains(".");
    }

    /**
     * Returns the name of this entity's field, such as 'candidate' in 'candidate.externalID'.
     *
     * @return null if the cell is not an association to another entity (no dot)
     */
    public String getAssociationBaseName() {
        String associationName = null;
        Integer dotIndex = name.indexOf(".");
        if (dotIndex != -1) {
            associationName = name.substring(0, dotIndex);
        }
        return associationName;
    }

    /**
     * Returns the name of the reference entity field, such as 'externalID' in 'candidate.externalID'.
     *
     * @return null if the cell is not an association to another entity (no dot)
     */
    public String getAssociationFieldName() {
        String associationField = null;
        Integer dotIndex = name.indexOf(".");
        if (dotIndex != -1) {
            associationField = name.substring(dotIndex + 1, name.length());
        }
        return associationField;
    }
}

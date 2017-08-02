package com.bullhorn.dataloader.data;

/**
 * Represents the raw data from an individual cell of data in a spreadsheet.
 *
 * Contains the contents of an individual cell (value) and the column header (name) for this cell.
 * TODO: Have cell own the splitting up multiple values separated by a token into an array of values.
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
     * Returns true if the cell is any type of address field.
     *
     * @return True if the cell is a field in the compound to-one address field (ex: address.zip)
     */
    public Boolean isAddress() {
        return isAssociation() && getAssociationBaseName().toLowerCase().contains("address");
    }

    /**
     * If the field is of the format: 'entity.field', then it is an association.
     *
     * @return True if the cell is an association to another entity (contains a dot)
     */
    public Boolean isAssociation() {
        return name.contains(".");
    }

    /**
     * Returns the name of this entity's field, such as 'candidate' in 'candidate.externalID'.
     *
     * @return Null if the cell is not an association to another entity (no dot)
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
     * @return Null if the cell is not an association to another entity (no dot)
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

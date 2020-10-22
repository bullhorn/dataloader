package com.bullhorn.dataloader.rest;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.bullhorn.dataloader.data.Cell;
import com.bullhorn.dataloader.data.Row;
import com.bullhorn.dataloader.util.PrintUtil;
import com.bullhorn.dataloader.util.StringConsts;
import com.bullhornsdk.data.model.entity.core.standard.Country;
import com.bullhornsdk.data.model.parameter.standard.ParamFactory;
import com.google.common.collect.Sets;

/**
 * Pre-loads data into memory prior to performing a process in order to avoid the cost of lookup calls.
 */
public class Preloader {

    private final RestSession restSession;
    private Map<String, Integer> countryNameToIdMap = null;
    private final PrintUtil printUtil;
    private Boolean nameFieldNotificationLogged = false;

    public Preloader(RestSession restSession, PrintUtil printUtil) {
        this.restSession = restSession;
        this.printUtil = printUtil;
    }

    /**
     * Called upon dataloader initialization (before tasks begin executing) in order to load any lookup data required
     * for entity to load.
     *
     * @param row the user provided row of data
     * @return the row that has potentially been modified to use internal bullhorn IDs
     */
    public Row convertRow(Row row) {
        Row convertedRow = new Row(row.getFilePath(), row.getNumber());
        for (Cell cell : row.getCells()) {
            convertedRow.addCell(convertCell(cell));
        }
        checkAndAddNameCell(convertedRow);
        return convertedRow;
    }

    /**
     * Potentially converts a given cell from lookup fields to internal bullhorn ID fields.
     *
     * @param cell the user provided cell
     * @return the cell that has potentially been modified to be an internal bullhorn ID
     */
    private Cell convertCell(Cell cell) {
        if (cell.isAddress() && cell.getAssociationFieldName().equalsIgnoreCase(StringConsts.COUNTRY_NAME)) {
            String name = cell.getAssociationBaseName() + "." + StringConsts.COUNTRY_ID;
            String value = cell.getValue();
            if (getCountryNameToIdMap().containsKey(value.toLowerCase())) {
                value = getCountryNameToIdMap().get(value.toLowerCase()).toString();
            }
            return new Cell(name, value);
        }
        return cell;
    }

    /**
     * Since the REST API only allows us to set the country using `countryID`, we query for all countries by name to
     * allow the `countryName` to upload by name instead of just the internal Bullhorn country code.
     *
     * Makes rest calls and stores the private data the first time through
     */
    private Map<String, Integer> getCountryNameToIdMap() {
        if (countryNameToIdMap == null) {
            countryNameToIdMap = createCountryNameToIdMap();
        }
        return countryNameToIdMap;
    }

    /**
     * Makes the REST API calls for obtaining all countries in the country table
     *
     * @return A map of name to internal country ID (bullhorn specific id)
     */
    private Map<String, Integer> createCountryNameToIdMap() {
        RestApi restApi = restSession.getRestApi();
        Map<String, Integer> countryNameToIdMap = new HashMap<>();
        List<Country> countryList = restApi.queryForList(Country.class, "id IS NOT null",
            Sets.newHashSet("id", "name"), ParamFactory.queryParams());
        countryList.forEach(n -> countryNameToIdMap.put(n.getName().trim().toLowerCase(), n.getId()));
        return countryNameToIdMap;
    }

    /**
     * Potentially add a new name cell to the row, if there is a firstName and lastName but no name.
     *
     * @param row the user provided row of data that might be modified
     */
    private void checkAndAddNameCell(Row row) {
        if (row.hasValue(StringConsts.FIRST_NAME) && row.hasValue(StringConsts.LAST_NAME) && !row.hasValue(StringConsts.NAME)) {
            row.addCell(new Cell(StringConsts.NAME, row.getValue(StringConsts.FIRST_NAME) + ' ' + row.getValue(StringConsts.LAST_NAME)));

            if (!nameFieldNotificationLogged) {
                printUtil.printAndLog("Added " + StringConsts.NAME
                    + " field as " + "'<" + StringConsts.FIRST_NAME + "> <" + StringConsts.LAST_NAME + ">'"
                    + " since both " + StringConsts.FIRST_NAME + " and " + StringConsts.LAST_NAME + " were provided but "
                    + StringConsts.NAME + " was not.");
                nameFieldNotificationLogged = true;
            }
        }
    }
}

package com.bullhorn.dataloader.rest;


import com.bullhorn.dataloader.data.Cell;
import com.bullhorn.dataloader.data.Row;
import com.bullhorn.dataloader.util.StringConsts;
import com.bullhornsdk.data.model.entity.core.standard.Country;
import com.bullhornsdk.data.model.parameter.standard.ParamFactory;
import com.google.common.collect.Sets;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Pre-loads data into memory prior to performing a process in order to avoid the cost of lookup calls.
 */
public class Preloader {

    private final RestSession restSession;
    private Map<String, Integer> countryNameToIdMap = null;

    public Preloader(RestSession restSession) {
        this.restSession = restSession;
    }

    /**
     * Called upon dataloader initialization (before tasks begin executing) in order to load any lookup data required
     * for entity to load.
     *
     * @param row the user provided row of data
     * @return the row that has potentially been modified to use internal bullhorn IDs
     */
    public Row convertRow(Row row) {
        Row convertedRow = new Row(row.getNumber());
        for (Cell cell : row.getCells()) {
            convertedRow.addCell(convertCell(cell));
        }
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
            if (getCountryNameToIdMap().containsKey(cell.getValue())) {
                value = getCountryNameToIdMap().get(cell.getValue()).toString();
            }
            return new Cell(name, value);
        }
        return cell;
    }

    // TODO: Make Private

    /**
     * Since the REST API only allows us to set the country using `countryID`, we query for all countries by name to
     * allow the `countryName` to upload by name instead of just the internal Bullhorn country code.
     *
     * Makes rest calls and stores the private data the first time through
     */
    public Map<String, Integer> getCountryNameToIdMap() {
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
        List<Country> countryList = restApi.queryForAllRecordsList(Country.class, "id IS NOT null",
            Sets.newHashSet("id", "name"), ParamFactory.queryParams());
        countryList.forEach(n -> countryNameToIdMap.put(n.getName().trim(), n.getId()));
        return countryNameToIdMap;
    }
}

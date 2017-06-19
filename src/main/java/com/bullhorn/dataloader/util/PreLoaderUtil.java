package com.bullhorn.dataloader.util;


import com.bullhorn.dataloader.service.executor.BullhornRestApi;
import com.bullhornsdk.data.model.entity.core.standard.Country;
import com.bullhornsdk.data.model.parameter.standard.ParamFactory;
import com.google.common.collect.Sets;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Pre-loads data into memory prior to performing a process in order to avoid the cost of lookup calls.
 */
public class PreLoaderUtil {

    final protected ConnectionUtil connectionUtil;
    Map<String, Integer> countryNameToIdMap = null;

    public PreLoaderUtil(ConnectionUtil connectionUtil) {
        this.connectionUtil = connectionUtil;
    }

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
        BullhornRestApi bullhornRestApi = connectionUtil.getSession();
        Map<String, Integer> countryNameToIdMap = new HashMap<>();
        List<Country> countryList = bullhornRestApi.queryForAllRecords(Country.class, "id IS NOT null", Sets.newHashSet("id", "name"), ParamFactory.queryParams()).getData();
        countryList.stream().forEach(n -> countryNameToIdMap.put(n.getName().trim(), n.getId()));
        return countryNameToIdMap;
    }
}

package com.bullhorn.dataloader.util;


import com.bullhorn.dataloader.enums.EntityInfo;
import com.bullhorn.dataloader.rest.BullhornRestApi;
import com.bullhornsdk.data.model.entity.core.standard.Country;
import com.bullhornsdk.data.model.parameter.standard.ParamFactory;
import com.google.common.collect.Sets;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Pre-loads data into memory prior to performing a process in order to avoid the cost of lookup calls.
 */
public class PreloadUtil {

    final private ConnectionUtil connectionUtil;
    private Map<String, Integer> countryNameToIdMap = null;

    public PreloadUtil(ConnectionUtil connectionUtil) {
        this.connectionUtil = connectionUtil;
    }

    /**
     * Called upon dataloader initialization (before tasks begin executing) in order to load any lookup data
     * required for entity to load.
     *
     * This method will determine what internal methods to call based on the entity to be loaded
     *
     * @param entityInfo The type of entity to be loaded
     */
    void preload(EntityInfo entityInfo) {
        Map<String, Method> methodMap = entityInfo.getSetterMethodMap();
        if (methodMap.containsKey("countryid")) {
            getCountryNameToIdMap();
        }
    }

    /**
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
        BullhornRestApi bullhornRestApi = connectionUtil.getSession();
        Map<String, Integer> countryNameToIdMap = new HashMap<>();
        List<Country> countryList = bullhornRestApi.queryForAllRecords(Country.class, "id IS NOT null", Sets.newHashSet("id", "name"), ParamFactory.queryParams()).getData();
        countryList.forEach(n -> countryNameToIdMap.put(n.getName().trim(), n.getId()));
        return countryNameToIdMap;
    }
}

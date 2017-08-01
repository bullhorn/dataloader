package com.bullhorn.dataloader.rest;


import com.bullhorn.dataloader.enums.EntityInfo;
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
public class Preloader {

    private final RestSession restSession;
    private Map<String, Integer> countryNameToIdMap = null;

    public Preloader(RestSession restSession) {
        this.restSession = restSession;
    }

    /**
     * Called upon dataloader initialization (before tasks begin executing) in order to load any lookup data
     * required for entity to load.
     *
     * This method will determine what internal methods to call based on the entity to be loaded
     *
     * @param entityInfo The type of entity to be loaded
     */
    public void preload(EntityInfo entityInfo) {
        Map<String, Method> methodMap = entityInfo.getSetterMethodMap();
        if (methodMap.containsKey("countryid")) {
            getCountryNameToIdMap();
        }
    }

    /**
     * Since the REST API only allows us to set the country using `countryID`, we query for all countries by name
     * to allow the `countryName` to upload by name instead of just the internal Bullhorn country code.
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

package com.bullhorn.dataloader.service.csv;

import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.Maps;

public class JsonRow {

    private final Map<String, Object> toManyProperties;

    private final Map<String, Object> nonToManyProperties;

    public JsonRow() {
        this.nonToManyProperties = Maps.newHashMap();
        this.toManyProperties = Maps.newHashMap();
    }

    public void addImmediateAction(String[] jsonPath, Object convertedValue) {
        addAction(jsonPath, convertedValue, nonToManyProperties);
    }

    public void addDeferredAction(String[] jsonPath, Object convertedValue) {
        addAction(jsonPath, convertedValue, toManyProperties);
    }

    void addAction(String[] jsonPath, Object convertedValue, Map<String, Object> toManyProperties) {
        Map<String, Object> nested = getOrCreateNested(toManyProperties, jsonPath);
        nested.put(jsonPath[jsonPath.length - 1], convertedValue);
    }

    private Map<String, Object> getOrCreateNested(Map<String, Object> actions, String[] jsonPath) {
        Map<String, Object> tmpMap = actions;
        for (int i = 0; i < jsonPath.length - 1; i++) {
            String path = jsonPath[i];
            tmpMap = getOrCreateMap(tmpMap, path);
        }
        return tmpMap;
    }

    private Map<String, Object> getOrCreateMap(Map<String, Object> tmpMap, String path) {
        if (!tmpMap.containsKey(path)) {
            tmpMap.put(path, new HashMap<String, Object>());
        }
        return (Map<String, Object>) tmpMap.get(path);
    }
    public Map<String, Object> getNonToManyProperties() {
        return nonToManyProperties;
    }

    public Map<String, Object> getToManyProperties() {
        return toManyProperties;
    }
}

package com.bullhorn.dataloader.service.csv;

import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.Maps;


/**
 * JsonRow maps JSON objects to tree structures using nested hash maps. The leaves are the type-casted values and the
 * internal nodes are strings that form the URI to access the values.
 *
 * The JsonRow knows about immediate and deferred actions. Immediate actions are run before deferred actions. There is
 * no implied ordering in executing the actions.
 */
public class JsonRow {

    private final Map<String, Object> deferredActions;

    private final Map<String, Object> immediateActions;

    public JsonRow() {
        this.immediateActions = Maps.newHashMap();
        this.deferredActions = Maps.newHashMap();
    }

    public void addImmediateAction(String[] jsonPath, Object convertedValue) {
        addAction(jsonPath, convertedValue, immediateActions);
    }

    public void addDeferredAction(String[] jsonPath, Object convertedValue) {
        addAction(jsonPath, convertedValue, deferredActions);
    }

    void addAction(String[] jsonPath, Object convertedValue, Map<String, Object> toManyProperties) {
        Map<String, Object> nested = getOrCreateNested(toManyProperties, jsonPath);
        nested.put(jsonPath[jsonPath.length - 1], convertedValue);
    }

    private Map<String, Object> getOrCreateNested(Map<String, Object> actions, String[] jsonPath) {
        Map<String, Object> tmpMap = actions;
        for (int i = 0; i < jsonPath.length - 1; i++) {
            tmpMap = getOrCreateMap(tmpMap, jsonPath[i]);
        }
        return tmpMap;
    }

    private Map<String, Object> getOrCreateMap(Map<String, Object> tmpMap, String path) {
        if (!tmpMap.containsKey(path)) {
            tmpMap.put(path, new HashMap<String, Object>());
        }
        return (Map<String, Object>) tmpMap.get(path);
    }
    public Map<String, Object> getImmediateActions() {
        return immediateActions;
    }

    public Map<String, Object> getDeferredActions() {
        return deferredActions;
    }
}

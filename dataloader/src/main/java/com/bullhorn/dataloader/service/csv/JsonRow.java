package com.bullhorn.dataloader.service.csv;

import static com.bullhorn.dataloader.util.CaseInsensitiveStringPredicate.isCustomObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;


/**
 * JsonRow maps JSON objects to tree structures using nested hash maps. The leaves are the type-casted values and the
 * internal nodes are strings that form the URI to access the values.
 * <p>
 * The JsonRow knows about immediate and deferred actions. Immediate actions are run before deferred actions. There is
 * no implied ordering in executing the actions.
 */
public class JsonRow {

    private final Map<String, Object> preprocessingActions;

    private final Map<String, Object> immediateActions;

    private final Map<String, Object> deferredActions;

    public JsonRow() {
        this.preprocessingActions = Maps.newHashMap();
        this.immediateActions = Maps.newHashMap();
        this.deferredActions = Maps.newHashMap();
    }

    public void addPreprocessing(String[] jsonPath, Object convertedValue) {
        addAction(jsonPath, convertedValue, preprocessingActions);
    }

    public void addImmediateAction(String[] jsonPath, Object convertedValue) {
        addAction(jsonPath, convertedValue, immediateActions);
    }

    public void addDeferredAction(String[] jsonPath, Object convertedValue) {
        addAction(jsonPath, convertedValue, deferredActions);
    }

    private void addAction(String[] jsonPath, Object convertedValue, Map<String, Object> toManyProperties) {
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
        Map<String, Object> map;
        if (isCustomObject(path)) {
            map = getCustomObjectMap(tmpMap, path);
        } else {
            map = getBoringObjectMap(tmpMap, path);
        }
        return map;
    }

    private static Map<String, Object> getBoringObjectMap(Map<String, Object> tmpMap, String path) {
        Map<String, Object> map;
        if (!tmpMap.containsKey(path)) {
            map = new HashMap<>();
            tmpMap.put(path, map);
        } else {
            map = (Map<String, Object>) tmpMap.get(path);
        }
        return map;
    }

    private static Map<String, Object> getCustomObjectMap(Map<String, Object> tmpMap, String path) {
        Map<String, Object> map;
        if (tmpMap.containsKey(path)) {
            List<Object> arr = (List) tmpMap.get(path);
            map = (Map<String, Object>) arr.get(0);
        } else {
            map = new HashMap<>();
            tmpMap.put(path, Lists.newArrayList(map));
        }
        return map;
    }

    public Map<String, Object> getPreprocessingActions() {
        return preprocessingActions;
    }

    public Map<String, Object> getImmediateActions() {
        return immediateActions;
    }

    public Map<String, Object> getDeferredActions() {
        return deferredActions;
    }
}

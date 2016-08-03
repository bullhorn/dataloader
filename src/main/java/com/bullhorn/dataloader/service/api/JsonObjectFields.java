package com.bullhorn.dataloader.service.api;

import org.json.JSONArray;

public class JsonObjectFields {
    private final String path;
    private final JSONArray jsonArray;

    public JsonObjectFields(String path, JSONArray jsonArray) {
        this.path = path;
        this.jsonArray = jsonArray;
    }

    public JSONArray getJsonArray() {
        return jsonArray;
    }

    public String getPath() {
        return path;
    }
}

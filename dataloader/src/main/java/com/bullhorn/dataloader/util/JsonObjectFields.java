package com.bullhorn.dataloader.util;

import org.json.JSONArray;

public class JsonObjectFields {
    private String path;
    private JSONArray jsonArray;

    public JsonObjectFields(String path, JSONArray jsonArray) {
        this.path = path;
        this.jsonArray = jsonArray;
    }

    public JSONArray getJsonArray() {
        return jsonArray;
    }

    public void setJsonArray(JSONArray jsonArray) {
        this.jsonArray = jsonArray;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}

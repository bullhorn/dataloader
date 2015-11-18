package com.bullhorn.dataloader.service.api;

import org.json.JSONArray;

public class JsonObjectFields {
    private String path;
    private JSONArray jsonArray;
    private boolean isToMany;

    public JsonObjectFields(String path, JSONArray jsonArray) {
        this.path = path;
        this.jsonArray = jsonArray;
        this.isToMany = false;
    }

    public JsonObjectFields(String path, JSONArray jsonArray, boolean isToMany) {
        this.path = path;
        this.jsonArray = jsonArray;
        this.isToMany = true;
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

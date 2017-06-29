package com.bullhorn.dataloader.rest;

import java.util.List;

/**
 * Represents the result from a search call that contains a flag to indicate whether or not the call was success,
 * and the results if it was. This is used for calls that may or may not be enabled for the current user.
 */
public class SearchResult<T> {

    private Boolean success;
    private List<T> list;

    SearchResult(Boolean success) {
        this.setSuccess(success);
    }

    public SearchResult(List<T> list) {
        this.setSuccess(true);
        this.setList(list);
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public List<T> getList() {
        return list;
    }

    public void setList(List<T> list) {
        this.list = list;
    }
}

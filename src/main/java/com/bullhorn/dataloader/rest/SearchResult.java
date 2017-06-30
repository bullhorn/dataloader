package com.bullhorn.dataloader.rest;

import java.util.List;

/**
 * Represents the result from a search call that contains a flag to indicate whether or not the call was success,
 * or whether the call is unauthorized due to a user settings, and the results if it was successful.
 * This is used for calls that may or may not be enabled for the current user.
 */
public class SearchResult<T> {

    private Boolean success = true;
    private Boolean authorized = true;
    private List<T> list = null;

    SearchResult() {
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
        this.success = true;
    }

    public Boolean getAuthorized() {
        return authorized;
    }

    public void setAuthorized(Boolean authorized) {
        this.authorized = authorized;
        if (!authorized) {
            this.success = false;
        }
    }
}

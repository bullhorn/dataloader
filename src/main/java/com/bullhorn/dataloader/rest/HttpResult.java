package com.bullhorn.dataloader.rest;

import java.net.HttpURLConnection;

public class HttpResult {
    public final boolean success;
    public final int status;
    public String body;

    public HttpResult(int status) {
        this.status = status;
        success = (this.status == HttpURLConnection.HTTP_OK);
    }
}

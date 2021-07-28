package com.bullhorn.dataloader.rest;

import org.apache.commons.lang3.StringUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Parameters {
    private Map<String, String> params = new HashMap<>();

    public void put(String name, Integer value) {
        put(name, (null==value) ? null : value.toString());
    }

    public void put(String name, Boolean value) {
        put(name, (null==value) ? null : value.toString());
    }

    public void put(String name, String value) {
        if (StringUtils.isEmpty(name)) throw new IllegalArgumentException("'name' can not be blank");
        params.put(name, value);
    }

    public String build() {
        List<String> paramList = new ArrayList<>(params.size());
        for(Map.Entry<String, String> next : params.entrySet()) {
            paramList.add(next.getKey() + "=" + encode(next.getValue()) );
        }

        return "?" + StringUtils.join(paramList, "&");
    }
    private String encode(String value) {
        if (value == null) return "";

        try {
            return URLEncoder.encode(value, "UTF-8");
        }
        catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

}

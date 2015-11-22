package com.bullhorn.dataloader.meta;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public enum MetaDataType {

    STRING("String") {
        @Override
        public Object convertFieldValue(String value, SimpleDateFormat simpleDateFormat) {
            return String.valueOf(value);
        }
    },
    INTEGER("Integer") {
        @Override
        public Object convertFieldValue(String value, SimpleDateFormat simpleDateFormat) {
            return value.isEmpty() ? 0 : Integer.valueOf(value);
        }
    },
    DOUBLE("Double") {
        @Override
        public Object convertFieldValue(String value, SimpleDateFormat simpleDateFormat) {
            return value.isEmpty() ? 0.0d : Double.valueOf(value);
        }
    },
    BOOLEAN("Boolean") {
        @Override
        public Object convertFieldValue(String value, SimpleDateFormat simpleDateFormat) {
            return value.isEmpty() ? false : Boolean.valueOf(value);
        }
    },
    TIMESTAMP("Timestamp") {
        @Override
        public Object convertFieldValue(String value, SimpleDateFormat simpleDateFormat) {
            if (value.isEmpty()) {
                return null;
            } else {
                try {
                    return simpleDateFormat.parse(value);
                } catch (ParseException e) {
                    log.error(e);
                    return value;
                }
            }
        }
    };

    private String name;
    MetaDataType(String name) {
        this.name = name;
    }
    private static final Log log = LogFactory.getLog(MetaDataType.class);

    abstract public Object convertFieldValue(String value, SimpleDateFormat simpleDateFormat);

    public static MetaDataType fromName(String name) {
        MetaDataType metaDataType = null;
        if (name != null) {
            for (MetaDataType type : MetaDataType.values()) {
                if (name.equalsIgnoreCase(type.name)) {
                    metaDataType = type;
                    break;
                }
            }
        }
        return metaDataType;
    }
}

package com.bullhorn.dataloader.util;

import java.util.Arrays;
import java.util.List;

/**
 * Global String Constants
 */
public class StringConsts {
    public static final List<String> ADDRESS_FIELDS = Arrays.asList("address1", "address2", "city", "state", "zip", "countryId", "countryName");
    public static final String ALL_FIELDS = "*";
    public static final String COLUMN_NAME_ALIAS_SUFFIX = "Column";
    public static final String CONVERTED_ATTACHMENTS_DIRECTORY = "convertedAttachments";
    public static final String CORPORATE_USER = "CorporateUser";
    public static final String COUNTRY_ID = "countryID";
    public static final String COUNTRY_NAME = "countryName";
    public static final String CSV = "csv";
    public static final String DATALOADER_PREFIX = "DATALOADER_";
    public static final String DESCRIPTION = "description";
    public static final String EXIST_FIELD_SUFFIX = "ExistField";
    public static final String EXTERNAL_ID = "externalID";
    public static final String FIRST_NAME = "firstName";
    public static final String ID = "id";
    public static final String IS_DELETED = "isDeleted";
    public static final String IS_RESUME = "isResume";
    public static final String LAST_NAME = "lastName";
    public static final String NAME = "name";
    public static final String NOTE_ID = "noteID";
    public static final String PARENT_ENTITY_ID = "parentEntityID";
    public static final String PROPERTY_FILE_ARG = "propertyfile";
    public static final String RELATIVE_FILE_PATH = "relativeFilePath";
    public static final String TIMESTAMP = DateUtil.getTimestamp();
    public static final String TO_MANY = "TO_MANY";
    public static final String TO_ONE = "TO_ONE";
}

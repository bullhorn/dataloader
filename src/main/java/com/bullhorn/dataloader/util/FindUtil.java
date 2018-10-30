package com.bullhorn.dataloader.util;

import com.bullhorn.dataloader.enums.EntityInfo;
import com.bullhorn.dataloader.rest.Field;
import com.bullhornsdk.data.exception.RestApiException;
import org.joda.time.DateTime;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Utility for constructing find call syntax (Search/Query statements)
 */
public class FindUtil {
    // Returns the format of a single term in a lucene search
    public static String getLuceneSearch(String field, String value, Class fieldType, EntityInfo fieldEntityInfo, PropertyFileUtil propertyFileUtil) {
        // Fix for the Note entity doing it's own thing when it comes to the 'id' field
        if (fieldEntityInfo == EntityInfo.NOTE && field.equals(StringConsts.ID)) {
            field = StringConsts.NOTE_ID;
        }

        if (Integer.class.equals(fieldType) || BigDecimal.class.equals(fieldType) || Boolean.class.equals(fieldType)
            || DateTime.class.equals(fieldType)) {
            return field + ":" + value;
        } else if (String.class.equals(fieldType)) {
            if (propertyFileUtil.getWildcardMatching()) {
                return field + ": " + value; // Flexible match - non quoted string (falls back to whatever text exists in the cell)
            } else {
                return field + ":\"" + value + "\""; // Literal match - equals quoted string
            }
        } else {
            throw new RestApiException("Failed to create lucene search string for: '" + field
                + "' with unsupported field type: " + fieldType);
        }
    }

    // Returns the search string for a list of terms in a lucene search
    public static String getLuceneSearch(List<Field> entityExistFields, PropertyFileUtil propertyFileUtil) {
        return entityExistFields.stream().map(
            n -> FindUtil.getLuceneSearch(n.getCell().getName(), n.getStringValue(), n.getFieldType(), n.getFieldEntity(), propertyFileUtil))
            .collect(Collectors.joining(" AND "));
    }

    // Returns the format of a single term in a query where clause
    public static String getSqlQuery(String field, String value, Class fieldType, PropertyFileUtil propertyFileUtil) {
        if (Integer.class.equals(fieldType) || BigDecimal.class.equals(fieldType) || Double.class.equals(fieldType)) {
            return field + "=" + value;
        } else if (Boolean.class.equals(fieldType)) {
            return field + "=" + (value.equals("1") ? "true" : Boolean.toString(Boolean.valueOf(value)));
        } else if (String.class.equals(fieldType)) {
            if (propertyFileUtil.getWildcardMatching()) {
                // Not all string fields in query entities support the like syntax. Only use it if there is a non-escaped asterisk.
                if (value.matches(".*[^\\\\][*].*")) {
                    return field + " like '" + value.replaceAll("[*]", "%") + "'"; // Flexible match - using like syntax
                } else {
                    return field + "='" + value.replaceAll("[\\\\][*]", "*") + "'"; // Literal match after unescaping asterisks
                }
            } else {
                return field + "='" + value + "'"; // Literal match - equals quoted string
            }
        } else if (DateTime.class.equals(fieldType)) {
            return field + value; // Allow the cell value to dictate the operation: <, >, or =
        } else {
            throw new RestApiException("Failed to create query where clause for: '" + field
                + "' with unsupported field type: " + fieldType);
        }
    }

    // Returns the search string for a list of terms in a query where clause
    public static String getSqlQuery(List<Field> entityExistFields, PropertyFileUtil propertyFileUtil) {
        return entityExistFields.stream().map(
            n -> FindUtil.getSqlQuery(n.getCell().getName(), n.getStringValue(), n.getFieldType(), propertyFileUtil))
            .collect(Collectors.joining(" AND "));
    }

    // TODO: Move down to lower level code

    /**
     * Since the 'isDeleted' value is not the same across all entities (Notes are different), this will return the appropriate string to use.
     *
     * @param entityInfo the type of entity being searched for
     * @param isDeleted  the boolean value to convert to a string
     * @return the isDeleted string value for the given boolean value, for search strings
     */
    public static String getSearchIsDeletedValue(EntityInfo entityInfo, Boolean isDeleted) {
        if (entityInfo == EntityInfo.NOTE) {
            return isDeleted ? "true" : "false";
        } else {
            return isDeleted ? "1" : "0";
        }
    }
}

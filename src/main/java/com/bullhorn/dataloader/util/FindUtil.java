package com.bullhorn.dataloader.util;

import com.bullhorn.dataloader.enums.EntityInfo;
import com.bullhorn.dataloader.rest.Field;
import com.bullhornsdk.data.exception.RestApiException;
import com.bullhornsdk.data.model.entity.core.standard.Person;
import com.bullhornsdk.data.model.entity.core.type.BullhornEntity;
import org.joda.time.DateTime;

import java.math.BigDecimal;
import java.util.Arrays;
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

    /**
     * Given a list of Fields, generates the lucene query string for /Search calls. To-Many fields will be split and turned into an OR statement.
     * All fields will be AND'ed together.
     *
     * @param entityExistFields the key/value pair list of fields to search on
     * @param propertyFileUtil  the propertyFile settings
     * @return the formatted lucene search string
     */
    public static String getLuceneSearch(List<Field> entityExistFields, PropertyFileUtil propertyFileUtil) {
        return entityExistFields.stream().map(field -> getLuceneSearch(field, propertyFileUtil)).collect(Collectors.joining(" AND "));
    }

    /**
     * Generates the lucene search string for a single field.
     *
     * For non-to-many fields: externalID: "1234567"
     * For to-many fields: (name:Jack OR name:Jill OR name:Spot)
     * TODO: For to-many id fields only: id: 1 2 3 4 5
     */
    private static String getLuceneSearch(Field field, PropertyFileUtil propertyFileUtil) {
        if (field.isToMany()) {
            String orClause = Arrays.stream(field.getStringValue().split(propertyFileUtil.getListDelimiter()))
                .map(value -> FindUtil.getLuceneSearch(field.getName(), value, field.getFieldType(), field.getFieldEntity(), propertyFileUtil))
                .collect(Collectors.joining(" OR "));
            return "(" + orClause + ")";
        } else {
            return FindUtil.getLuceneSearch(field.getName(), field.getStringValue(), field.getFieldType(), field.getFieldEntity(), propertyFileUtil);
        }
    }

    /**
     * Returns the format of a single term in a query where clause
     */
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

    /**
     * Given a list of Fields, generates the sql query string for /Query calls. To-Many fields will be split and turned into an OR statement.
     * All fields will be AND'ed together.
     *
     * @param entityExistFields the key/value pair list of fields to search on
     * @param propertyFileUtil  the propertyFile settings
     * @return the formatted where clause for the query string
     */
    public static String getSqlQuery(List<Field> entityExistFields, PropertyFileUtil propertyFileUtil) {
        return entityExistFields.stream().map(field -> FindUtil.getSqlQuery(field, propertyFileUtil)).collect(Collectors.joining(" AND "));
    }

    /**
     * Generates the query where clause for a single field
     *
     * For non-to-many fields: externalID='1234567'
     * For to-many fields: name='Jack' OR name='Jill' OR name='Spot'
     */
    private static String getSqlQuery(Field field, PropertyFileUtil propertyFileUtil) {
        if (field.isToMany()) {
            return Arrays.stream(field.getStringValue().split(propertyFileUtil.getListDelimiter()))
                .map(value -> FindUtil.getSqlQuery(field.getName(), value, field.getFieldType(), propertyFileUtil))
                .collect(Collectors.joining(" OR "));
        } else {
            return FindUtil.getSqlQuery(field.getName(), field.getStringValue(), field.getFieldType(), propertyFileUtil);
        }
    }

    /**
     * Since the 'isDeleted' value is not the same across all entities (Notes are different), this will return the appropriate string to use.
     *
     * @param entityInfo the type of entity being searched for
     * @param isDeleted  the boolean value to convert to a string
     * @return the isDeleted string value for the given boolean value, for search strings
     */
    public static String getIsDeletedValue(EntityInfo entityInfo, Boolean isDeleted) {
        if (entityInfo == EntityInfo.NOTE) {
            return isDeleted ? "true" : "false";
        } else {
            return isDeleted ? "1" : "0";
        }
    }

    /**
     * Used to determine if a person entity is truly soft-deleted or not. CorporateUser persons when disabled have their
     * deleted flag set to true, but they should always be considered active.
     *
     * @return true if the entity is not soft-deleted
     */
    public static Boolean isPersonActive(BullhornEntity entity) {
        if (entity.getClass() == Person.class) {
            Person person = (Person) entity;
            return person.getPersonSubtype().equals(StringConsts.CORPORATE_USER) || !person.getIsDeleted();
        }
        return true;
    }
}

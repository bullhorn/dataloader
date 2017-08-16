package com.bullhorn.dataloader.util;

import com.bullhorn.dataloader.enums.EntityInfo;
import com.bullhornsdk.data.exception.RestApiException;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Utility for low level method related methods used in DataLoader
 */
public class MethodUtil {

    /**
     * Returns the map of setter methods (starting with "set") for the given class
     *
     * @return A map of field names to setter methods that can invoked generically using `method.invoke`
     */
    public static Map<String, Method> getSetterMethodMap(Class anyClass) {
        Map<String, Method> setterMethodMap = new HashMap<>();

        for (Method method : Arrays.asList(anyClass.getMethods())) {
            if ("set".equalsIgnoreCase(method.getName().substring(0, 3))) {
                setterMethodMap.put(method.getName().substring(3).toLowerCase(), method);
            }
        }

        return setterMethodMap;
    }

    /**
     * Returns the setter method for the given cell on the given entity.
     *
     * @param entityInfo the entity type to find a method on (ex: EntityInfo.CANDIDATE)
     * @param fieldName  the field name of the method (ex: "externalID")
     * @return the method if it exists
     */
    public static Method getSetterMethod(EntityInfo entityInfo, String fieldName) {
        Map<String, Method> setterMethodMap = getSetterMethodMap(entityInfo.getEntityClass());
        for (String methodName : setterMethodMap.keySet()) {
            if (methodName.equalsIgnoreCase(fieldName)) {
                return setterMethodMap.get(methodName);
            }
        }
        throw new RestApiException("'" + fieldName + "' does not exist on " + entityInfo.getEntityName());
    }

    /**
     * Given a set of fields and a fieldName to search for, this method returns the field name that best matches the
     * given search fieldName. An exact match is used first, and then defaults to the first one found containing the
     * fieldName.
     *
     * @param fieldSet    the field names to search through
     * @param searchField any field name to search for
     * @return the valid name of a field from the methodMap
     */
    public static String findBestMatch(Set<String> fieldSet, String searchField) {
        Set<String> matches = fieldSet.stream()
            .filter(n -> StringUtils.containsIgnoreCase(n, searchField))
            .collect(Collectors.toSet());
        if (ArrayUtil.containsIgnoreCase(matches, searchField)) {
            return ArrayUtil.getMatchingStringIgnoreCase(matches, searchField);
        } else if (matches.size() > 0) {
            return matches.iterator().next();
        }
        return null;
    }

    /**
     * Converts the given string value to the given type, and if it's a date, using the given dateTimeFormatter.
     *
     * @param value             the user supplied string value
     * @param type              the type to convert to for rest calls
     * @param dateTimeFormatter the user supplied date time format
     * @return the object that represents the string data
     * @throws ParseException for bad provided date time string
     */
    public static Object convertStringToObject(String value, Class type, DateTimeFormatter dateTimeFormatter)
        throws ParseException {
        String trimmedValue = value.trim();

        if (String.class.equals(type)) {
            return trimmedValue;
        } else if (Integer.class.equals(type)) {
            return (StringUtils.isEmpty(trimmedValue)) ? 0 : Integer.parseInt(trimmedValue);
        } else if (Boolean.class.equals(type)) {
            return (trimmedValue.equals("1")) || Boolean.parseBoolean(trimmedValue);
        } else if (DateTime.class.equals(type)) {
            return StringUtils.isEmpty(trimmedValue) ? null : dateTimeFormatter.parseDateTime(trimmedValue);
        } else if (BigDecimal.class.equals(type)) {
            DecimalFormat decimalFormat = new DecimalFormat();
            decimalFormat.setParseBigDecimal(true);
            return StringUtils.isEmpty(trimmedValue) ? decimalFormat.parse(String.valueOf(0.0)) : decimalFormat.parse(trimmedValue);
        }

        return null;
    }
}

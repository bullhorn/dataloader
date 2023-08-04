package com.bullhorn.dataloader.util;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang.WordUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;

import com.bullhorn.dataloader.enums.EntityInfo;
import com.bullhorn.dataloader.enums.ErrorInfo;

import com.bullhornsdk.data.model.entity.core.paybill.optionslookup.SimplifiedOptionsLookup;

/**
 * Utility for low level method related methods used in DataLoader
 */
public class MethodUtil {

    /**
     * Returns the map of getter methods (starting with "get") for the given class
     * Account for methods that are named slightly different in the SDK-REST:
     * isEnabled => getIsEnabled() / getEnabled() without the "is" prefix.
     *
     * @return A map of field names to getter methods that can invoked generically using `method.invoke`
     */
    private static Map<String, Method> getGetterMethodMap(Class anyClass) {
        Map<String, Method> getterMethodMap = new HashMap<>();
        List<String> fieldNames = Arrays.stream(anyClass.getDeclaredFields())
            .map(field -> field.getName().toLowerCase()).collect(Collectors.toList());

        for (Method method : anyClass.getMethods()) {
            if ("get".equalsIgnoreCase(method.getName().substring(0, 3))) {
                String name = method.getName().substring(3).toLowerCase();
                String altName = "is" + name;
                if (fieldNames.contains(name)) {
                    getterMethodMap.put(name, method);
                } else if (fieldNames.contains(altName)) {
                    getterMethodMap.put(altName, method);
                }
            }
        }

        return getterMethodMap;
    }

    /**
     * Returns the map of setter methods (starting with "set") for the given class
     * Account for methods that are named slightly different in the SDK-REST:
     * isEnabled => getIsEnabled() / getEnabled() without the "is" prefix.
     *
     * @return A map of field names to setter methods that can invoked generically using `method.invoke`
     */
    public static Map<String, Method> getSetterMethodMap(Class anyClass) {
        Map<String, Method> setterMethodMap = new HashMap<>();
        List<String> fieldNames = Arrays.stream(anyClass.getDeclaredFields())
            .map(field -> field.getName().toLowerCase()).collect(Collectors.toList());

        for (Method method : anyClass.getMethods()) {
            if ("set".equalsIgnoreCase(method.getName().substring(0, 3))) {
                String name = method.getName().substring(3).toLowerCase();
                String altName = "is" + name;
                if (fieldNames.contains(name)) {
                    setterMethodMap.put(name, method);
                } else if (fieldNames.contains(altName)) {
                    setterMethodMap.put(altName, method);
                }
            }
        }

        return setterMethodMap;
    }

    /**
     * Returns the getter method for the given cell on the given entity.
     *
     * @param entityInfo the entity type to find a method on (ex: EntityInfo.CANDIDATE)
     * @param fieldName  the field name of the method (ex: "externalID")
     * @return the method if it exists
     */
    public static Method getGetterMethod(EntityInfo entityInfo, String fieldName) {
        Map<String, Method> getterMethodMap = getGetterMethodMap(entityInfo.getEntityClass());
        return getMethod(entityInfo, fieldName, getterMethodMap);
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
        return getMethod(entityInfo, fieldName, setterMethodMap);
    }

    /**
     * Check that users are not making a common address field mistake, to provide a better error message
     */
    private static void checkMalformedAddressField(String fieldName) {
        if (ArrayUtil.containsIgnoreCase(StringConsts.ADDRESS_FIELDS, fieldName)) {
            throw new DataLoaderException(ErrorInfo.INCORRECT_COLUMN_NAME,
                "Invalid address field format: '" + fieldName + "'. Must use: 'address."
                    + ArrayUtil.getMatchingStringIgnoreCase(StringConsts.ADDRESS_FIELDS, fieldName)
                    + "' to set an address field.");
        }
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
     * <p>
     * If the date is being used to query for existing records, then it does not need to be in the form of
     * the date time format, it can stay as a string until used in the find call.
     *
     * @param value             the user supplied string value
     * @param type              the type to convert to for rest calls
     * @param dateTimeFormatter the user supplied date time format
     * @return the object that represents the string data
     * @throws ParseException for bad provided date time string
     */
    public static Object convertStringToObject(String value, Class type, DateTimeFormatter dateTimeFormatter)
        throws ParseException {
        String searchDateRange = "\\[.* TO .*\\]";
        String queryDateRange = "[<=>].*";
        String trimmedValue = value.trim();

        if (String.class.equals(type)) {
            return trimmedValue;
        } else if (Integer.class.equals(type)) {
            return (StringUtils.isEmpty(trimmedValue)) ? 0 : Integer.parseInt(trimmedValue);
        } else if (Boolean.class.equals(type)) {
            return (trimmedValue.equals("1")) || Boolean.parseBoolean(trimmedValue);
        } else if (DateTime.class.equals(type)
            && !trimmedValue.matches(searchDateRange)
            && !trimmedValue.matches(queryDateRange)) {
            return StringUtils.isEmpty(trimmedValue) ? null : dateTimeFormatter.parseDateTime(trimmedValue);
        } else if (BigDecimal.class.equals(type)) {
            DecimalFormat decimalFormat = new DecimalFormat();
            decimalFormat.setParseBigDecimal(true);
            return StringUtils.isEmpty(trimmedValue) ? decimalFormat.parse(String.valueOf(0.0)) : decimalFormat.parse(trimmedValue);
        } else if (SimplifiedOptionsLookup.class.equals(type)) {
            SimplifiedOptionsLookup simplifiedOptionsLookup = new SimplifiedOptionsLookup();
            simplifiedOptionsLookup.setId(Integer.parseInt(value));
            return simplifiedOptionsLookup;
        }

        return null;
    }

    /**
     * Returns the name of the field associated with the getter or setter.
     * <p>
     * For example, the method: Candidate:getExternalID() will return the field name: 'externalID'
     * that can be used as a valid field name in Rest.
     *
     * @param method A getter or setter method
     * @return the field name in rest that corresponds to that getter or setter
     */
    public static String getFieldNameFromMethod(Method method) {
        return WordUtils.uncapitalize(method.getName().substring(3));
    }

    private static Method getMethod(EntityInfo entityInfo, String fieldName, Map<String, Method> methodMap) {
        for (String methodName : methodMap.keySet()) {
            if (methodName.equalsIgnoreCase(fieldName)) {
                return methodMap.get(methodName);
            }
        }
        checkMalformedAddressField(fieldName);
        throw new DataLoaderException(ErrorInfo.INCORRECT_COLUMN_NAME,
            "'" + fieldName + "' does not exist on " + entityInfo.getEntityName());
    }
}

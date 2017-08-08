package com.bullhorn.dataloader.util;

import java.lang.reflect.Method;
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
            .filter(n -> n.toLowerCase().contains(searchField.toLowerCase()))
            .collect(Collectors.toSet());
        if (matches.contains(searchField)) {
            return searchField;
        } else if (matches.size() > 0) {
            return matches.iterator().next();
        }
        return null;
    }
}

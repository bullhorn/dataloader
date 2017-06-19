package com.bullhorn.dataloader.util;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class MethodUtil {

    /**
     * Returns the map of setter methods (starting with "set") for the given class
     *
     * @return A map of field names to setter methods that can invoked generically using `method.invoke`
     */
    public static Map<String, Method> getSetterMethodMap(Class aClass) {
        Map<String, Method> setterMethodMap = new HashMap<>();

        for (Method method : Arrays.asList(aClass.getMethods())) {
            if ("set".equalsIgnoreCase(method.getName().substring(0, 3))) {
                setterMethodMap.put(method.getName().substring(3).toLowerCase(), method);
            }
        }

        return setterMethodMap;
    }
}

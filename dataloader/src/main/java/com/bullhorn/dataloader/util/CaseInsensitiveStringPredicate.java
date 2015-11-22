package com.bullhorn.dataloader.util;

public class CaseInsensitiveStringPredicate {

    public static boolean equalsIgnoreCase(String a, String b) {
        if (a == null && b == null) {
            return false;
        } else if (a != null) {
            return a.equalsIgnoreCase(b);
        } else {
            return false;
        }
    }

    public static boolean isCustomObject(String associationName) {
        return associationName.startsWith("customObject");
    }

    public static boolean isToOne(String association) {
        return equalsIgnoreCase("TO_ONE", association);
    }

    public static boolean isToMany(String association) {
        return equalsIgnoreCase("TO_MANY", association);
    }

    public static boolean isPut(String s) {
        return equalsIgnoreCase("put", s);
    }
}

package com.bullhorn.dataloader.util;

public class CaseInsensitiveStringPredicate {

    public static boolean equalsIgnoreCase(String a, String b) {
        if (a == null && b == null) {
            return false;
        } else if (a != null) {
            return a.equalsIgnoreCase(b);
        } else {
            return b.equalsIgnoreCase(a);
        }
    }

    public static boolean isCategories(String associationName) {
        return equalsIgnoreCase("categories", associationName);
    }

    public static boolean isSkills(String associationName) {
        return equalsIgnoreCase("skills", associationName);
    }

    public static boolean isBusinessSectors(String associationName) {
        return equalsIgnoreCase("businessSectors", associationName);
    }

    public static boolean isCustomObject(String associationName) {
        return associationName.startsWith("customObject");
    }

    public static boolean isToMany(String association) {
        return equalsIgnoreCase("TO_MANY", association);
    }

    public static boolean isId(String id) {
        return equalsIgnoreCase("id", id);
    }

    public static boolean isGet(String s) {
        return equalsIgnoreCase("get", s);
    }

    public static boolean isPut(String s) {
        return equalsIgnoreCase("put", s);
    }

    public static boolean isDelete(String s) {
        return equalsIgnoreCase("delete", s);
    }
}

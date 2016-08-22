package com.bullhorn.dataloader.util;

public class AssociationFilter {

    private AssociationFilter() {
    }

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
        return associationName.contains("customObject") || associationName.contains("CustomObject");
    }

    public static boolean isToOne(String associationType) {
        return equalsIgnoreCase("TO_ONE", associationType);
    }

    public static boolean isToMany(String associationType) {
        return equalsIgnoreCase("TO_MANY", associationType);
    }

    public static boolean isPut(String associationType) {
        return equalsIgnoreCase("put", associationType);
    }

    public static boolean isInteger(String dataType) {
        return equalsIgnoreCase("integer", dataType);
    }
}

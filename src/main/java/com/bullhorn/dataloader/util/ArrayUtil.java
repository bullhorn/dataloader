package com.bullhorn.dataloader.util;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Utility for low level array related methods used in DataLoader
 */
public class ArrayUtil {

    /**
     * Prepends an element to a String array
     *
     * @param firstElement  The first element to add to the new array
     * @param originalArray The original array
     * @return A copy of the original array with an extra first element
     */
    public static String[] prepend(String firstElement, String[] originalArray) {
        String[] newArray = new String[originalArray.length + 1];
        newArray[0] = firstElement;
        System.arraycopy(originalArray, 0, newArray, 1, originalArray.length);
        return newArray;
    }

    /**
     * Appends an element to a String array
     *
     * @param originalArray The original array
     * @param lastElement   The first element to add to the new array
     * @return A copy of the original array with an extra first element
     */
    public static String[] append(String[] originalArray, String lastElement) {
        String[] newArray = new String[originalArray.length + 1];
        newArray[originalArray.length] = lastElement;
        System.arraycopy(originalArray, 0, newArray, 0, originalArray.length);
        return newArray;
    }

    /**
     * Given a collection of string objects, returns true if it contains the given searchString, case insensitive.
     *
     * @param strings      the string to search through
     * @param searchString the string to search for
     * @return true if found, false otherwise
     */
    public static Boolean containsIgnoreCase(Collection<String> strings, String searchString) {
        for (String string : strings) {
            if (string.equalsIgnoreCase(searchString)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Given a collection of string objects, returns the first matching string, case insensitive.
     *
     * @param strings      the string to search through
     * @param searchString the string to search for
     * @return the string that was found, null otherwise
     */
    public static String getMatchingStringIgnoreCase(Collection<String> strings, String searchString) {
        for (String string : strings) {
            if (string.equalsIgnoreCase(searchString)) {
                return string;
            }
        }
        return null;
    }

    /**
     * Given a collection of strings, returns the collection of duplicate strings.
     *
     * @param strings the collection of strings
     * @return the duplicate strings
     */
    public static Collection<String> getDuplicates(Collection<String> strings) {
        Collection<String> uniques = new ArrayList<>();
        Collection<String> duplicates = new ArrayList<>();
        for (String string : strings) {
            if (uniques.contains(string)) {
                duplicates.add(string);
            } else {
                uniques.add(string);
            }
        }
        return duplicates;
    }
}

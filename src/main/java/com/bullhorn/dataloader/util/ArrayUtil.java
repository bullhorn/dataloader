package com.bullhorn.dataloader.util;

public class ArrayUtil {

    /**
     * Prepends an element to a String array
     *
     * @param firstElement  The first element to add to the new array
     * @param originalArray The original array
     * @return A copy of the original array with a preceeding first element
     */
    public static String[] prepend(String firstElement, String[] originalArray) {
        String[] newArray = new String[originalArray.length + 1];
        newArray[0] = firstElement;
        System.arraycopy(originalArray, 0, newArray, 1, originalArray.length);
        return newArray;
    }

    private ArrayUtil() {
    }
}

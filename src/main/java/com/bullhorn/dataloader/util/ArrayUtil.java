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

    /**
     * Appends an element to a String array
     *
     * @param originalArray The original array
     * @param lastElement   The first element to add to the new array
     * @return A copy of the original array with a preceeding first element
     */
    public static String[] append(String[] originalArray, String lastElement) {
        String[] newArray = new String[originalArray.length + 1];
        newArray[originalArray.length] = lastElement;
        System.arraycopy(originalArray, 0, newArray, 0, originalArray.length);
        return newArray;
    }
}

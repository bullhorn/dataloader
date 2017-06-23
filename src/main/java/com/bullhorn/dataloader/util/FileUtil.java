package com.bullhorn.dataloader.util;

import com.bullhorn.dataloader.enums.EntityInfo;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Utility for getting CSV files from disk.
 *
 * Handles validating and sorting individual files or all files in a directory.
 */
public class FileUtil {

    /**
     * Given a file or directory, this method will determine all valid CSV files that can be used by DataLoader and
     * collect them into a list indexed by the entity that they correspond to based on the filename. For a filename
     * argument, this list will be either empty or contain exactly one matching entity to filename.
     *
     * @param filePath   any file or directory
     * @param validationUtil The validation utility
     * @param comparator specifies how the sorted map should be sorted by entity
     * @return a Map of entity enums to lists of valid files.
     */
    static public SortedMap<EntityInfo, List<String>> getValidCsvFiles(String filePath, ValidationUtil validationUtil, Comparator<EntityInfo> comparator) {
        File file = new File(filePath);
        if (file.isDirectory()) {
            return getValidCsvFilesFromDirectory(file, validationUtil, comparator);
        } else {
            return getValidCsvFilesFromFilePath(filePath, validationUtil, comparator);
        }
    }

    /**
     * Given a directory, this method searches the directory for all valid CSV files and returns the map.
     * Multiple files for a single entity will be sorted alphabetically.
     *
     * @param directory The path to the directory (relative or absolute)
     * @param validationUtil The validation utility
     * @param comparator How to sort the list
     * @return The sorted map of entities to a list of files for each entity
     */
    static public SortedMap<EntityInfo, List<String>> getValidCsvFilesFromDirectory(File directory, ValidationUtil validationUtil, Comparator<EntityInfo> comparator) {
        SortedMap<EntityInfo, List<String>> entityToFileListMap = new TreeMap<>(comparator);

        String[] fileNames = directory.list();
        Arrays.sort(fileNames);
        for (String fileName : fileNames) {
            String absoluteFilePath = directory.getAbsolutePath() + File.separator + fileName;
            if (validationUtil.isValidCsvFile(absoluteFilePath, false)) {
                EntityInfo entityInfo = extractEntityFromFileName(fileName);
                if (entityInfo != null) {
                    if (!entityToFileListMap.containsKey(entityInfo)) {
                        entityToFileListMap.put(entityInfo, new ArrayList<>());
                    }
                    List<String> files = entityToFileListMap.get(entityInfo);
                    files.add(absoluteFilePath);
                }
            }
        }

        return entityToFileListMap;
    }

    /**
     * Given an individual file path, this method constructs the entity to file map and returns it.
     *
     * @param filePath The path to the file (relative or absolute)
     * @param validationUtil The validation utility
     * @param comparator How to sort the list
     * @return The sorted map of entities to a list of files for each entity
     */
    static public SortedMap<EntityInfo, List<String>> getValidCsvFilesFromFilePath(String filePath, ValidationUtil validationUtil, Comparator<EntityInfo> comparator) {
        SortedMap<EntityInfo, List<String>> entityToFileListMap = new TreeMap<>(comparator);

        if (validationUtil.isValidCsvFile(filePath, false)) {
            EntityInfo entityInfo = extractEntityFromFileName(filePath);
            if (entityInfo != null) {
                entityToFileListMap.put(entityInfo, Arrays.asList(filePath));
            }
        }

        return entityToFileListMap;
    }

    /**
     * Returns the list of loadable csv files in load order
     *
     * @param filePath The given file or directory
     * @param validationUtil The validation utility
     * @return the subset of getValidCsvFiles that are loadable
     */
    static public SortedMap<EntityInfo, List<String>> getLoadableCsvFilesFromPath(String filePath, ValidationUtil validationUtil) {
        SortedMap<EntityInfo, List<String>> loadableEntityToFileListMap = new TreeMap<>(EntityInfo.loadOrderComparator);

        SortedMap<EntityInfo, List<String>> entityToFileListMap = getValidCsvFiles(filePath, validationUtil, EntityInfo.loadOrderComparator);
        for (Map.Entry<EntityInfo, List<String>> entityFileEntry : entityToFileListMap.entrySet()) {
            EntityInfo entityInfo = entityFileEntry.getKey();
            if (entityInfo.isLoadable()) {
                loadableEntityToFileListMap.put(entityFileEntry.getKey(), entityFileEntry.getValue());
            }
        }

        return loadableEntityToFileListMap;
    }

    /**
     * Returns the list of deletable csv files in delete order
     *
     * @param filePath The given file or directory
     * @param validationUtil The validation utility
     * @return the subset of getValidCsvFiles that are deletable
     */
    static public SortedMap<EntityInfo, List<String>> getDeletableCsvFilesFromPath(String filePath, ValidationUtil validationUtil) {
        SortedMap<EntityInfo, List<String>> deletableEntityToFileListMap = new TreeMap<>(EntityInfo.deleteOrderComparator);

        SortedMap<EntityInfo, List<String>> entityToFileListMap = getValidCsvFiles(filePath, validationUtil, EntityInfo.deleteOrderComparator);
        for (Map.Entry<EntityInfo, List<String>> entityFileEntry : entityToFileListMap.entrySet()) {
            EntityInfo entityInfo = entityFileEntry.getKey();
            if (entityInfo.isDeletable()) {
                deletableEntityToFileListMap.put(entityFileEntry.getKey(), entityFileEntry.getValue());
            }
        }

        return deletableEntityToFileListMap;
    }

    /**
     * Extractions entity type from a file path.
     * <p>
     * The file name must start with the name of the entity
     *
     * @param fileName path from which to extract entity name
     * @return the SDK-Rest entity, or null if not found
     */
    static public EntityInfo extractEntityFromFileName(String fileName) {
        File file = new File(fileName);

        String upperCaseFileName = file.getName().toUpperCase();
        EntityInfo bestMatch = null;
        for (EntityInfo entityInfo : EntityInfo.values()) {
            if (upperCaseFileName.startsWith(entityInfo.getEntityName().toUpperCase())) {
                if (bestMatch == null) {
                    bestMatch = entityInfo;
                } else if (bestMatch.getEntityName().length() < entityInfo.getEntityName().length()) {
                    // longer name is better
                    bestMatch = entityInfo;
                }
            }
        }

        if (bestMatch == null) {
            return null;
        } else {
            return bestMatch;
        }
    }
}

package com.bullhorn.dataloader.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import com.bullhorn.dataloader.data.Row;
import com.bullhorn.dataloader.enums.EntityInfo;

/**
 * Utility for getting CSV files from disk.
 *
 * Handles validating and sorting individual files or all files in a directory.
 */
public class FileUtil {

    /**
     * For a directory:
     * Will determine all valid CSV files that can be used by Data Loader and collect them into a list indexed
     * by the entity that they correspond to based on the filename.
     *
     * For a file:
     * Will return the list containing exactly one matching entity to filename.
     *
     * @return an empty list if no valid files can be found
     */
    public static SortedMap<EntityInfo, List<String>> getValidCsvFiles(String filePath,
                                                                       PropertyFileUtil propertyFileUtil,
                                                                       Comparator<EntityInfo> comparator) {
        File file = new File(filePath);
        if (file.isDirectory()) {
            return getValidCsvFilesFromDirectory(file, comparator);
        } else {
            return getValidCsvFilesFromFilePath(filePath, propertyFileUtil, comparator);
        }
    }

    /**
     * Given a directory, this method searches the directory for all valid CSV files and returns the map. Multiple files
     * for a single entity will be sorted alphabetically.
     *
     * @param directory  The path to the directory (relative or absolute)
     * @param comparator How to sort the list
     * @return The sorted map of entities to a list of files for each entity
     */
    private static SortedMap<EntityInfo, List<String>> getValidCsvFilesFromDirectory(File directory, Comparator<EntityInfo> comparator) {
        SortedMap<EntityInfo, List<String>> entityToFileListMap = new TreeMap<>(comparator);

        String[] fileNames = directory.list();
        if (fileNames != null) {
            Arrays.sort(fileNames);
            for (String fileName : fileNames) {
                String absoluteFilePath = directory.getAbsolutePath() + File.separator + fileName;
                if (ValidationUtil.validateCsvFile(absoluteFilePath)) {
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
        }

        return entityToFileListMap;
    }

    /**
     * Given an individual file path, this method constructs the entity to file map and returns it.
     *
     * @param filePath         The path to the file (relative or absolute)
     * @param propertyFileUtil The property file utility
     * @param comparator       How to sort the list
     * @return The sorted map of entities to a list of files for each entity
     */
    private static SortedMap<EntityInfo, List<String>> getValidCsvFilesFromFilePath(String filePath,
                                                                                    PropertyFileUtil propertyFileUtil,
                                                                                    Comparator<EntityInfo> comparator) {
        SortedMap<EntityInfo, List<String>> entityToFileListMap = new TreeMap<>(comparator);

        if (ValidationUtil.validateCsvFile(filePath)) {
            EntityInfo entityInfo = extractEntityFromFileNameOrProperty(filePath, propertyFileUtil);
            if (entityInfo != null) {
                entityToFileListMap.put(entityInfo, Collections.singletonList(filePath));
            }
        }

        return entityToFileListMap;
    }

    public static SortedMap<EntityInfo, List<String>> getLoadableCsvFilesFromPath(String filePath, PropertyFileUtil propertyFileUtil) {
        SortedMap<EntityInfo, List<String>> loadableEntityToFileListMap = new TreeMap<>(EntityInfo.loadOrderComparator);
        getValidCsvFiles(filePath, propertyFileUtil, EntityInfo.loadOrderComparator).entrySet().stream()
            .filter(e -> e.getKey().isLoadable())
            .forEach(e -> loadableEntityToFileListMap.put(e.getKey(), e.getValue()));
        return loadableEntityToFileListMap;
    }

    public static SortedMap<EntityInfo, List<String>> getDeletableCsvFilesFromPath(String filePath, PropertyFileUtil propertyFileUtil) {
        SortedMap<EntityInfo, List<String>> deletableEntityToFileListMap = new TreeMap<>(EntityInfo.deleteOrderComparator);
        getValidCsvFiles(filePath, propertyFileUtil, EntityInfo.deleteOrderComparator).entrySet().stream()
            .filter(e -> e.getKey().isDeletable())
            .forEach(e -> deletableEntityToFileListMap.put(e.getKey(), e.getValue()));
        return deletableEntityToFileListMap;
    }

    /**
     * Finds the longest match of a supported entity name to the start of a filename, or overrides using the property file.
     *
     * @return the single best matching entity from the filename, or the override entity, or null if not found
     */
    public static EntityInfo extractEntityFromFileNameOrProperty(String fileName, PropertyFileUtil propertyFileUtil) {
        EntityInfo entityInfo = propertyFileUtil.getEntity();
        if (entityInfo == null) {
            entityInfo = FileUtil.extractEntityFromFileName(fileName);
        }
        return entityInfo;
    }

    /**
     * Finds the longest match of a supported entity name to the start of a filename, or overrides using the property file.
     *
     * @return the single best matching entity, or null if not found
     */
    public static EntityInfo extractEntityFromFileName(String fileName) {
        File file = new File(fileName);
        String upperCaseFileName = file.getName().toUpperCase();
        EntityInfo bestMatch = null;
        for (EntityInfo entityInfo : EntityInfo.values()) {
            if (upperCaseFileName.startsWith(entityInfo.getEntityName().toUpperCase())) {
                if (bestMatch == null) {
                    bestMatch = entityInfo;
                } else if (bestMatch.getEntityName().length() < entityInfo.getEntityName().length()) {
                    bestMatch = entityInfo; // longer entity name wins
                }
            }
        }
        return bestMatch;
    }

    /**
     * Returns the relativeFilePath file, checking relative to the current working directory and the input file.
     *
     * @param row a row of data for converting or loading attachments
     * @return the file, if found, throws exception if not found
     * @throws IOException If the column is missing
     */
    public static File getAttachmentFile(Row row) throws IOException {
        File attachmentFile;
        try {
            attachmentFile = new File(row.getValue(StringConsts.RELATIVE_FILE_PATH));
        } catch (NullPointerException e) {
            throw new IOException("Missing the '" + StringConsts.RELATIVE_FILE_PATH + "' column required for attachments");
        }
        // If the relativeFilePath is not relative to the current working directory, then try relative to the CSV file's directory
        if (!attachmentFile.exists()) {
            File currentCsvFile = new File(row.getFilePath());
            attachmentFile = new File(currentCsvFile.getParent(), row.getValue(StringConsts.RELATIVE_FILE_PATH));
        }
        return attachmentFile;
    }
}

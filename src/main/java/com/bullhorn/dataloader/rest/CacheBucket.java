package com.bullhorn.dataloader.rest;

import com.bullhornsdk.data.model.entity.core.type.BullhornEntity;
import com.google.common.collect.Maps;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Provides multiple types of caching strategies within an individual bucket, where a bucket is all searches that have the same:
 * Entity Type -> Fields Requested -> Search Criteria, such as: Lookup Candidate IDs using their ExternalID.
 */
public class CacheBucket {
    // Level one strategy - match the exact search string
    private final Map<String, List<BullhornEntity>> simpleCache;

    private String delimiter;

    CacheBucket(String delimiter) {
        this.delimiter = delimiter;
        simpleCache = Maps.newHashMap();
    }

    public List<BullhornEntity> get(List<Field> entityExistFields) {
        String cacheKey = getSimpleCacheKey(entityExistFields);
        if (simpleCache.containsKey(cacheKey)) {
            return simpleCache.get(cacheKey);
        }
        return null;
    }

    public void set(List<Field> entityExistFields, List<BullhornEntity> entities) {
        String cacheKey = getSimpleCacheKey(entityExistFields);
        simpleCache.put(cacheKey, entities);
    }

    /**
     * Return a simple string for determining if we have cached this exact search before
     *
     * Handles reordering multiple values so that simple ordering doesn't cause a cache miss.
     *
     * @param entityExistFields the fields to search for
     * @return the string to search for in level one cache
     */
    private String getSimpleCacheKey(List<Field> entityExistFields) {
        if (entityExistFields.size() == 1) {
            return entityExistFields.get(0).split(delimiter).stream().sorted().collect(Collectors.joining(delimiter));
        }
        return entityExistFields.stream().map(Field::getStringValue).collect(Collectors.joining(","));
    }
}

package com.bullhorn.dataloader.rest;

import com.bullhorn.dataloader.util.PropertyFileUtil;
import com.bullhornsdk.data.model.entity.core.type.BullhornEntity;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.lang.reflect.InvocationTargetException;
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

    // Level two strategy - cache individual entities found. Not as straightforward as level one since there is not a direct correlation between
    // search term and number of results. One search term, such as "Java*" can result in several returned items. If those items are indexed by
    // their name to id map, then later on, lookups for: "Javascript" and "Java" can result in the concrete call without having to make a new
    // Rest call.
    private Map<String, List<BullhornEntity>> individualValueCache;

    private PropertyFileUtil propertyFileUtil;

    CacheBucket(PropertyFileUtil propertyFileUtil) {
        this.propertyFileUtil = propertyFileUtil;
        simpleCache = Maps.newHashMap();
        individualValueCache = Maps.newHashMap();
    }

    public List<BullhornEntity> get(List<Field> entityExistFields) {
        String cacheKey = getSimpleCacheKey(entityExistFields);
        if (simpleCache.containsKey(cacheKey)) {
            return simpleCache.get(cacheKey);
        }

        if (entityExistFields.size() == 1) {
            Field field = entityExistFields.get(0);
            List<BullhornEntity> entities = Lists.newArrayList();
            for (String searchValue : field.split(propertyFileUtil.getListDelimiter())) {
                List<BullhornEntity> partialResults = individualValueCache.get(searchValue);
                if (partialResults == null) {
                    return null; // short circuit if any results are missing
                } else {
                    entities.addAll(partialResults);
                }
            }
            return entities;
        }
        return null;
    }

    public void set(List<Field> entityExistFields, List<BullhornEntity> entities) throws InvocationTargetException, IllegalAccessException {
        String cacheKey = getSimpleCacheKey(entityExistFields);
        simpleCache.put(cacheKey, entities);

        if (entityExistFields.size() == 1) {
            Field existField = entityExistFields.get(0);

            // Not going to attempt a level 2 cache if wildcards are in use, since this breaks the 1:1 we need for disaggregate caching
            if (propertyFileUtil.getWildcardMatching().equals(true) && existField.getStringValue().contains("*")) {
                return;
            }

            // Split up into partial results and save the individual parts
            for (String searchValue : existField.split(propertyFileUtil.getListDelimiter())) {
                List<BullhornEntity> partialResults = Lists.newArrayList();
                for (BullhornEntity entity : entities) {
                    String value = existField.getStringValueFromEntity(entity, propertyFileUtil.getListDelimiter());
                    if (value != null && value.equals(searchValue)) {
                        partialResults.add(entity);
                    }
                }
                individualValueCache.put(searchValue, partialResults);
            }
        }
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
            return entityExistFields.get(0).split(propertyFileUtil.getListDelimiter()).stream().sorted()
                .collect(Collectors.joining(propertyFileUtil.getListDelimiter()));
        }
        return entityExistFields.stream().map(Field::getStringValue).collect(Collectors.joining(","));
    }
}

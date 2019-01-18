package com.bullhorn.dataloader.rest;

import com.bullhorn.dataloader.enums.EntityInfo;
import com.bullhorn.dataloader.util.PropertyFileUtil;
import com.bullhornsdk.data.model.entity.core.type.BullhornEntity;
import com.google.common.collect.Maps;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Cache takes the parts of a query or search call and boils it down to a bucket of cached data:
 * Entity Type -> Fields Requested -> Search Criteria -> Individual Search Value
 */
public class Cache {

    /**
     * Provides multiple types of caching strategies within an individual bucket, where a bucket is all searches that have the same:
     * Entity Type -> Fields Requested -> Search Criteria, such as: Lookup Candidate IDs using their ExternalID.
     */
    public class Bucket {
        // Level one strategy - match the exact search string
        private final Map<String, List<BullhornEntity>> simpleCache;

        Bucket() {
            simpleCache = Maps.newHashMap();
        }

        public List<BullhornEntity> get(String cacheKey) {
            if (simpleCache.containsKey(cacheKey)) {
                return simpleCache.get(cacheKey);
            }
            return null;
        }

        public void set(String cacheKey, List<BullhornEntity> entities) {
            simpleCache.put(cacheKey, entities);
        }
    }

    /**
     * The Cache is a tree of maps four layers deep of:
     *
     * entityInfo - the type of entity
     * --> returnFields - the comma separated list of fields used in the fields parameter
     * ----> entityExistFields (names) - the comma separated list of names of fields to search for in the query/where clause
     * ------> Bucket of field values to entities
     */
    private final Map<EntityInfo, Map<String, Map<String, Bucket>>> entityInfoMap;
    private final PropertyFileUtil propertyFileUtil;

    public Cache(PropertyFileUtil propertyFileUtil) {
        entityInfoMap = Maps.newHashMap();
        this.propertyFileUtil = propertyFileUtil;
    }

    /**
     * Returns a cached rest return of a search/query call if present, null if never cached.
     *
     * Maps the search/query call to a list of cached results. For example, here is how the method parameters map to a candidate search call:
     *
     * http://bullhorn-rest-endpoint/search/Candidate?fields=id,name&query=status:"Active"
     *
     * @param entityInfo        the entity name to search for (ex: /Candidate)
     * @param entityExistFields the fields and values that make up the query/where clause (ex: status:"Active")
     * @param returnFields      the fields parameter (ex: id,name)
     * @return null if no cached entry exists, empty list if records were searched for and not found, one or more records if found previously
     */
    public synchronized List<BullhornEntity> getEntry(EntityInfo entityInfo, List<Field> entityExistFields, Set<String> returnFields) {
        List<BullhornEntity> entities = null;

        Map<String, Map<String, Bucket>> returnFieldsMap = entityInfoMap.get(entityInfo);
        if (returnFieldsMap != null) {
            String returnFieldsString = returnFields.stream().sorted().collect(Collectors.joining(","));
            Map<String, Bucket> searchNameMap = returnFieldsMap.get(returnFieldsString);
            if (searchNameMap != null) {
                String searchNameString = entityExistFields.stream().map(field -> field.getCell().getName()).collect(Collectors.joining(","));
                Bucket bucket = searchNameMap.get(searchNameString);
                if (bucket != null) {
                    String searchValuesString = getSimpleCacheKey(entityExistFields, propertyFileUtil);
                    entities = bucket.get(searchValuesString);
                }
            }
        }

        return entities;
    }

    public synchronized void setEntry(EntityInfo entityInfo, List<Field> entityExistFields, Set<String> returnFields, List<BullhornEntity> entities) {
        Map<String, Map<String, Bucket>> returnFieldsMap = entityInfoMap.computeIfAbsent(entityInfo, k -> Maps.newHashMap());

        String returnFieldsString = returnFields.stream().sorted().collect(Collectors.joining(","));
        Map<String, Bucket> searchNameMap = returnFieldsMap.computeIfAbsent(returnFieldsString, k -> Maps.newHashMap());

        String searchNameString = entityExistFields.stream().map(field -> field.getCell().getName()).collect(Collectors.joining(","));
        String cacheKey = getSimpleCacheKey(entityExistFields, propertyFileUtil);
        Bucket bucket = searchNameMap.computeIfAbsent(searchNameString, k -> new Bucket());
        bucket.set(cacheKey, entities);
    }

    /**
     * Return a simple string for determining if we have cached this exact search before
     *
     * Handles reordering multiple values so that simple ordering doesn't cause a cache miss.
     *
     * @param entityExistFields the fields to search for
     * @param propertyFileUtil  used for list delimiter
     * @return the string to search for in level one cache
     */
    private static String getSimpleCacheKey(List<Field> entityExistFields, PropertyFileUtil propertyFileUtil) {
        if (entityExistFields.size() == 1) {
            return entityExistFields.get(0).split(propertyFileUtil.getListDelimiter()).stream().sorted()
                .collect(Collectors.joining(propertyFileUtil.getListDelimiter()));
        }
        return entityExistFields.stream().map(Field::getStringValue).collect(Collectors.joining(","));
    }
}

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
     * The Cache is a tree of maps four layers deep of:
     *
     * entityInfo - the type of entity
     * --> returnFields - the comma separated list of fields used in the fields parameter
     * ----> entityExistFields (names) - the comma separated list of names of fields to search for in the query/where clause
     * ------> entityExistField (value) - the individual value of a field (in the case of a To-Many field, these are split up)
     *
     * The entityExistField is split apart for To-Many's into individual values so that queries for multiple objects at once can be pieced together
     * from queries for individual objects and vice-versa.
     */
    private final Map<EntityInfo, Map<String, Map<String, Map<String, List<BullhornEntity>>>>> entityInfoMap;
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

        Map<String, Map<String, Map<String, List<BullhornEntity>>>> returnFieldsMap = entityInfoMap.get(entityInfo);
        if (returnFieldsMap != null) {
            String returnFieldsString = returnFields.stream().sorted().collect(Collectors.joining(","));
            Map<String, Map<String, List<BullhornEntity>>> searchNameMap = returnFieldsMap.get(returnFieldsString);
            if (searchNameMap != null) {
                String searchNameString = entityExistFields.stream().map(field -> field.getCell().getName()).collect(Collectors.joining(","));
                Map<String, List<BullhornEntity>> searchValueMap = searchNameMap.get(searchNameString);
                if (searchValueMap != null) {
                    String searchValuesString = getSimpleCacheKey(entityExistFields);
                    entities = searchValueMap.get(searchValuesString);
                }
            }
        }

        return entities;
    }

    public synchronized void setEntry(EntityInfo entityInfo, List<Field> entityExistFields, Set<String> returnFields, List<BullhornEntity> entities) {
        Map<String, Map<String, Map<String, List<BullhornEntity>>>> returnFieldsMap = entityInfoMap.computeIfAbsent(
            entityInfo, k -> Maps.newHashMap());

        String returnFieldsString = returnFields.stream().sorted().collect(Collectors.joining(","));
        Map<String, Map<String, List<BullhornEntity>>> searchNameMap = returnFieldsMap.computeIfAbsent(returnFieldsString, k -> Maps.newHashMap());

        String searchNameString = entityExistFields.stream().map(field -> field.getCell().getName()).collect(Collectors.joining(","));
        Map<String, List<BullhornEntity>> searchValueMap = searchNameMap.computeIfAbsent(searchNameString, k -> Maps.newHashMap());

        String cacheKey = getSimpleCacheKey(entityExistFields);
        searchValueMap.put(cacheKey, entities);
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

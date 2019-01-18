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
     * ------> Bucket of field values to entities
     */
    private final Map<EntityInfo, Map<String, Map<String, CacheBucket>>> entityInfoMap;
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

        Map<String, Map<String, CacheBucket>> returnFieldsMap = entityInfoMap.get(entityInfo);
        if (returnFieldsMap != null) {
            String returnFieldsString = returnFields.stream().sorted().collect(Collectors.joining(","));
            Map<String, CacheBucket> searchNameMap = returnFieldsMap.get(returnFieldsString);
            if (searchNameMap != null) {
                String searchNameString = entityExistFields.stream().map(field -> field.getCell().getName()).collect(Collectors.joining(","));
                CacheBucket cacheBucket = searchNameMap.get(searchNameString);
                if (cacheBucket != null) {
                    entities = cacheBucket.get(entityExistFields);
                }
            }
        }

        return entities;
    }

    public synchronized void setEntry(EntityInfo entityInfo, List<Field> entityExistFields, Set<String> returnFields, List<BullhornEntity> entities) {
        Map<String, Map<String, CacheBucket>> returnFieldsMap = entityInfoMap.computeIfAbsent(entityInfo, k -> Maps.newHashMap());

        String returnFieldsString = returnFields.stream().sorted().collect(Collectors.joining(","));
        Map<String, CacheBucket> searchNameMap = returnFieldsMap.computeIfAbsent(returnFieldsString, k -> Maps.newHashMap());

        String searchNameString = entityExistFields.stream().map(field -> field.getCell().getName()).collect(Collectors.joining(","));
        CacheBucket cacheBucket = searchNameMap.computeIfAbsent(searchNameString, k -> new CacheBucket(propertyFileUtil.getListDelimiter()));

        cacheBucket.set(entityExistFields, entities);
    }
}

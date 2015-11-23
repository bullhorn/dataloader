package com.bullhorn.dataloader.service.query;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bullhorn.dataloader.util.StringConsts;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class EntityQuery {

    private final Log log = LogFactory.getLog(EntityQuery.class);

    private final String entity;

    private final Map<String, String> filterFields = Maps.newConcurrentMap();
    private final Object nestedJson;

    public Integer getFilterFieldCount() {
        return filterFieldCount;
    }

    private Integer filterFieldCount = 0;

    private Optional<Integer> id = Optional.empty();

    public EntityQuery(String entity, Object nestedJson) {
        this.entity = entity;
        this.nestedJson = nestedJson;
    }

    public Object getNestedJson() {
        return nestedJson;
    }

    public void addIsDeleted(String key, String value) {
        filterFields.put(key, value);
    }

    public void addInt(String key, String value) {
        incrementCount();
        if (StringConsts.ID.equals(key)) {
            this.id = Optional.of(Integer.parseInt(value));
        }
        filterFields.put(key, value);
    }

    public void addString(String key, String value) {
        incrementCount();
        filterFields.put(key, "'" + value + "'");
    }

    void incrementCount() {
        filterFieldCount += 1;
    }

    public String getWhereClause() {
        List<String> whereClauses = Lists.newArrayList();
        whereClauses.addAll(filterFields.keySet().stream()
                .map(field -> field + "=" + filterFields.get(field))
                .collect(Collectors.toList()));
        try {
            return URLEncoder.encode(Joiner.on(" AND ").join(whereClauses), StringConsts.UTF);
        } catch (UnsupportedEncodingException e) {
            log.error(e);
            throw new IllegalArgumentException("Unable to encode where clause", e);
        }
    }

    public String getWhereByIdClause() {
        if (getId().isPresent()) {
            try {
                return URLEncoder.encode("id=" + getId().get().toString(), StringConsts.UTF);
            } catch (UnsupportedEncodingException e) {
                log.error(e);
                throw new IllegalArgumentException("Unable to encode where clause", e);
            }
        }
        throw new IllegalArgumentException("Trying to build a query by ID but none was present");
    }

    public Optional<Integer> getId() {
        return id;
    }

    public String getEntity() {
        return entity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EntityQuery)) return false;

        EntityQuery that = (EntityQuery) o;

        if (getEntity() != null ? !getEntity().equals(that.getEntity()) : that.getEntity() != null) return false;
        if (filterFields != null ? !filterFields.equals(that.filterFields) : that.filterFields != null) return false;
        if (getNestedJson() != null ? !getNestedJson().equals(that.getNestedJson()) : that.getNestedJson() != null)
            return false;
        return !(getId() != null ? !getId().equals(that.getId()) : that.getId() != null);

    }

    @Override
    public int hashCode() {
        int result = getEntity() != null ? getEntity().hashCode() : 0;
        result = 31 * result + (filterFields != null ? filterFields.hashCode() : 0);
        result = 31 * result + (getNestedJson() != null ? getNestedJson().hashCode() : 0);
        result = 31 * result + (getId() != null ? getId().hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "EntityQuery{" +
                "entity='" + entity + '\'' +
                ", filterFields=" + filterFields +
                ", nestedJson=" + nestedJson +
                ", id=" + id +
                '}';
    }
}

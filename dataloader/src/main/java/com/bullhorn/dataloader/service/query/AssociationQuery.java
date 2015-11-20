package com.bullhorn.dataloader.service.query;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class AssociationQuery {

    private final Log log = LogFactory.getLog(AssociationQuery.class);

    private final String entity;

    private final Map<String, String> filterFields = Maps.newConcurrentMap();
    private final Object nestedJson;

    private Optional<Integer> id = Optional.empty();
    private String UTF = "UTF-8";

    public AssociationQuery(String entity, Object nestedJson) {
        this.entity = entity;
        this.nestedJson = nestedJson;
    }

    public Object getNestedJson() {
        return nestedJson;
    }

    public void addInt(String key, String value) {
        if (key.equals("id")) {
            this.id = Optional.of(Integer.parseInt(value));
        }
        filterFields.put(key, value);
    }

    public void addString(String key, String value) {
        filterFields.put(key, "'" + value + "'");
    }

    public String getWhereClause() {
        List<String> whereClauses = Lists.newArrayList();
        whereClauses.addAll(filterFields.keySet().stream()
                .map(field -> field + "=" + filterFields.get(field))
                .collect(Collectors.toList()));
        try {
            return URLEncoder.encode(Joiner.on(" AND ").join(whereClauses), UTF);
        } catch (UnsupportedEncodingException e) {
            log.error(e);
            throw new IllegalArgumentException("Unable to encode where clause", e);
        }
    }

    public String getWhereByIdClause() {
        if (getId().isPresent()) {
            try {
                return URLEncoder.encode("id=" + getId().get().toString(), UTF);
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
        if (!(o instanceof AssociationQuery)) return false;

        AssociationQuery that = (AssociationQuery) o;

        return Objects.equals(getEntity(), that.getEntity())
                && filterFields.equals(that.filterFields);
    }

    @Override
    public int hashCode() {
        int result = getEntity().hashCode();
        result = 31 * result + filterFields.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "AssociationQuery{" +
                "entity='" + entity + '\'' +
                ", filterFields=" + filterFields +
                '}';
    }
}

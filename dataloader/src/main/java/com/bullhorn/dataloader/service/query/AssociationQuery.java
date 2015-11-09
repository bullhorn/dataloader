package com.bullhorn.dataloader.service.query;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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

    public AssociationQuery(String entity) {
        this.entity = entity;
    }

    public void addCondition(String field, String filter) {
        this.filterFields.put(field, filter);
    }

    public String getWhereClause() {
        List<String> whereClauses = Lists.newArrayList();
        whereClauses.addAll(filterFields.keySet().stream()
                .map(field -> field + "=" + filterFields.get(field))
                .collect(Collectors.toList()));
        try {
            return URLEncoder.encode(Joiner.on('&').join(whereClauses), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            log.error(e);
            throw new IllegalArgumentException("Unable to encode where clause", e);
        }
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
}

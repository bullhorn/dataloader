package com.bullhorn.dataloader.service.query;

import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class EntityQueryTest {

    @Test
    public void testGetWhereClause_id() {
        Map<String, Object> nestedJson = Maps.newHashMap();

        EntityQuery entityQuery = new EntityQuery("ClientCorporation", nestedJson);
        entityQuery.addInt("id", "42");

        Assert.assertEquals("id%3D42", entityQuery.getWhereClause());
    }

    @Test
    public void testGetWhereClause_int() {
        Map<String, Object> nestedJson = Maps.newHashMap();

        EntityQuery entityQuery = new EntityQuery("ClientCorporation", nestedJson);
        entityQuery.addInt("int1", "42");

        Assert.assertEquals("int1%3D42", entityQuery.getWhereClause());
    }

    @Test
    public void testGetWhereClause_twoValues() {
        Map<String, Object> nestedJson = Maps.newHashMap();

        EntityQuery entityQuery = new EntityQuery("ClientCorporation", nestedJson);
        entityQuery.addInt("int1", "42");
        entityQuery.addString("string", "42");

        Assert.assertEquals("string%3D%2742%27+AND+int1%3D42", entityQuery.getWhereClause());
    }

    @Test
    public void testGetWhereIdClause_id() {
        Map<String, Object> nestedJson = Maps.newHashMap();

        EntityQuery entityQuery = new EntityQuery("ClientCorporation", nestedJson);
        entityQuery.addInt("id", "42");

        Assert.assertEquals("id%3D42", entityQuery.getWhereClause());
    }

    @Test
    public void testGetWhereIdClause_id_notAdded() {
        Map<String, Object> nestedJson = Maps.newHashMap();

        EntityQuery entityQuery = new EntityQuery("ClientCorporation", nestedJson);

        Assert.assertEquals("", entityQuery.getWhereClause());
    }

    @Test
    public void testGetWhereByIdClause_id() {
        Map<String, Object> nestedJson = Maps.newHashMap();

        EntityQuery entityQuery = new EntityQuery("ClientCorporation", nestedJson);
        entityQuery.addInt("id", "42");

        Assert.assertEquals("id%3D42", entityQuery.getWhereByIdClause());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetWhereByIdClause_id_notAdded() {
        Map<String, Object> nestedJson = Maps.newHashMap();

        EntityQuery entityQuery = new EntityQuery("ClientCorporation", nestedJson);

        Assert.assertEquals("", entityQuery.getWhereByIdClause());
    }

    @Test
    public void testHashCode_entity() {
        Set<EntityQuery> associationQueries = Sets.newHashSet();
        associationQueries.add(new EntityQuery("ClientCorporation", null));
        associationQueries.add(new EntityQuery("ClientContact", null));
        associationQueries.add(new EntityQuery("Candidate", null));
        associationQueries.add(new EntityQuery("Candidate", null));

        Assert.assertEquals(3, associationQueries.size());
    }

    @Test
    public void testHashCode_filterFields() {
        Set<EntityQuery> associationQueries = Sets.newHashSet();
        associationQueries.add(new EntityQuery("ClientCorporation", null));
        associationQueries.add(new EntityQuery("ClientCorporation", null) {{
            addInt("int", "42");
        }});

        Assert.assertEquals(2, associationQueries.size());
    }

    @Test
    public void testEquals() {
        EntityQuery entityQuery1 = new EntityQuery("ClientCorporation", null) {{ addInt("int", "42"); }};
        EntityQuery entityQuery2 = new EntityQuery("ClientCorporation", null) {{ addInt("int", "42"); }};
        EntityQuery different = new EntityQuery("ClientCorporation", null) {{ addInt("int", "43"); }};

        Assert.assertEquals(entityQuery1, entityQuery2);
        Assert.assertNotSame(entityQuery1, different);
    }

    @Test
    public void testToString() {
        EntityQuery entityQuery = new EntityQuery("ClientCorporation", null) {{ addInt("int", "42"); }};
        Assert.assertEquals(
                "EntityQuery{" +
                        "entity='ClientCorporation'" +
                        ", filterFields={int=42}" +
                        ", nestedJson=null" +
                        ", id=Optional.empty}",
                entityQuery.toString());
    }
}

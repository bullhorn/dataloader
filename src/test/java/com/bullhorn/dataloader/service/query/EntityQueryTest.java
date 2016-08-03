package com.bullhorn.dataloader.service.query;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import com.bullhorn.dataloader.service.api.EntityInstance;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class EntityQueryTest {

    @Test
    public void testGetWhereClause_id() {
        final Map<String, Object> nestedJson = Maps.newHashMap();
        final EntityQuery entityQuery = new EntityQuery("ClientCorporation", nestedJson);
        entityQuery.addInt("id", "42");

        Assert.assertEquals("id%3D42", entityQuery.getWhereClause());
    }

    @Test
    public void testGetWhereClause_int() {
        final Map<String, Object> nestedJson = Maps.newHashMap();
        final EntityQuery entityQuery = new EntityQuery("ClientCorporation", nestedJson);
        entityQuery.addInt("int1", "42");

        Assert.assertEquals("int1%3D42", entityQuery.getWhereClause());
    }

    @Test
    public void testGetWhereClause_twoValues() {
        final Map<String, Object> nestedJson = Maps.newHashMap();
        final EntityQuery entityQuery = new EntityQuery("ClientCorporation", nestedJson);
        entityQuery.addInt("int1", "42");
        entityQuery.addString("string", "42");

        Assert.assertEquals("string%3D%2742%27+AND+int1%3D42", entityQuery.getWhereClause());
    }

    @Test
    public void testGetWhereIdClause_id() {
        final Map<String, Object> nestedJson = Maps.newHashMap();
        final EntityQuery entityQuery = new EntityQuery("ClientCorporation", nestedJson);
        entityQuery.addInt("id", "42");
        entityQuery.addInt("int1", "42");
        entityQuery.addString("string", "42");

        Assert.assertEquals("id%3D42", entityQuery.getWhereByIdClause());
        Assert.assertNotEquals(entityQuery.getWhereClause(), entityQuery.getWhereByIdClause());
    }

    @Test
    public void testGetWhereIdClause_id_notAdded() {
        final Map<String, Object> nestedJson = Maps.newHashMap();
        final EntityQuery entityQuery = new EntityQuery("ClientCorporation", nestedJson);

        Assert.assertEquals("", entityQuery.getWhereClause());
    }

    @Test
    public void testGetWhereByIdClause_id() {
        final Map<String, Object> nestedJson = Maps.newHashMap();
        final EntityQuery entityQuery = new EntityQuery("ClientCorporation", nestedJson);
        entityQuery.addInt("id", "42");
        entityQuery.addInt("int1", "42");
        entityQuery.addString("string", "42");

        Assert.assertEquals("id%3D42", entityQuery.getWhereByIdClause());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetWhereByIdClause_id_notAdded() {
        final Map<String, Object> nestedJson = Maps.newHashMap();
        final EntityQuery entityQuery = new EntityQuery("ClientCorporation", nestedJson);

        Assert.assertEquals("", entityQuery.getWhereByIdClause());
    }

    @Test
    public void testGetSearchClause_id() {
        final Map<String, Object> nestedJson = Maps.newHashMap();
        final EntityQuery entityQuery = new EntityQuery("ClientCorporation", nestedJson);
        entityQuery.addInt("id", "42");

        Assert.assertEquals("id%3A%2242%22", entityQuery.getSearchClause());
    }

    @Test
    public void testGetSearchClause_int() {
        final Map<String, Object> nestedJson = Maps.newHashMap();
        final EntityQuery entityQuery = new EntityQuery("ClientCorporation", nestedJson);
        entityQuery.addInt("int1", "42");

        Assert.assertEquals("int1%3A%2242%22", entityQuery.getSearchClause());
    }

    @Test
    public void testAddFieldWithoutCount() {
        final Map<String, Object> nestedJson = Maps.newHashMap();
        final EntityQuery entityQuery = new EntityQuery("ClientCorporation", nestedJson);
        Assert.assertEquals(0, entityQuery.getFilterFieldCount().intValue());

        entityQuery.addInt("int1", "42");
        Assert.assertEquals(1, entityQuery.getFilterFieldCount().intValue());

        entityQuery.addString("text1", "blah");
        Assert.assertEquals(2, entityQuery.getFilterFieldCount().intValue());

        entityQuery.addFieldWithoutCount("isDeleted", "false");
        Assert.assertEquals(2, entityQuery.getFilterFieldCount().intValue());
        Assert.assertEquals("int1%3D42+AND+isDeleted%3Dfalse+AND+text1%3D%27blah%27", entityQuery.getWhereClause());
    }

    @Test
    public void testAddMemberOfWithoutCount() {
        final Map<String, Object> nestedJson = Maps.newHashMap();
        final EntityQuery entityQuery = new EntityQuery("ClientCorporation", nestedJson);
        entityQuery.addMemberOfWithoutCount("privateLabel", "12345");

        Assert.assertEquals("12345+member+of+privateLabel", entityQuery.getWhereClause());
        Assert.assertEquals(0, entityQuery.getFilterFieldCount().intValue());
    }

    @Test
    public void testHashCode_entity() {
        final Set<EntityQuery> associationQueries = Sets.newHashSet();
        associationQueries.add(new EntityQuery("ClientCorporation", null));
        associationQueries.add(new EntityQuery("ClientContact", null));
        associationQueries.add(new EntityQuery("Candidate", null));
        associationQueries.add(new EntityQuery("Candidate", null));

        Assert.assertEquals(3, associationQueries.size());
    }

    @Test
    public void testHashCode_filterFields() {
        final Set<EntityQuery> associationQueries = Sets.newHashSet();
        associationQueries.add(new EntityQuery("ClientCorporation", null));
        associationQueries.add(new EntityQuery("ClientCorporation", null));
        associationQueries.add(new EntityQuery("ClientCorporation", null) {{
            addInt("int", "42");
        }});

        Assert.assertEquals(2, associationQueries.size());
    }

    @Test
    public void testEquals_identity() {
        final EntityQuery entityQuery1 = new EntityQuery("ClientCorporation", null) {{ addInt("int", "42"); }};
        final EntityQuery entityQuery2 = entityQuery1;
        Assert.assertEquals(entityQuery1, entityQuery2);
    }

    @Test
    public void testEquals_type() {
        final EntityQuery entityQuery1 = new EntityQuery("ClientCorporation", null) {{ addInt("int", "42"); }};
        final EntityInstance entityInstance = new EntityInstance("99", "Candidate");
        Assert.assertNotEquals(entityQuery1, entityInstance);
    }

    @Test
    public void testEquals_entity() {
        final EntityQuery entityQuery1 = new EntityQuery("ClientCorporation", null);
        final EntityQuery entityQuery2 = new EntityQuery("ClientCorporation", null);
        final EntityQuery different = new EntityQuery("ClientContact", null);

        Assert.assertEquals(entityQuery1, entityQuery2);
        Assert.assertNotEquals(entityQuery1, different);
    }

    @Test
    public void testEquals_filterFields() {
        final EntityQuery entityQuery1 = new EntityQuery("ClientCorporation", null) {{ addInt("int", "42"); }};
        final EntityQuery entityQuery2 = new EntityQuery("ClientCorporation", null) {{ addInt("int", "42"); }};
        final EntityQuery different = new EntityQuery("ClientCorporation", null) {{ addInt("int", "43"); }};

        Assert.assertEquals(entityQuery1, entityQuery2);
        Assert.assertNotEquals(entityQuery1, different);
    }

    @Test
    public void testEquals_nestedJson() {
        final Map<String, Object> map1 = new HashMap<>();
        map1.put("isDeleted", "false");

        final Map<String, Object> map2 = new HashMap<>();
        map2.put("isDeleted", "true");

        final EntityQuery entityQuery1 = new EntityQuery("ClientCorporation", map1);
        final EntityQuery entityQuery2 = new EntityQuery("ClientCorporation", map1);
        final EntityQuery different = new EntityQuery("ClientCorporation",  map2);
        final EntityQuery differentNull = new EntityQuery("ClientCorporation",  null);

        Assert.assertEquals(entityQuery1, entityQuery2);
        Assert.assertNotEquals(entityQuery1, different);
        Assert.assertNotEquals(entityQuery1, differentNull);
    }

    @Test
    public void testEquals_nestedJsonString() {
        final EntityQuery entityQuery1 = new EntityQuery("ClientCorporation", "{isDeleted: true}");
        final EntityQuery entityQuery2 = new EntityQuery("ClientCorporation", "{isDeleted: true}");
        final EntityQuery different = new EntityQuery("ClientCorporation", "{isDeleted: false}");

        Assert.assertEquals(entityQuery1, entityQuery2);
        Assert.assertNotEquals(entityQuery1, different);
    }

    @Test
    public void testEquals_id() {
        final EntityQuery entityQuery1 = new EntityQuery("ClientCorporation", null) {{ addInt("id", "42"); }};
        final EntityQuery entityQuery2 = new EntityQuery("ClientCorporation", null) {{ addInt("id", "42"); }};
        final EntityQuery different = new EntityQuery("ClientCorporation", null) {{ addInt("id", "43"); }};

        Assert.assertEquals(entityQuery1, entityQuery2);
        Assert.assertNotEquals(entityQuery1, different);
    }

    @Test
    public void testToString() {
        final EntityQuery entityQuery1 = new EntityQuery("ClientCorporation", null) {{ addInt("int", "42"); }};
        Assert.assertEquals(
                "EntityQuery{" +
                        "entity='ClientCorporation'" +
                        ", filterFields={int=42}" +
                        ", nestedJson=null" +
                        ", id=Optional.empty}",
                entityQuery1.toString());

        final EntityQuery entityQuery2 = new EntityQuery("ClientCorporation", new JSONObject("{isDeleted: false}")) {{ addInt("int", "42"); }};
        Assert.assertEquals(
                "EntityQuery{" +
                        "entity='ClientCorporation'" +
                        ", filterFields={int=42}" +
                        ", nestedJson={\"isDeleted\":false}" +
                        ", id=Optional.empty}",
                entityQuery2.toString());
    }
}

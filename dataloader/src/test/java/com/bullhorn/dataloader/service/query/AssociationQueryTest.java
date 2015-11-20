package com.bullhorn.dataloader.service.query;

import java.util.Map;
import java.util.Set;

import org.junit.Test;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import junit.framework.TestCase;

public class AssociationQueryTest {

    @Test
    public void testGetWhereClause_id() {
        Map<String, Object> nestedJson = Maps.newHashMap();

        AssociationQuery associationQuery = new AssociationQuery("ClientCorporation", nestedJson);
        associationQuery.addInt("id", "42");

        TestCase.assertEquals("id%3D42", associationQuery.getWhereClause());
    }

    @Test
    public void testGetWhereClause_int() {
        Map<String, Object> nestedJson = Maps.newHashMap();

        AssociationQuery associationQuery = new AssociationQuery("ClientCorporation", nestedJson);
        associationQuery.addInt("int1", "42");

        TestCase.assertEquals("int1%3D42", associationQuery.getWhereClause());
    }

    @Test
    public void testGetWhereClause_twoValues() {
        Map<String, Object> nestedJson = Maps.newHashMap();

        AssociationQuery associationQuery = new AssociationQuery("ClientCorporation", nestedJson);
        associationQuery.addInt("int1", "42");
        associationQuery.addString("string", "42");

        TestCase.assertEquals("string%3D%2742%27+AND+int1%3D42", associationQuery.getWhereClause());
    }

    @Test
    public void testGetWhereIdClause_id() {
        Map<String, Object> nestedJson = Maps.newHashMap();

        AssociationQuery associationQuery = new AssociationQuery("ClientCorporation", nestedJson);
        associationQuery.addInt("id", "42");

        TestCase.assertEquals("id%3D42", associationQuery.getWhereClause());
    }

    @Test
    public void testGetWhereIdClause_id_notAdded() {
        Map<String, Object> nestedJson = Maps.newHashMap();

        AssociationQuery associationQuery = new AssociationQuery("ClientCorporation", nestedJson);

        TestCase.assertEquals("", associationQuery.getWhereClause());
    }

    @Test
    public void testGetWhereByIdClause_id() {
        Map<String, Object> nestedJson = Maps.newHashMap();

        AssociationQuery associationQuery = new AssociationQuery("ClientCorporation", nestedJson);
        associationQuery.addInt("id", "42");

        TestCase.assertEquals("id%3D42", associationQuery.getWhereByIdClause());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetWhereByIdClause_id_notAdded() {
        Map<String, Object> nestedJson = Maps.newHashMap();

        AssociationQuery associationQuery = new AssociationQuery("ClientCorporation", nestedJson);

        TestCase.assertEquals("", associationQuery.getWhereByIdClause());
    }

    @Test
    public void testHashCode_entity() {
        Set<AssociationQuery> associationQueries = Sets.newHashSet();
        associationQueries.add(new AssociationQuery("ClientCorporation", null));
        associationQueries.add(new AssociationQuery("ClientContact", null));
        associationQueries.add(new AssociationQuery("Candidate", null));
        associationQueries.add(new AssociationQuery("Candidate", null));

        TestCase.assertEquals(3, associationQueries.size());
    }

    @Test
    public void testHashCode_filterFields() {
        Set<AssociationQuery> associationQueries = Sets.newHashSet();
        associationQueries.add(new AssociationQuery("ClientCorporation", null));
        associationQueries.add(new AssociationQuery("ClientCorporation", null) {{
            addInt("int", "42");
        }});

        TestCase.assertEquals(2, associationQueries.size());
    }

    @Test
    public void testEquals() {
        AssociationQuery associationQuery1 = new AssociationQuery("ClientCorporation", null) {{ addInt("int", "42"); }};
        AssociationQuery associationQuery2 = new AssociationQuery("ClientCorporation", null) {{ addInt("int", "42"); }};
        AssociationQuery different = new AssociationQuery("ClientCorporation", null) {{ addInt("int", "43"); }};

        TestCase.assertEquals(associationQuery1, associationQuery2);
        TestCase.assertNotSame(associationQuery1, different);
    }

    @Test
    public void testToString() {
        AssociationQuery associationQuery = new AssociationQuery("ClientCorporation", null) {{ addInt("int", "42"); }};
        TestCase.assertEquals(
                "AssociationQuery{" +
                        "entity='ClientCorporation'" +
                        ", filterFields={int=42}" +
                        ", nestedJson=null" +
                        ", id=Optional.empty}",
                associationQuery.toString());
    }
}

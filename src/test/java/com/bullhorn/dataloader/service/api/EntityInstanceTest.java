package com.bullhorn.dataloader.service.api;

import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import com.bullhorn.dataloader.service.query.EntityQuery;
import com.google.common.collect.Sets;

public class EntityInstanceTest {

    @Test
    public void testToString() {
        final EntityInstance entityInstance = new EntityInstance("99", "Candidate");

        Assert.assertEquals(entityInstance.getEntityId(), "99");
        Assert.assertEquals(entityInstance.getEntityName(), "Candidate");
        Assert.assertEquals(entityInstance.toString(), "EntityInstance{entityName='Candidate', entityId='99'}");
    }

    @Test
    public void testEquals_identity() {
        final EntityInstance entityInstance1 = new EntityInstance("99", "Candidate");
        final EntityInstance entityInstance2 = entityInstance1;

        Assert.assertEquals(entityInstance1, entityInstance2);
    }

    @Test
    public void testEquals_type() {
        final EntityInstance entityInstance = new EntityInstance("99", "Candidate");
        final EntityQuery entityQuery = new EntityQuery("Candidate", null);

        Assert.assertNotEquals(entityInstance, entityQuery);
    }

    @Test
    public void testEquals_entity() {
        final EntityInstance entityInstance1 = new EntityInstance("99", "Candidate");
        final EntityInstance entityInstance2 = new EntityInstance("99", "Candidate");
        final EntityInstance different = new EntityInstance("99", "ClientContact");

        Assert.assertEquals(entityInstance1, entityInstance2);
        Assert.assertNotEquals(entityInstance1, different);
    }

    @Test
    public void testEquals_id() {
        final EntityInstance entityInstance1 = new EntityInstance("99", "Candidate");
        final EntityInstance entityInstance2 = new EntityInstance("99", "Candidate");
        final EntityInstance different = new EntityInstance("100", "Candidate");

        Assert.assertEquals(entityInstance1, entityInstance2);
        Assert.assertNotEquals(entityInstance1, different);
    }

    @Test
    public void testHashCode_id() {
        final Set<EntityInstance> entityInstances = Sets.newHashSet();

        entityInstances.add(new EntityInstance("99", "Candidate"));
        entityInstances.add(new EntityInstance("99", "Candidate"));
        entityInstances.add(new EntityInstance("100", "Candidate"));

        Assert.assertEquals(2, entityInstances.size());
    }

    @Test
    public void testHashCode_entity() {
        final Set<EntityInstance> entityInstances = Sets.newHashSet();

        entityInstances.add(new EntityInstance("99", "Candidate"));
        entityInstances.add(new EntityInstance("99", "Candidate"));
        entityInstances.add(new EntityInstance("99", "ClientContact"));

        Assert.assertEquals(2, entityInstances.size());
    }
}
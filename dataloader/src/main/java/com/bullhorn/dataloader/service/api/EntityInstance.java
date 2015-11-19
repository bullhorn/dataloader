package com.bullhorn.dataloader.service.api;

public class EntityInstance {
    private final String entityName;
    private final String entityId;

    public EntityInstance(String entityName, String entityId) {
        this.entityName = entityName;
        this.entityId = entityId;
    }

    @Override
    public String toString() {
        return "EntityInstance{" +
                "entityName='" + entityName + '\'' +
                ", entityId='" + entityId + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EntityInstance)) return false;

        EntityInstance entityInstance = (EntityInstance) o;

        return entityName.equals(entityInstance.entityName)
                && entityId.equals(entityInstance.entityId);

    }

    @Override
    public int hashCode() {
        int result = entityName.hashCode();
        result = 31 * result + entityId.hashCode();
        return result;
    }
}

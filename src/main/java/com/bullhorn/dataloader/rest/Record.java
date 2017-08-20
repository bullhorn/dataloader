package com.bullhorn.dataloader.rest;

import com.bullhorn.dataloader.data.Cell;
import com.bullhorn.dataloader.data.Row;
import com.bullhorn.dataloader.enums.EntityInfo;
import com.bullhorn.dataloader.util.ArrayUtil;
import com.bullhorn.dataloader.util.PropertyFileUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The data from a Row applied to a specific entity and set of fields (direct or associated to-one) on an entity.
 */
public class Record {

    private final EntityInfo entityInfo;
    private final Row row;
    private final List<Field> fields;

    /**
     * Constructor which takes the type of entity and the raw cell data.
     *
     * @param entityInfo       the type of entity that this row is for
     * @param row              the raw data that this record represents
     * @param propertyFileUtil the user set properties
     */
    public Record(EntityInfo entityInfo, Row row, PropertyFileUtil propertyFileUtil) {
        this.entityInfo = entityInfo;
        this.row = row;
        this.fields = new ArrayList<>();
        List<String> existFields = propertyFileUtil.getEntityExistFields(entityInfo);
        for (Cell cell : row.getCells()) {
            Boolean isExistField = ArrayUtil.containsIgnoreCase(existFields, cell.getName());
            Field field = new Field(entityInfo, cell, isExistField, propertyFileUtil.getDateParser());
            this.fields.add(field);
        }
    }

    public EntityInfo getEntityInfo() {
        return entityInfo;
    }

    public Integer getNumber() {
        return row.getNumber();
    }

    /**
     * Returns all valid fields in this row. This way, empty association and date fields are safely ignored.
     *
     * @return only the fields that can be set on the entity
     */
    public List<Field> getFields() {
        return fields;
    }

    /**
     * Returns all fields being used for the entity exist check.
     *
     * @return an empty list if no exist field check is configured or exist fields are missing
     */
    public List<Field> getEntityExistFields() {
        return fields.stream().filter(Field::isExistField).collect(Collectors.toList());
    }
}
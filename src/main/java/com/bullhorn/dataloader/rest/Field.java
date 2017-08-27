package com.bullhorn.dataloader.rest;

import com.bullhorn.dataloader.data.Cell;
import com.bullhorn.dataloader.enums.EntityInfo;
import com.bullhorn.dataloader.util.AssociationUtil;
import com.bullhorn.dataloader.util.MethodUtil;
import com.bullhornsdk.data.model.entity.core.type.BullhornEntity;
import com.bullhornsdk.data.model.entity.embedded.Address;
import com.bullhornsdk.data.model.entity.embedded.OneToMany;
import org.joda.time.format.DateTimeFormatter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.util.List;

/**
 * The data from a Cell applied to a specific entity and field (direct or associated to-one) on an entity.
 *
 * This information is required for looking up entities in REST and is derived from the simple string value in the Cell.
 * It is derived from the simple Cell data and constructs a rich set of data required for making REST calls.
 */
public class Field {

    private final EntityInfo entityInfo;
    private final Cell cell;
    private Boolean existField;
    private final DateTimeFormatter dateTimeFormatter;
    private Method getMethod = null;
    private Method setMethod = null;
    private Method getAssociationMethod = null;
    private Method setAssociationMethod = null;

    /**
     * Constructor which takes the type of entity and the raw cell data.
     *
     * @param entityInfo        the type of entity that this cell is for
     * @param cell              the raw data from the spreadsheet
     * @param existField        set to true if this field is to be used for determining if the entity exists (a.k.a.
     *                          duplicate checker)
     * @param dateTimeFormatter the format of date/time that the user has configured
     */
    public Field(EntityInfo entityInfo,
                 Cell cell,
                 Boolean existField,
                 DateTimeFormatter dateTimeFormatter) {
        this.entityInfo = entityInfo;
        this.cell = cell;
        this.existField = existField;
        this.dateTimeFormatter = dateTimeFormatter;

        if (!cell.isIgnored()) {
            // The getMethod/setMethod will be the direct methods on either the current entity or the associated entity.
            // For Example:
            //     'externalID' => <CurrentEntity>:getExternalId() / <CurrentEntity>:setExternalId()
            //   'candidate.id' => Candidate:getId() / Candidate:setId()
            //  'candidates.id' => Candidate:getId() / Candidate:setId()
            this.getMethod = MethodUtil.getGetterMethod(getFieldEntity(), getName());
            this.setMethod = MethodUtil.getSetterMethod(getFieldEntity(), getName());

            // For all non-direct fields, store the get/set methods for the association, such as getAddress()/setAddress()
            if (cell.isAssociation()) {
                this.getAssociationMethod = MethodUtil.getGetterMethod(entityInfo, cell.getAssociationBaseName());
                this.setAssociationMethod = MethodUtil.getSetterMethod(entityInfo, cell.getAssociationBaseName());
            } else {
                this.getAssociationMethod = null;
                this.setAssociationMethod = null;
            }
        }
    }

    public Cell getCell() {
        return cell;
    }

    public EntityInfo getEntityInfo() {
        return entityInfo;
    }

    public Boolean isExistField() {
        return existField;
    }

    public void setExistField(Boolean existField) {
        this.existField = existField;
    }

    public Boolean isToOne() {
        return cell.isAssociation() && !cell.isAddress()
            && !AssociationUtil.isToMany(entityInfo, cell.getAssociationBaseName());
    }

    public Boolean isToMany() {
        return cell.isAssociation() && AssociationUtil.isToMany(entityInfo, cell.getAssociationBaseName());
    }

    /**
     * Returns the name of the field on either the current entity or associated entity.
     *
     * @return the name of the field (the direct field on this entity, or the direct field on the associated entity)
     */
    public String getName() {
        if (cell.isAssociation()) {
            return cell.getAssociationFieldName();
        }
        return cell.getName();
    }

    /**
     * Returns the type of entity that this field exists on, either direct or associated.
     *
     * @return this entity if direct, an associated entity if To-One or To-Many.
     */
    public EntityInfo getFieldEntity() {
        if (cell.isAssociation()) {
            return AssociationUtil.getFieldEntity(entityInfo, cell);
        }
        return entityInfo;
    }

    /**
     * Returns the type of the field that this cell represents.
     *
     * This will be the simple data type. If an assoc    public String getName() {
     * if (cell.isAssociation()) {
     * return cell.getAssociationFieldName();
     * }
     * return cell.getName();
     * iation, the data type will be that of the association's field.
     *
     * @return cannot be null, since that would throw an exception in the constructor.
     */
    public Class getFieldType() {
        return setMethod.getParameterTypes()[0];
    }

    /**
     * Returns the value in the cell, converted to the correct type.
     *
     * @return the value that has been converted to the appropriate type, or null if there is an error.
     */
    public Object getValue() throws ParseException {
        return MethodUtil.convertStringToObject(cell.getValue(), getFieldType(), dateTimeFormatter);
    }

    /**
     * Returns the string value in the cell.
     *
     * @return the unmodified value
     */
    public String getStringValue() {
        return cell.getValue();
    }

    /**
     * Calls the appropriate set method on the given SDK-REST entity object in order to send the entity in a REST call.
     *
     * This only applies to direct or compound (address) fields, that have a simple value type.
     *
     * @param entity the entity object to populate
     */
    public void populateFieldOnEntity(BullhornEntity entity) throws ParseException, InvocationTargetException,
        IllegalAccessException {
        if (cell.isAddress()) {
            Address address = (Address) getAssociationMethod.invoke(entity);
            if (address == null) {
                address = new Address();
            }
            setMethod.invoke(address, getValue());
            setAssociationMethod.invoke(entity, address);
        } else {
            setMethod.invoke(entity, getValue());
        }
    }

    /**
     * Calls the appropriate set method on the given SDK-REST entity object in order to set an association.
     *
     * @param entity           the entity object to populate
     * @param associatedEntity the association entity to set
     */
    @SuppressWarnings("unchecked")
    public void populateAssociationOnEntity(BullhornEntity entity, BullhornEntity associatedEntity) throws
        ParseException, InvocationTargetException, IllegalAccessException {
        setMethod.invoke(associatedEntity, getValue());
        if (isToOne()) {
            setAssociationMethod.invoke(entity, associatedEntity);
        } else {
            OneToMany<BullhornEntity> oneToMany = (OneToMany<BullhornEntity>) getAssociationMethod.invoke(entity);
            if (oneToMany == null) {
                oneToMany = new OneToMany<>();
            }
            List<BullhornEntity> associations = oneToMany.getData();
            associations.add(associatedEntity);
            oneToMany.setData(associations);
            setAssociationMethod.invoke(entity, oneToMany);
        }
    }

    /**
     * Calls the appropriate get method on the given SDK-REST entity object in order to get the value from an entity.
     *
     * @param entity the entity object to get the value of the field from
     */
    public Object getValueFromEntity(BullhornEntity entity) throws InvocationTargetException, IllegalAccessException {
        return getMethod.invoke(entity);
    }
}

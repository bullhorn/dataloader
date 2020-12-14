package com.bullhorn.dataloader.rest;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;

import com.bullhorn.dataloader.data.Cell;
import com.bullhorn.dataloader.enums.EntityInfo;
import com.bullhorn.dataloader.util.AssociationUtil;
import com.bullhorn.dataloader.util.MethodUtil;
import com.bullhornsdk.data.model.entity.core.type.BullhornEntity;
import com.bullhornsdk.data.model.entity.embedded.Address;
import com.bullhornsdk.data.model.entity.embedded.OneToMany;
import com.google.common.collect.Lists;

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
    private final Method getMethod;
    private final Method setMethod;
    private final Method getAssociationMethod;
    private final Method setAssociationMethod;

    /**
     * Constructor which takes the type of entity and the raw cell data.
     *
     * Raw cell data may be modified to match the correct field name if the capitalization is incorrect.
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

        // The getMethod/setMethod will be the direct methods on either the current entity or the associated entity.
        // For Example:
        //     'externalID' => <CurrentEntity>:getExternalId() / <CurrentEntity>:setExternalId()
        //   'candidate.id' => Candidate:getId() / Candidate:setId()
        //  'candidates.id' => Candidate:getId() / Candidate:setId()
        this.getMethod = MethodUtil.getGetterMethod(getFieldEntity(), getName());
        this.setMethod = MethodUtil.getSetterMethod(getFieldEntity(), getName());
        final String verifiedFieldName = MethodUtil.getFieldNameFromMethod(getMethod);

        // For all non-direct fields, store the get/set methods for the association, such as getAddress()/setAddress()
        if (cell.isAssociation()) {
            this.getAssociationMethod = MethodUtil.getGetterMethod(entityInfo, cell.getAssociationBaseName());
            this.setAssociationMethod = MethodUtil.getSetterMethod(entityInfo, cell.getAssociationBaseName());
            final String verifiedAssociationBaseName = MethodUtil.getFieldNameFromMethod(getAssociationMethod);

            // Correct capitalization mistakes for the association cell
            if (!this.cell.getAssociationBaseName().equals(verifiedAssociationBaseName)
                || !this.cell.getAssociationFieldName().equals(verifiedFieldName)) {
                this.cell.setAssociationNames(verifiedAssociationBaseName, verifiedFieldName);
            }
        } else {
            this.getAssociationMethod = null;
            this.setAssociationMethod = null;

            // Correct capitalization mistakes for the direct cell
            if (!this.cell.getName().equals(verifiedFieldName)) {
                this.cell.setName(verifiedFieldName);
            }
        }
    }

    public Cell getCell() {
        return cell;
    }

    public EntityInfo getEntityInfo() {
        return entityInfo;
    }

    /**
     * Returns the primary or associated entity
     *
     * @param isPrimaryEntity true = use the entity, false = use the associated (field) entity
     */
    public EntityInfo getEntityInfo(Boolean isPrimaryEntity) {
        return isPrimaryEntity ? getEntityInfo() : getFieldEntity();
    }

    Boolean isExistField() {
        return existField;
    }

    void setExistField(Boolean existField) {
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
        return cell.isAssociation() ? cell.getAssociationFieldName() : cell.getName();
    }

    /**
     * Returns the full name if the primaryEntity, the association name otherwise.
     *
     * Consider the column: `person.externalID` on the PersonCustomObjectInstance1 entity:
     * - When looking for existing Person records to check for existence, we need a Person lookup for `externalID=`
     * - When looking for existing PersonCustomObjectInstance1 records, we need a PersonCustomObjectInstance1 lookup for `person.externalID=`
     * The lookup for person records is an association lookup, the lookup for custom objects is a primary lookup.
     *
     * @param isPrimaryEntity true = get entire name of cell, false = get name of association
     */
    public String getName(Boolean isPrimaryEntity) {
        return isPrimaryEntity ? getCell().getName() : getName();
    }

    /**
     * Returns the name of the field that is valid within the field parameter of a Get call.
     *
     * For direct fields, just the name of the field: firstName
     * For compound fields, the name of the field
     * When a lookup for the association (isPrimaryEntity=false), then it is always just the name of the field
     *
     * @param isPrimaryEntity true = use the full name of the field if compound, false = use only the field name
     */
    public String getFieldParameterName(Boolean isPrimaryEntity) {
        return cell.isAssociation() && isPrimaryEntity ? cell.getAssociationBaseName() + "(" + cell.getAssociationFieldName() + ")"
            : getName(isPrimaryEntity);
    }

    /**
     * Returns the type of entity that this field exists on, either direct or associated.
     *
     * @return this entity if direct, an associated entity if To-One or To-Many.
     */
    public EntityInfo getFieldEntity() {
        return cell.isAssociation() ? AssociationUtil.getFieldEntity(entityInfo, cell) : entityInfo;
    }

    /**
     * Returns the type of the field that this cell represents.
     *
     * This will be the simple data type. If an association, the data type will be that of the association's field.
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
     * Given a delimiter, returns the list of unique values, for example:
     *
     * 'Skill1;Skill2;Skill3' = ['Skill1', 'Skill2', 'Skill3']
     * 'Skill1;Skill1' = ['Skill1']
     *
     * @param delimiter the character(s) to split on
     * @return the list of unique values
     */
    public List<String> split(String delimiter) {
        return Lists.newArrayList(getStringValue().split(delimiter)).stream().distinct().collect(Collectors.toList());
    }

    /**
     * Calls the appropriate get method on the given SDK-REST entity object in order to get the value from an entity.
     *
     * For Association fields, behaves differently when given a parent entity vs. the field entity.
     * For Example, when dealing with the Note field: `candidates.id`:
     * - When given a note entity, uses the given delimiter to combine all candidate ID values into one delimited string.
     * - When given an individual Candidate entity, calls getId() method on the Candidate object.
     *
     * @param entity    the entity to pull data from
     * @param delimiter the character(s) to split on
     * @return the string value of this field on the given entity
     */
    public String getStringValueFromEntity(Object entity, String delimiter) throws InvocationTargetException, IllegalAccessException {
        if (cell.isAssociation() && entityInfo.getEntityClass().equals(entity.getClass())) {
            if (isToMany()) {
                List<String> values = new ArrayList<>();
                OneToMany toManyAssociation = (OneToMany) getAssociationMethod.invoke(entity);
                if (toManyAssociation != null) {
                    for (Object association : toManyAssociation.getData()) {
                        Object value = getMethod.invoke(association);
                        if (value != null) {
                            String stringValue = String.valueOf(value);
                            values.add(stringValue);
                        }
                    }
                }
                return String.join(delimiter, values);
            } else {
                Object toOneAssociation = getAssociationMethod.invoke(entity);
                if (toOneAssociation == null) {
                    return "";
                }
                Object value = getMethod.invoke(toOneAssociation);
                return value != null ? String.valueOf(value) : "";
            }
        }

        Object value = getMethod.invoke(entity);
        if (value == null) {
            return "";
        }
        if (DateTime.class.equals(value.getClass())) {
            DateTime dateTime = (DateTime) value;
            return dateTimeFormatter.print(dateTime);
        }
        return String.valueOf(value);
    }

    /**
     * Calls the appropriate set method on the given SDK-REST entity object in order to send the entity in a REST call.
     *
     * This only applies to direct or compound (address) fields that have a simple value type.
     *
     * @param entity the entity object to populate
     */
    public void populateFieldOnEntity(Object entity) throws ParseException, InvocationTargetException, IllegalAccessException {
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
        if (isToMany()) {
            OneToMany<BullhornEntity> oneToMany = (OneToMany<BullhornEntity>) getAssociationMethod.invoke(entity);
            if (oneToMany == null) {
                oneToMany = new OneToMany<>();
            }
            List<BullhornEntity> associations = oneToMany.getData();
            associations.add(associatedEntity);
            oneToMany.setData(associations);
            setAssociationMethod.invoke(entity, oneToMany);
        } else {
            setAssociationMethod.invoke(entity, associatedEntity);
        }
    }

    /**
     * Returns the oneToMany object for a To-Many association.
     *
     * @param entity the entity object to get the association value from.
     */
    public OneToMany getOneToManyFromEntity(BullhornEntity entity) throws InvocationTargetException, IllegalAccessException {
        return (OneToMany) getAssociationMethod.invoke(entity);
    }

    /**
     * Sets a To-Many field of a given entity to the given object.
     *
     * @param entity    the entity object to populate
     * @param oneToMany the OneToMany object for this To-Many field
     */
    public void populateOneToManyOnEntity(BullhornEntity entity, OneToMany oneToMany) throws InvocationTargetException, IllegalAccessException {
        setAssociationMethod.invoke(entity, oneToMany);
    }
}

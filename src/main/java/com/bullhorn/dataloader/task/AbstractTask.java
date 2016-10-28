package com.bullhorn.dataloader.task;

import com.bullhorn.dataloader.meta.EntityInfo;
import com.bullhorn.dataloader.service.Command;
import com.bullhorn.dataloader.service.csv.CsvFileWriter;
import com.bullhorn.dataloader.service.csv.Result;
import com.bullhorn.dataloader.util.ActionTotals;
import com.bullhorn.dataloader.util.PrintUtil;
import com.bullhorn.dataloader.util.PropertyFileUtil;
import com.bullhorn.dataloader.util.StringConsts;
import com.bullhorn.dataloader.util.validation.EntityValidation;
import com.bullhornsdk.data.api.BullhornData;
import com.bullhornsdk.data.exception.RestApiException;
import com.bullhornsdk.data.model.entity.association.AssociationFactory;
import com.bullhornsdk.data.model.entity.association.AssociationField;
import com.bullhornsdk.data.model.entity.association.EntityAssociations;
import com.bullhornsdk.data.model.entity.core.standard.Candidate;
import com.bullhornsdk.data.model.entity.core.standard.Category;
import com.bullhornsdk.data.model.entity.core.standard.ClientContact;
import com.bullhornsdk.data.model.entity.core.standard.ClientCorporation;
import com.bullhornsdk.data.model.entity.core.standard.CorporateUser;
import com.bullhornsdk.data.model.entity.core.standard.JobOrder;
import com.bullhornsdk.data.model.entity.core.standard.Lead;
import com.bullhornsdk.data.model.entity.core.standard.Note;
import com.bullhornsdk.data.model.entity.core.standard.Opportunity;
import com.bullhornsdk.data.model.entity.core.standard.Placement;
import com.bullhornsdk.data.model.entity.core.standard.Tearsheet;
import com.bullhornsdk.data.model.entity.core.type.AssociationEntity;
import com.bullhornsdk.data.model.entity.core.type.BullhornEntity;
import com.bullhornsdk.data.model.entity.core.type.QueryEntity;
import com.bullhornsdk.data.model.entity.core.type.SearchEntity;
import com.bullhornsdk.data.model.enums.BullhornEntityInfo;
import com.bullhornsdk.data.model.parameter.standard.ParamFactory;
import com.bullhornsdk.data.model.response.crud.CrudResponse;
import com.bullhornsdk.data.model.response.crud.Message;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.DateTimeException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public abstract class AbstractTask<A extends AssociationEntity, E extends EntityAssociations, B extends BullhornEntity> implements Runnable {
    static private Map<Class<AssociationEntity>, List<AssociationField<AssociationEntity, BullhornEntity>>> entityClassToAssociationsMap = new HashMap<>();

    protected static AtomicInteger rowProcessedCount = new AtomicInteger(0);
    protected Command command;
    protected Integer rowNumber;
    protected EntityInfo entityInfo;
    protected Class<B> entityClass;
    protected Integer bullhornParentId;
    protected Map<String, String> dataMap;
    protected CsvFileWriter csvWriter;
    protected PropertyFileUtil propertyFileUtil;
    protected BullhornData bullhornData;
    protected PrintUtil printUtil;
    protected ActionTotals actionTotals;

    public AbstractTask(Command command,
                        Integer rowNumber,
                        EntityInfo entityInfo,
                        Map<String, String> dataMap,
                        CsvFileWriter csvWriter,
                        PropertyFileUtil propertyFileUtil,
                        BullhornData bullhornData,
                        PrintUtil printUtil,
                        ActionTotals actionTotals) {
        this.command = command;
        this.rowNumber = rowNumber;
        this.entityInfo = entityInfo;
        this.dataMap = dataMap;
        this.csvWriter = csvWriter;
        this.propertyFileUtil = propertyFileUtil;
        this.bullhornData = bullhornData;
        this.printUtil = printUtil;
        this.actionTotals = actionTotals;
    }

    protected void init() {
        entityClass = entityInfo.getBullhornEntityInfo().getType();
    }

    protected void addParentEntityIDtoDataMap() {
        dataMap.put(StringConsts.PARENT_ENTITY_ID, bullhornParentId.toString());
    }

    protected void writeToResultCSV(Result result) {
        int attempts = 0;
        while (attempts < 3) {
            try {
                csvWriter.writeRow(dataMap.values().toArray(new String[0]), result);
                updateActionTotals(result);
                updateRowProcessedCounts();
                break;
            } catch (IOException e) {
                printUtil.printAndLog(e);
                attempts++;
            }
        }
    }

    protected void updateRowProcessedCounts() {
        rowProcessedCount.incrementAndGet();
        if (rowProcessedCount.intValue() % 111 == 0) {
            printUtil.printAndLog("Processed: " + NumberFormat.getNumberInstance(Locale.US).format(rowProcessedCount) + " records.");
        }
    }

    private void updateActionTotals(Result result) {
        actionTotals.incrementActionTotal(result.getAction());
    }

    protected Result handleFailure(Exception e) {
        printUtil.printAndLog(e);
        return Result.Failure(e);
    }

    protected Result handleFailure(Exception e, Integer entityID) {
        printUtil.printAndLog(e);
        if (entityID != null) {
            return Result.Failure(e, entityID);
        }
        return Result.Failure(e);
    }

    protected <S extends SearchEntity> List<B> searchForEntity(String field, String value, Class fieldType, Class<B> fieldEntityClass, Set<String> fieldsToReturn) {
        String query = getQueryStatement(field, value, fieldType, fieldEntityClass);
        fieldsToReturn = fieldsToReturn == null ? Sets.newHashSet("id") : fieldsToReturn;
        return (List<B>) bullhornData.search((Class<S>) fieldEntityClass, query, fieldsToReturn, ParamFactory.searchParams()).getData();
    }

    protected <Q extends QueryEntity> List<B> queryForEntity(String field, String value, Class fieldType, Class<B> fieldEntityClass, Set<String> fieldsToReturn) {
        String where = getWhereStatement(field, value, fieldType);
        fieldsToReturn = fieldsToReturn == null ? Sets.newHashSet("id") : fieldsToReturn;
        return (List<B>) bullhornData.query((Class<Q>) fieldEntityClass, where, fieldsToReturn, ParamFactory.queryParams()).getData();
    }

    protected String getQueryStatement(String field, String value, Class fieldType, Class<B> fieldEntityClass) {
        if (fieldEntityClass.equals(Note.class) && field.equals(StringConsts.ID)) {
            field = StringConsts.NOTE_ID;
        }

        if (Integer.class.equals(fieldType) || BigDecimal.class.equals(fieldType) || Double.class.equals(fieldType) || Boolean.class.equals(fieldType)) {
            return field + ":" + value;
        } else if (DateTime.class.equals(fieldType) || String.class.equals(fieldType)) {
            return field + ":\"" + value + "\"";
        } else {
            throw new RestApiException("Row " + rowNumber + ": Failed to create lucene search string for: '" + field + "' with unsupported field type: " + fieldType);
        }
    }

    protected List<B> findEntityList(Map<String, String> entityExistFieldsMap) {
        if (!entityExistFieldsMap.isEmpty()) {
            if (SearchEntity.class.isAssignableFrom(entityClass)) {
                return searchForEntity(entityExistFieldsMap);
            } else {
                return queryForEntity(entityExistFieldsMap);
            }
        }

        return new ArrayList<B>();
    }

    private <S extends SearchEntity> List<B> searchForEntity(Map<String, String> entityExistFieldsMap) {
        String query = entityExistFieldsMap.keySet().stream().map(n -> getQueryStatement(n, entityExistFieldsMap.get(n), getFieldType(entityClass, n, n), getFieldEntityClass(n))).collect(Collectors.joining(" AND "));
        return (List<B>) bullhornData.search((Class<S>) entityClass, query, Sets.newHashSet("id"), ParamFactory.searchParams()).getData();
    }

    private <Q extends QueryEntity> List<B> queryForEntity(Map<String, String> entityExistFieldsMap) {
        String query = entityExistFieldsMap.keySet().stream().map(n -> getWhereStatement(n, entityExistFieldsMap.get(n), getFieldType(entityClass, n, n))).collect(Collectors.joining(" AND "));
        return (List<B>) bullhornData.query((Class<Q>) entityClass, query, Sets.newHashSet("id"), ParamFactory.queryParams()).getData();
    }

    protected String getWhereStatement(String field, String value, Class fieldType) {
        if (Integer.class.equals(fieldType) || BigDecimal.class.equals(fieldType) || Double.class.equals(fieldType)) {
            return field + "=" + value;
        } else if (Boolean.class.equals(fieldType)) {
            return field + "=" + getBooleanWhereStatement(value);
        } else if (String.class.equals(fieldType)) {
            return field + "='" + value + "'";
        } else if (DateTime.class.equals(fieldType)) {
            return field + "=" + getDateQuery(value);
        } else {
            throw new RestApiException("Row " + rowNumber + ": Failed to create query where clause for: '" + field + "' with unsupported field type: " + fieldType);
        }
    }

    protected String getBooleanWhereStatement(String value) {
        if (value.equals("1")) {
            return "true";
        } else {
            return Boolean.toString(Boolean.valueOf(value));
        }
    }

    protected String getDateQuery(String value) {
        if (EntityValidation.isCustomObject(entityInfo.getEntityName())){
            DateTimeFormatter formatter = propertyFileUtil.getDateParser();
            DateTime dateTime = formatter.parseDateTime(value);
            return String.valueOf(dateTime.toDate().getTime());
        } else {
            DateTimeFormatter formatter = propertyFileUtil.getDateParser();
            DateTime dateTime = formatter.parseDateTime(value);
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
            return df.format(dateTime.toDate());
        }
    }

    protected Map<String, String> getEntityExistFieldsMap() throws IOException {
        Map<String, String> entityExistFieldsMap = new HashMap<>();

        Optional<List<String>> existFields = propertyFileUtil.getEntityExistFields(entityClass.getSimpleName());
        if (existFields.isPresent()) {
            for (String existField : existFields.get()) {
                entityExistFieldsMap.put(existField, dataMap.get(existField));
            }
        }

        return entityExistFieldsMap;
    }

    protected Object convertStringToClass(Method method, String value) throws ParseException {
        Class convertToClass = method.getParameterTypes()[0];
        value = value.trim();

        if (String.class.equals(convertToClass)) {
            return value;
        } else if (Integer.class.equals(convertToClass)) {
            if (StringUtils.isEmpty(value)) {
                return 0;
            }
            return Integer.parseInt(value);
        } else if (Double.class.equals(convertToClass)) {
            if (StringUtils.isEmpty(value)) {
                return 0.0;
            }
            return Double.parseDouble(value);
        } else if (Boolean.class.equals(convertToClass)) {
            if (StringUtils.isEmpty(value)) {
                return Boolean.parseBoolean(null);
            }
            return Boolean.parseBoolean(value);
        } else if (DateTime.class.equals(convertToClass)) {
            DateTimeFormatter formatter = propertyFileUtil.getDateParser();
            if (StringUtils.isEmpty(value)) {
                throw new DateTimeException("Row " + rowNumber + ": Cannot set date value to null");
            }
            return (DateTime) formatter.parseDateTime(value);
        } else if (BigDecimal.class.equals(convertToClass)) {
            DecimalFormat decimalFormat = new DecimalFormat();
            decimalFormat.setParseBigDecimal(true);
            if (StringUtils.isEmpty(value)) {
                return (BigDecimal) decimalFormat.parse(String.valueOf(0.0));
            }
            return (BigDecimal) decimalFormat.parse(value);
        }
        return null;
    }

    /**
     * Returns the type of the given field on the given entity
     *
     * @param fieldType The type of the field if it already is known, otherwise the type of the parent
     * @param field The name of the field, like: 'commentingPerson.id', otherwise just the fieldName itself
     * @param fieldName The part of the field after the '.', like: 'id'
     * @return The class type of the field retrieved from the SDK-REST object.
     */
    protected Class getFieldType(Class<B> fieldType, String field, String fieldName) {
        if (fieldName.indexOf(".") > -1) {
            fieldType = BullhornEntityInfo.getTypeFromName(fieldName.substring(0, fieldName.indexOf("."))).getType();
            fieldName = fieldName.substring(fieldName.indexOf(".") + 1);
        }
        String getMethodName = "get" + fieldName;
        List<Method> methods = Arrays.asList(fieldType.getMethods()).stream().filter(n -> getMethodName.equalsIgnoreCase(n.getName())).collect(Collectors.toList());
        if (methods.isEmpty()) {
            throw new RestApiException("Row " + rowNumber + ": '" + field + "': '" + fieldName + "' does not exist on " + fieldType.getSimpleName());
        }

        return methods.get(0).getReturnType();
    }

    /**
     * Returns the Bullhorn entity class for the given field on the given entity
     *
     * @param field The name of the field, like: 'commentingPerson.id', otherwise just the fieldName itself
     * @return The Bullhorn entity class that the field is on
     */
    protected Class<B> getFieldEntityClass(String field) {
        if (field.contains(".")) {
            return BullhornEntityInfo.getTypeFromName(field.substring(0, field.indexOf("."))).getType();
        } else {
            return entityClass;
        }
    }

    protected void checkForRestSdkErrorMessages(CrudResponse response) {
        if (response != null && !response.getMessages().isEmpty() && response.getChangedEntityId() == null) {
            StringBuilder sb = new StringBuilder();
            for (Message message : response.getMessages()) {
                sb.append("\tError occurred on field " + message.getPropertyName() + " due to the following: " + message.getDetailMessage());
                sb.append("\n");
            }
            throw new RestApiException("Row " + rowNumber + ": Error occurred when making " + response.getChangeType().toString() + " REST call:\n" + sb.toString());
        }
    }

    protected void checkForRequiredFieldsError(RestApiException e) {
        if (e.getMessage().indexOf("\"type\" : \"DUPLICATE_VALUE\"") > -1 && e.getMessage().indexOf("\"propertyName\" : null") > -1) {
            throw new RestApiException("Missing required fields for " + entityClass.getSimpleName() + ".");
        } else {
            throw e;
        }
    }

    /**
     * populates a field on an entity using reflection
     *
     * @param field     field to populate
     * @param value     value to populate field with
     * @param entity    the entity to populate
     * @param methodMap map of set methods on entity
     */
    protected void populateFieldOnEntity(String field, String value, Object entity, Map<String, Method> methodMap) throws ParseException, InvocationTargetException, IllegalAccessException {
        Method method = methodMap.get(field.toLowerCase());
        if (method == null) {
            throw new RestApiException("Row " + rowNumber + ": Invalid field: '" + field + "' does not exist on " + entity.getClass().getSimpleName());
        }

        if (isAddressField(field) && methodMap.containsKey("address")) {
            throw new RestApiException("Row " + rowNumber + ": Invalid address field format: '" + field + "' Must use 'address." + field + "' in csv header" );
        }

        if (value != null) {
            method.invoke(entity, convertStringToClass(method, value));
        }
    }

    private boolean isAddressField(String field) {
        List<String> addressFields = Arrays.asList("address1", "address2", "city", "state", "zip", "countryid", "countryname");
        return addressFields.indexOf(field.toLowerCase()) > -1;
    }

    protected String getCamelCasedClassToString() {
        return entityClass.getSimpleName().substring(0, 1).toLowerCase() + entityClass.getSimpleName().substring(1);
    }

    protected static synchronized List<AssociationField<AssociationEntity, BullhornEntity>> getAssociationFields(Class<AssociationEntity> entityClass) {
        try {
            if (entityClassToAssociationsMap.containsKey(entityClass)) {
                return entityClassToAssociationsMap.get(entityClass);
            } else {
                EntityAssociations entityAssociations = getEntityAssociations((Class<AssociationEntity>) entityClass);
                List<AssociationField<AssociationEntity, BullhornEntity>> associationFields = entityAssociations.allAssociations();
                entityClassToAssociationsMap.put((Class<AssociationEntity>) entityClass, associationFields);
                return associationFields;
            }
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    private static synchronized EntityAssociations getEntityAssociations(Class entityClass) {
        return (entityClass == Candidate.class ? AssociationFactory.candidateAssociations() :
            (entityClass == Category.class ? AssociationFactory.categoryAssociations() :
                (entityClass == ClientContact.class ? AssociationFactory.clientContactAssociations() :
                    (entityClass == ClientCorporation.class ? AssociationFactory.clientCorporationAssociations() :
                        (entityClass == CorporateUser.class ? AssociationFactory.corporateUserAssociations() :
                            (entityClass == JobOrder.class ? AssociationFactory.jobOrderAssociations() :
                                (entityClass == Note.class ? AssociationFactory.noteAssociations() :
                                    (entityClass == Placement.class ? AssociationFactory.placementAssociations() :
                                        (entityClass == Opportunity.class ? AssociationFactory.opportunityAssociations() :
                                            (entityClass == Lead.class ? AssociationFactory.leadAssociations() :
                                                entityClass == Tearsheet.class ? AssociationFactory.tearsheetAssociations() : null))))))))));
    }
}

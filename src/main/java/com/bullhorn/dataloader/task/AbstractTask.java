package com.bullhorn.dataloader.task;

import com.bullhorn.dataloader.consts.TaskConsts;
import com.bullhorn.dataloader.meta.EntityInfo;
import com.bullhorn.dataloader.service.Command;
import com.bullhorn.dataloader.service.csv.CsvFileWriter;
import com.bullhorn.dataloader.service.csv.Result;
import com.bullhorn.dataloader.util.ActionTotals;
import com.bullhorn.dataloader.util.PrintUtil;
import com.bullhorn.dataloader.util.PropertyFileUtil;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public abstract class AbstractTask<A extends AssociationEntity, E extends EntityAssociations, B extends BullhornEntity> implements Runnable, TaskConsts {

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
                        LinkedHashMap<String, String> dataMap,
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
        dataMap.put(TaskConsts.PARENT_ENTITY_ID, bullhornParentId.toString());
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

    protected <S extends SearchEntity> List<B> searchForEntity(String field, String value, Class fieldType, Class<B> entityClass, Set<String> fieldsToReturn) {
        String query = getQueryStatement(field, value, fieldType);
        fieldsToReturn = fieldsToReturn == null ? Sets.newHashSet("id") : fieldsToReturn;
        return (List<B>) bullhornData.search((Class<S>) entityClass, query, fieldsToReturn, ParamFactory.searchParams()).getData();
    }

    protected <Q extends QueryEntity> List<B> queryForEntity(String field, String value, Class fieldType, Class<B> entityClass, Set<String> fieldsToReturn) {
        String where = getWhereStatement(field, value, fieldType);
        fieldsToReturn = fieldsToReturn == null ? Sets.newHashSet("id") : fieldsToReturn;
        return (List<B>) bullhornData.query((Class<Q>) entityClass, where, fieldsToReturn, ParamFactory.queryParams()).getData();
    }

    protected String getQueryStatement(String field, String value, Class fieldType) {
        if (Integer.class.equals(fieldType) || BigDecimal.class.equals(fieldType) || Double.class.equals(fieldType)) {
            return field + ":" + value;
        } else if (DateTime.class.equals(fieldType) || String.class.equals(fieldType)) {
            return field + ":\"" + value + "\"";
        } else {
            throw new RestApiException("Row " + rowNumber + ": Failed to create lucene search string for: '" + field + "' with unsupported field type: " + fieldType);
        }
    }

    protected List<B> findEntityList() throws IOException {
        Map<String, String> entityExistFieldsMap = getEntityExistFieldsMap();

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
        String query = entityExistFieldsMap.keySet().stream().map(n -> getQueryStatement(n, entityExistFieldsMap.get(n), getFieldType(entityClass, n))).collect(Collectors.joining(" AND "));
        return (List<B>) bullhornData.search((Class<S>) entityClass, query, Sets.newHashSet("id"), ParamFactory.searchParams()).getData();
    }

    private <Q extends QueryEntity> List<B> queryForEntity(Map<String, String> entityExistFieldsMap) {
        String query = entityExistFieldsMap.keySet().stream().map(n -> getWhereStatement(n, entityExistFieldsMap.get(n), getFieldType(entityClass, n))).collect(Collectors.joining(" AND "));
        return (List<B>) bullhornData.query((Class<Q>) entityClass, query, Sets.newHashSet("id"), ParamFactory.queryParams()).getData();
    }

    protected String getWhereStatement(String field, String value, Class fieldType) {
        if (Integer.class.equals(fieldType) || BigDecimal.class.equals(fieldType) || Double.class.equals(fieldType)) {
            return field + "=" + value;
        } else if (String.class.equals(fieldType)) {
            return field + "='" + value + "'";
        } else if (DateTime.class.equals(fieldType)) {
            return field + "=" + getDateQuery(value);
        } else {
            throw new RestApiException("Row " + rowNumber + ": Failed to create query where clause for: '" + field + "' with unsupported field type: " + fieldType);
        }
    }

    private String getDateQuery(String value) {
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
        if (StringUtils.isEmpty(value)) {
            return null;
        }
        Class convertToClass = method.getParameterTypes()[0];
        value = value.trim();
        if (String.class.equals(convertToClass)) {
            return value;
        } else if (Integer.class.equals(convertToClass)) {
            return Integer.parseInt(value);
        } else if (Double.class.equals(convertToClass)) {
            return Double.parseDouble(value);
        } else if (Boolean.class.equals(convertToClass)) {
            return Boolean.parseBoolean(value);
        } else if (DateTime.class.equals(convertToClass)) {
            DateTimeFormatter formatter = propertyFileUtil.getDateParser();
            return (DateTime) formatter.parseDateTime(value);
        } else if (BigDecimal.class.equals(convertToClass)) {
            DecimalFormat decimalFormat = new DecimalFormat();
            decimalFormat.setParseBigDecimal(true);
            return (BigDecimal) decimalFormat.parse(value);
        }
        return null;
    }

    protected Class getFieldType(Class<B> toOneEntityClass, String fieldName) {
        if (fieldName.indexOf(".") > -1) {
            toOneEntityClass = BullhornEntityInfo.getTypeFromName(fieldName.substring(0, fieldName.indexOf("."))).getType();
            fieldName = fieldName.substring(fieldName.indexOf(".") + 1);
        }
        String getMethodName = "get" + fieldName;
        List<Method> methods = Arrays.asList(toOneEntityClass.getMethods()).stream().filter(n -> getMethodName.equalsIgnoreCase(n.getName())).collect(Collectors.toList());
        if (methods.isEmpty()) {
            throw new RestApiException("Row " + rowNumber + ": To-One Association field: '" + fieldName + "' does not exist on " + toOneEntityClass.getSimpleName());
        }

        return methods.get(0).getReturnType();
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

        if (value != null && !"".equalsIgnoreCase(value)) {
            method.invoke(entity, convertStringToClass(method, value));
        }
    }

    protected String getCamelCasedClassToString() {
        return entityClass.getSimpleName().substring(0, 1).toLowerCase() + entityClass.getSimpleName().substring(1);
    }

    protected List<AssociationField<A, B>> getAssociationFields(Class<B> associationClass) {
        try {
            E entityAssociations = getEntityAssociations((Class<A>) associationClass);
            return entityAssociations.allAssociations();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    private E getEntityAssociations(Class<A> entityClass) {
        return (entityClass == Candidate.class ? (E) AssociationFactory.candidateAssociations() : (entityClass == Category.class ? (E) AssociationFactory.categoryAssociations() : (entityClass == ClientContact.class ? (E) AssociationFactory.clientContactAssociations() : (entityClass == ClientCorporation.class ? (E) AssociationFactory.clientCorporationAssociations() : (entityClass == CorporateUser.class ? (E) AssociationFactory.corporateUserAssociations() : (entityClass == JobOrder.class ? (E) AssociationFactory.jobOrderAssociations() : (entityClass == Note.class ? (E) AssociationFactory.noteAssociations() : (entityClass == Placement.class ? (E) AssociationFactory.placementAssociations() : (entityClass == Opportunity.class ? (E) AssociationFactory.opportunityAssociations() : (entityClass == Lead.class ? (E) AssociationFactory.leadAssociations() : entityClass == Tearsheet.class ? (E) AssociationFactory.tearsheetAssociations() : null))))))))));
    }
}

package com.bullhorn.dataloader.util;

import com.bullhornsdk.data.api.BullhornData;
import com.bullhornsdk.data.model.entity.core.type.BullhornEntity;
import com.bullhornsdk.data.model.entity.meta.Field;
import com.bullhornsdk.data.model.entity.meta.MetaData;
import com.bullhornsdk.data.model.enums.BullhornEntityInfo;
import com.bullhornsdk.data.model.enums.MetaParameter;
import com.bullhornsdk.data.util.ReadOnly;
import com.csvreader.CsvWriter;
import com.google.common.collect.Sets;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class TemplateUtil<B extends BullhornEntity> {

    private final Set<String> compositeTypes = Sets.newHashSet("address");
    private BullhornData bullhornData;

    public TemplateUtil(BullhornData bullhornData) {
        this.bullhornData = bullhornData;
    }

    public void writeExampleEntityCsv(String entity) throws IOException, ClassNotFoundException {
        Set<Field> metaFieldSet = getMetaFieldSet(entity);

        ArrayList<String> headers = new ArrayList<>();
        ArrayList<String> dataTypes = new ArrayList<>();
        populateDataTypes(entity, metaFieldSet, headers, dataTypes);

        writeToCsv(entity, headers, dataTypes);
    }

    private Set<Field> getMetaFieldSet(String entity) {
        MetaData<B> metaData = bullhornData.getMetaData(BullhornEntityInfo.getTypeFromName(entity).getType(), MetaParameter.FULL, null);
        Set<Field> metaFieldSet = new HashSet<>(metaData.getFields());
        Set<Field> associationFields = metaFieldSet.stream().filter(n -> n.getAssociatedEntity() != null).collect(Collectors.toSet());
        addAssociatedFields(metaFieldSet, associationFields);
        return metaFieldSet;
    }

    private void writeToCsv(String entity, ArrayList<String> headers, ArrayList<String> dataTypes) throws IOException {
        CsvWriter csvWriter = new CsvWriter(entity + "Example.csv");
        csvWriter.writeRecord(headers.toArray(new String[0]));
        csvWriter.writeRecord(dataTypes.toArray(new String[0]));
        csvWriter.flush();
        csvWriter.close();
    }

    protected void populateDataTypes(String entity, Set<Field> metaFieldSet, ArrayList<String> headers, ArrayList<String> dataTypes) throws IOException, ClassNotFoundException {
        final HashSet<String> methodSet = getEntityFields(entity);

        for (Field field : metaFieldSet) {
            if ((methodSet.contains(field.getName().toLowerCase()) && !field.getName().contains("."))) {
                if (!isCompositeType(field)) {
                    if (StringConsts.TO_MANY.equalsIgnoreCase(field.getType()) || StringConsts.TO_ONE.equalsIgnoreCase(field.getType())) {
                        field.setName(field.getName() + ".id");
                        field.setDataType("Integer");
                    }
                    headers.add(field.getName());
                    dataTypes.add(field.getDataType());
                } else if (isCompositeType(field)) {
                    List<Method> compositeMethodList = getCompositeMethodList(entity, field);
                    compositeMethodList.stream().forEach(n -> headers.add(getCompositeHeaderName(n, field)));
                    compositeMethodList.stream().forEach(n -> dataTypes.add(n.getReturnType().getSimpleName()));
                }
            }
        }
    }

    private HashSet<String> getEntityFields(String entity) throws ClassNotFoundException {
        final HashSet<String> methodSet = new HashSet<>();
        final Class entityClass = BullhornEntityInfo.getTypeFromName(entity).getType();

        for (Method method : Arrays.asList(entityClass.getMethods())) {
            if ("set".equalsIgnoreCase(method.getName().substring(0, 3)) && !method.isAnnotationPresent(ReadOnly.class)) {
                methodSet.add(method.getName().substring(3).toLowerCase());
            }
        }
        return methodSet;
    }

    private String getCompositeHeaderName(Method method, Field field) {
        return field.getName() + "." + method.getName().substring(3, 4).toLowerCase() + method.getName().substring(4);
    }

    private List<Method> getCompositeMethodList(String entity, Field field) {
        Class compositeClass = getGetMethod(BullhornEntityInfo.getTypeFromName(entity).getType(), field.getName());
        List<Method> methodList = new ArrayList<>();
        for (Method method : Arrays.asList(compositeClass.getMethods())) {
            if ("get".equalsIgnoreCase(method.getName().substring(0, 3))
                && !"getAdditionalProperties".equalsIgnoreCase(method.getName())
                && !"getClass".equalsIgnoreCase(method.getName())) {
                methodList.add(method);
            }
        }
        return methodList;
    }

    protected Class getGetMethod(Class<B> toOneEntityClass, String fieldName) {
        String getMethodName = "get" + fieldName;
        return Arrays.asList(toOneEntityClass.getMethods()).stream().filter(n -> getMethodName.equalsIgnoreCase(n.getName())).collect(Collectors.toList()).get(0).getReturnType();
    }

    protected void addAssociatedFields(Set<Field> metaFieldSet, Set<Field> associationFields) {
        for (Field field : associationFields) {
            field.getAssociatedEntity().getFields().stream().forEach(n -> n.setName(field.getName() + "." + n.getName()));
            addExternalIDWhenExists(field);
            metaFieldSet.addAll(field.getAssociatedEntity().getFields());
        }
    }

    protected void addExternalIDWhenExists(Field field) {
        try {
            if (BullhornEntityInfo.getTypeFromName(field.getOptionsType()).getType().getMethod("getExternalID") != null) {
                Field externalIdField = new Field();
                externalIdField.setName(field.getName() + ".externalID");
                externalIdField.setDataType("String");
                List<Field> newFieldList = new ArrayList<>();
                newFieldList.add(externalIdField);
                newFieldList.addAll(field.getAssociatedEntity().getFields());
                field.getAssociatedEntity().setFields(newFieldList);
            }
        } catch (NoSuchMethodException e) {
        } catch (IllegalArgumentException e) {
        }
    }

    protected boolean hasId(Set<Field> metaFieldSet, String column) {
        return metaFieldSet.stream().map(n -> n.getName()).anyMatch(n -> n.equalsIgnoreCase(column + ".id"));
    }

    protected boolean isCompositeType(Field field) {
        return "COMPOSITE".equalsIgnoreCase(field.getType());
    }
}

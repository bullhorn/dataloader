package com.bullhorn.dataloader.util;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.bullhorn.dataloader.data.CsvFileReader;
import com.bullhorn.dataloader.enums.EntityInfo;
import com.bullhorn.dataloader.rest.RestApi;
import com.bullhornsdk.data.model.entity.core.type.BullhornEntity;
import com.bullhornsdk.data.model.entity.meta.Field;
import com.bullhornsdk.data.model.entity.meta.MetaData;
import com.bullhornsdk.data.model.enums.BullhornEntityInfo;
import com.bullhornsdk.data.model.enums.MetaParameter;
import com.bullhornsdk.data.util.ReadOnly;
import com.csvreader.CsvWriter;
import com.google.common.collect.Sets;

/**
 * Utility for querying meta and creating an example file that includes all potential fields for an entity.
 */
public class TemplateUtil<B extends BullhornEntity> {

    private final RestApi restApi;
    private final PropertyFileUtil propertyFileUtil;
    private final PrintUtil printUtil;

    public TemplateUtil(RestApi restApi, PropertyFileUtil propertyFileUtil, PrintUtil printUtil) {
        this.restApi = restApi;
        this.propertyFileUtil = propertyFileUtil;
        this.printUtil = printUtil;
    }

    public void writeExampleEntityCsv(EntityInfo entityInfo) throws IOException {
        Set<Field> metaFieldSet = getMetaFieldSet(entityInfo);
        List<String> headers = new ArrayList<>();
        List<String> dataTypes = new ArrayList<>();
        populateDataTypes(entityInfo, metaFieldSet, headers, dataTypes);

        CsvWriter csvWriter = new CsvWriter(entityInfo.getEntityName() + "Example.csv");
        csvWriter.writeRecord(headers.toArray(new String[0]));
        csvWriter.writeRecord(dataTypes.toArray(new String[0]));
        csvWriter.flush();
        csvWriter.close();
    }

    public void compareMetaToExampleFile(EntityInfo entityInfo, String exampleFile) throws IOException {
        Set<Field> metaFieldSet = getMetaFieldSet(entityInfo);
        List<String> restMetaHeaders = new ArrayList<>();
        List<String> dataTypes = new ArrayList<>();
        populateDataTypes(entityInfo, metaFieldSet, restMetaHeaders, dataTypes);

        CsvFileReader csvFileReader = new CsvFileReader(exampleFile, propertyFileUtil, printUtil);
        List<String> exampleFileHeaders = new ArrayList<>(Arrays.asList(csvFileReader.getHeaders()));
        List<String> exampleHeaders = exampleFileHeaders.stream().map(h -> h.replaceAll("\\..+", "")).distinct().collect(Collectors.toList());
        List<String> restHeaders = restMetaHeaders.stream().map(h -> h.replaceAll("\\..+", "")).distinct().collect(Collectors.toList());

        List<String> missingFields = restHeaders.stream().filter(h -> !exampleHeaders.contains(h)).sorted().collect(Collectors.toList());
        if (!missingFields.isEmpty()) {
            printUtil.printAndLog("\nHeaders in Rest that are not in example file:");
            missingFields.forEach(missingField -> printUtil.printAndLog(" " + (missingFields.indexOf(missingField) + 1) + ". " + missingField));
        }

        List<String> extraFields = exampleHeaders.stream().filter(h -> !restHeaders.contains(h)).sorted().collect(Collectors.toList());
        if (!extraFields.isEmpty()) {
            printUtil.printAndLog("\nHeaders in example file that are not in Rest:");
            extraFields.forEach(extraField -> printUtil.printAndLog(" " + (extraFields.indexOf(extraField) + 1) + ". " + extraField));
        }
    }

    @SuppressWarnings("unchecked")
    private Set<Field> getMetaFieldSet(EntityInfo entityInfo) {
        MetaData metaData = restApi.getMetaData(entityInfo.getEntityClass(), MetaParameter.FULL, Sets.newHashSet(StringConsts.ALL_FIELDS));
        Set<Field> metaFieldSet = Sets.newHashSet(metaData.getFields());
        Set<Field> associationFields = metaFieldSet.stream()
            .filter(n -> n.getAssociatedEntity() != null)
            .collect(Collectors.toSet());
        addAssociatedFields(metaFieldSet, associationFields);
        return metaFieldSet;
    }

    void populateDataTypes(EntityInfo entityInfo,
                           Set<Field> metaFieldSet,
                           List<String> headers,
                           List<String> dataTypes) {
        Set<String> methodSet = getEntityFields(entityInfo);

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
                    List<Method> compositeMethodList = getCompositeMethodList(entityInfo, field);
                    compositeMethodList.forEach(n -> headers.add(getCompositeHeaderName(n, field)));
                    compositeMethodList.forEach(n -> dataTypes.add(n.getReturnType().getSimpleName()));
                }
            }
        }
    }

    private Set<String> getEntityFields(EntityInfo entityInfo) {
        Set<String> methodSet = Sets.newHashSet();
        Map<String, Method> methodMap = MethodUtil.getSetterMethodMap(entityInfo.getEntityClass());
        for (Map.Entry<String, Method> entry : methodMap.entrySet()) {
            if (!entry.getValue().isAnnotationPresent(ReadOnly.class)) {
                methodSet.add(entry.getKey());
            }
        }
        return methodSet;
    }

    private String getCompositeHeaderName(Method method, Field field) {
        return field.getName() + "." + method.getName().substring(3, 4).toLowerCase() + method.getName().substring(4);
    }

    @SuppressWarnings("unchecked")
    private List<Method> getCompositeMethodList(EntityInfo entityInfo, Field field) {
        Class compositeClass = getGetMethod(entityInfo.getEntityClass(), field.getName());
        List<Method> methodList = new ArrayList<>();
        for (Method method : compositeClass.getMethods()) {
            if ("get".equalsIgnoreCase(method.getName().substring(0, 3))
                && !"getAdditionalProperties".equalsIgnoreCase(method.getName())
                && !"getClass".equalsIgnoreCase(method.getName())) {
                methodList.add(method);
            }
        }
        return methodList;
    }

    private Class getGetMethod(Class<B> toOneEntityClass, String fieldName) {
        String getMethodName = "get" + fieldName;
        return Arrays.stream(toOneEntityClass.getMethods())
            .filter(n -> getMethodName.equalsIgnoreCase(n.getName()))
            .collect(Collectors.toList()).get(0).getReturnType();
    }

    void addAssociatedFields(Set<Field> metaFieldSet, Set<Field> associationFields) {
        for (Field field : associationFields) {
            field.getAssociatedEntity().getFields().forEach(n -> n.setName(field.getName() + "." + n.getName()));
            addExternalIdWhenExists(field);
            metaFieldSet.addAll(field.getAssociatedEntity().getFields());
        }
    }

    private void addExternalIdWhenExists(Field field) {
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
        } catch (Exception e) {
            // Ignore errors here
        }
    }

    boolean isCompositeType(Field field) {
        return "COMPOSITE".equalsIgnoreCase(field.getType());
    }
}

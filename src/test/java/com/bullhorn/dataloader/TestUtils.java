package com.bullhorn.dataloader;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.json.JSONObject;
import org.junit.Assert;

import com.bullhorn.dataloader.data.ActionTotals;
import com.bullhorn.dataloader.data.Cell;
import com.bullhorn.dataloader.data.CsvFileWriter;
import com.bullhorn.dataloader.data.Result;
import com.bullhorn.dataloader.data.Row;
import com.bullhorn.dataloader.enums.Command;
import com.bullhorn.dataloader.enums.EntityInfo;
import com.bullhorn.dataloader.rest.Field;
import com.bullhorn.dataloader.rest.Record;
import com.bullhorn.dataloader.util.PropertyFileUtil;
import com.bullhorn.dataloader.util.StringConsts;
import com.bullhornsdk.data.model.entity.core.standard.Country;
import com.bullhornsdk.data.model.entity.core.standard.Person;
import com.bullhornsdk.data.model.entity.core.standard.Skill;
import com.bullhornsdk.data.model.entity.core.type.BullhornEntity;
import com.bullhornsdk.data.model.entity.embedded.OneToMany;
import com.bullhornsdk.data.model.enums.ChangeType;
import com.bullhornsdk.data.model.response.crud.AbstractCrudResponse;
import com.bullhornsdk.data.model.response.crud.Message;
import com.bullhornsdk.data.model.response.list.ListWrapper;
import com.bullhornsdk.data.model.response.list.StandardListWrapper;

/**
 * Utilities used in tests
 */
public class TestUtils {
    /**
     * Given a list of entity objects, this method constructs the list returned by SDK-REST
     *
     * @param entityList The list of entity objects
     * @return The list from the SDK-REST that contains the entities
     */
    @SafeVarargs
    public static <B extends BullhornEntity> List<B> getList(B... entityList) {
        return new ArrayList<>(Arrays.asList(entityList));
    }

    /**
     * Version of getList that casts to a concrete list of BullhornEntity objects, instead of keeping the generic type.
     */
    @SuppressWarnings("unchecked")
    public static <B extends BullhornEntity> List<BullhornEntity> getConcreteList(B... entityList) {
        return TestUtils.getList(entityList);
    }

    /**
     * Given an entity type and list of ids, this method constructs the list returned by SDK-REST
     *
     * @param entityClass The type of entity to create a list wrapper for
     * @param idList      The array of IDs to assign to new entity objects
     * @return The list from the SDK-REST that contains the entities
     */
    public static <B extends BullhornEntity> List<B> getList(Class<B> entityClass, int... idList) throws IllegalAccessException, InstantiationException {
        List<B> list = new ArrayList<>();
        for (int id : idList) {
            B entity = entityClass.newInstance();
            entity.setId(id);
            list.add(entity);
        }
        return list;
    }

    /**
     * Version of getList that casts to a concrete list of BullhornEntity objects, instead of keeping the generic type.
     */
    @SuppressWarnings("unchecked")
    public static <B extends BullhornEntity> List<BullhornEntity> getConcreteList(Class<B> entityClass, int... idList) throws InstantiationException, IllegalAccessException {
        return (List<BullhornEntity>) TestUtils.getList(entityClass, idList);
    }

    /**
     * Given an entity type, the total return count, start and list of ids, this method constructs the list wrapper returned by SDK-REST
     *
     * @param entityClass The type of entity to create a list wrapper for
     * @param start       The first record to start at in the list
     * @param total       The total number of records that match the search criteria
     * @param idList      The array of IDs to assign to new entity objects - count is set to the length of this list
     * @return The listWrapper from the SDK-REST that contains the entity data and count/total/start
     */
    public static <B extends BullhornEntity> ListWrapper<B> getListWrapper(Class<B> entityClass, Integer start, Integer total, int... idList)
        throws IllegalAccessException, InstantiationException {
        ListWrapper<B> listWrapper = new StandardListWrapper<>(TestUtils.getList(entityClass, idList));
        listWrapper.setTotal(total);
        listWrapper.setStart(start);
        return listWrapper;
    }

    /**
     * Given a total count and a list of entities, constructs the OneToMany object for the given entities
     *
     * @param total      The total number of records that exist, even if they are not present in the entityList
     * @param entityList The entities that are present in the data portion of the OneToMany
     * @return The oneToMany object that holds entity associations in the SDK-REST
     */
    @SafeVarargs
    public static <B extends BullhornEntity> OneToMany<B> getOneToMany(Integer total, B... entityList) {
        OneToMany<B> oneToMany = new OneToMany<>(entityList);
        oneToMany.setTotal(total);
        return oneToMany;
    }

    /**
     * Convenience method for mocking a CreateResponse from SDK-REST
     *
     * @param changeType      INSERT, UPDATE, or DELETE
     * @param changedEntityId The id of the entity that has been inserted
     * @return A new create response object
     */
    public static AbstractCrudResponse getResponse(ChangeType changeType, Integer changedEntityId) {
        AbstractCrudResponse response = new AbstractCrudResponse();
        response.setChangeType(changeType.toString());
        response.setChangedEntityId(changedEntityId);
        return response;
    }

    /**
     * Version of getResponse that returns an error
     *
     * @param changeType      INSERT, UPDATE, or DELETE
     * @param changedEntityId The id of the entity that has been inserted
     * @param propertyName    The property with an error
     * @param errorMessage    The error message for the property
     * @return A new create response object
     */
    public static AbstractCrudResponse getResponse(ChangeType changeType, Integer changedEntityId, String propertyName, String errorMessage) {
        AbstractCrudResponse response = getResponse(changeType, changedEntityId);
        Message message = new Message();
        message.setPropertyName(propertyName);
        message.setDetailMessage(errorMessage);
        response.setMessages(Collections.singletonList(message));
        return response;
    }

    /**
     * Verifies that the actionTotalsMock has been called exactly expectedTotal times for the expectedAction, and that
     * all other totals have not been called at all.
     *
     * @param actionTotalsMock The mocked action totals object
     * @param expectedAction   Which one out of the many actions are expected to be incremented
     * @param expectedTotal    The number of rows that are expected to be in the total
     */
    public static void verifyActionTotals(ActionTotals actionTotalsMock, Result.Action expectedAction, Integer expectedTotal) {
        for (Result.Action action : Result.Action.values()) {
            if (action == expectedAction) {
                verify(actionTotalsMock, times(expectedTotal)).incrementActionTotal(action);
            } else {
                verify(actionTotalsMock, never()).incrementActionTotal(action);
            }
        }
    }

    /**
     * Returns the full path to the resource file with the given name
     *
     * @param filename The name of the resource file to locate on disk
     * @return The absolute path to the resource file
     * @throws NullPointerException If the file does not exist
     */
    public static String getResourceFilePath(String filename) {
        URL url = getResourceUrl(filename);
        return new File(url.getFile()).getAbsolutePath();
    }

    private static URL getResourceUrl(String filename) {
        ClassLoader classLoader = TestUtils.class.getClassLoader();
        URL url = classLoader.getResource(filename);
        if (url == null) {
            url = classLoader.getResource("unitTest/" + filename);
            if (url == null) {
                url = classLoader.getResource("integrationTest/" + filename);
            }
        }
        return url;
    }

    /**
     * Given a directory, this will replace all instances of the given text string with another text string for CSV files.
     * Will recursively replace for all subdirectories.
     *
     * @param directory   The directory to recurse through
     * @param findText    The text to find
     * @param replaceText The text to replace the foundText with
     * @throws IOException In case the directory is null
     */
    public static void replaceTextInFiles(File directory, String findText, String replaceText) throws IOException {
        File[] directoryListing = directory.listFiles();
        if (directoryListing != null) {
            for (File file : directoryListing) {
                String extension = FilenameUtils.getExtension(file.getPath());
                if (extension.equalsIgnoreCase(StringConsts.CSV)) {
                    String content = FileUtils.readFileToString(file, "UTF-8");
                    content = content.replaceAll(findText, replaceText);
                    FileUtils.writeStringToFile(file, content, "UTF-8");
                } else if (file.isDirectory()) {
                    replaceTextInFiles(file, findText, replaceText);
                }
            }
        } else {
            throw new IllegalArgumentException("Integration Test Failure: Cannot access the directory: '" + directory + "' for testing.");
        }
    }

    /**
     * Given a results file, this will determine if the results file is for a deletable entity
     *
     * @param file The results file to check
     * @return true if the entity is invalid or can be deleted, false if non-deletable
     */
    public static boolean isResultsFileDeletable(File file) {
        String fileName = file.getName();
        EntityInfo entityInfo = EntityInfo.fromString(fileName.substring(0, fileName.indexOf("_")));
        return entityInfo == null || entityInfo.isDeletable();
    }

    /**
     * Given a directory, this will check that all of the files in the directory have been successfully processed by
     * checking that success files exist in the results directory and failure files do not.
     *
     * @param directory The directory to recurse through
     * @param command   The command used when running DataLoader
     */
    public static void checkResultsFiles(File directory, Command command) {
        File[] directoryListing = directory.listFiles();
        if (directoryListing != null) {
            for (File file : directoryListing) {
                String extension = FilenameUtils.getExtension(file.getPath());
                if (extension.equalsIgnoreCase(StringConsts.CSV)) {
                    checkResultsFile(file, command);
                }
            }
        } else {
            throw new IllegalArgumentException("Integration Test Failure: Cannot access the directory: '" + directory + "' for testing.");
        }
    }

    /**
     * Given a file, this will check that a success and not a failure results file have been created.
     *
     * @param file    The file to check
     * @param command The command used when running DataLoader
     */
    public static void checkResultsFile(File file, Command command) {
        String successFilePath = CsvFileWriter.getResultsFilePath(file.getPath(), command, Result.Status.SUCCESS);
        String failureFilePath = CsvFileWriter.getResultsFilePath(file.getPath(), command, Result.Status.FAILURE);

        File successFile = new File(successFilePath);
        File failureFile = new File(failureFilePath);

        Assert.assertTrue("Verify " + successFilePath + " Exists Failed!", successFile.exists());
        Assert.assertFalse("Verify " + failureFilePath + " Does not Exist Failed!", failureFile.exists());
    }

    /**
     * Checks that the command line output has only the desired action.
     *
     * @param output         the command line output
     * @param expectedAction the action that should be in the output (and only that action)
     */
    public static void checkCommandLineOutput(String output, Result.Action expectedAction) {
        String actionString = expectedAction.toString().toLowerCase();
        Assert.assertFalse("Error messages output during " + actionString + " step", output.contains("ERROR"));
        Assert.assertFalse("Failed to process any records during " + actionString + " step", output.contains("processed: 0"));
        Assert.assertTrue("There are failures during " + actionString + " step", output.contains("failed: 0"));

        if (Result.Action.DELETE.equals(expectedAction)) {
            Assert.assertFalse("No deletes performed", output.contains("deleted: 0"));
        } else if (Result.Action.EXPORT.equals(expectedAction)) {
            Assert.assertFalse("No exports performed", output.contains("exported: 0"));
        } else if (Result.Action.INSERT.equals(expectedAction)) {
            Assert.assertFalse("No inserts performed", output.contains("inserted: 0"));
            Assert.assertTrue("Update performed during " + actionString + " step", output.contains("updated: 0"));
            Assert.assertTrue("Skip performed during " + actionString + " step", output.contains("skipped: 0"));
        } else if (Result.Action.UPDATE.equals(expectedAction)) {
            Assert.assertFalse("No updates performed", output.contains("updated: 0"));
            Assert.assertTrue("Insert performed during " + actionString + " step", output.contains("inserted: 0"));
            Assert.assertTrue("Skip performed during " + actionString + " step", output.contains("skipped: 0"));
        } else if (Result.Action.SKIP.equals(expectedAction)) {
            Assert.assertFalse("No skips performed", output.contains("skipped: 0"));
            Assert.assertTrue("Insert performed during " + actionString + " step", output.contains("inserted: 0"));
            Assert.assertTrue("Update performed during " + actionString + " step", output.contains("updated: 0"));
        }
    }

    /**
     * Given a comma separated list of keys and their expected values, asserts that the values in the object match.
     *
     * @param jsonObject     object to test
     * @param keys           comma separated list of keys, like: "firstName,lastName,email"
     * @param expectedValues comma separated values list, like: "John,Smith,jsmith@bullhorn.com"
     */
    public static void checkJsonObject(JSONObject jsonObject, String keys, String expectedValues) {
        String[] keyArray = keys.split(",");
        String[] valueArray = expectedValues.split(",", -1);
        Assert.assertEquals("Test setup failure: keys/expectedValues provided are not the same length", keyArray.length, valueArray.length);
        for (int i = 0; i < keyArray.length; i++) {
            String key = keyArray[i];
            String value = valueArray[i];
            Assert.assertTrue("Json object is missing field: " + key, jsonObject.has(key));
            Assert.assertEquals("Json object value mismatch. Expected: " + value + " but Actual is: " + jsonObject.getString(key),
                value, jsonObject.getString(key));
        }
    }

    /**
     * Given a comma separated list of headers and values, just as you would see in the CSV file itself, this
     * convenience method constructs the Row object that represents that data.
     *
     * @param headers comma separated header list, like: "firstName,lastName,email"
     * @param values  comma separated values list, like: "John,Smith,jsmith@bullhorn.com"
     * @return the row object that contains these values
     */
    public static Row createRow(String headers, String values) throws IOException {
        String[] headerArray = headers.split(",");
        String[] valueArray = values.split(",");
        return createRow(headerArray, valueArray);
    }

    /**
     * Given a list of headers and values, this convenience method constructs the Row object that represents that data.
     */
    public static Row createRow(String[] headerArray, String[] valueArray) throws IOException {
        if (headerArray.length != valueArray.length) {
            throw new IOException("Test Setup Failure - Create Row called with headers/values mismatching in length: "
                + headerArray.length + " headers, " + valueArray.length + " values.");
        }

        Row row = new Row("path/to/fake/file.csv", 1);
        for (int i = 0; i < headerArray.length; ++i) {
            Cell cell = new Cell(headerArray[i], valueArray[i]);
            row.addCell(cell);
        }
        return row;
    }

    /**
     * Convenience method for creating a record for testing
     *
     * @param entityInfo           the type of entity to create
     * @param headers              comma separated list of fields to fill out in the entity
     * @param values               comma separated list of field values to use when filling in the entity
     * @param propertyFileUtilMock the property file to use when constructing the record
     * @return the newly created record
     */
    public static Record createRecord(EntityInfo entityInfo, String headers, String values,
                                      PropertyFileUtil propertyFileUtilMock) throws IOException {
        Row row = createRow(headers, values);
        return new Record(entityInfo, row, propertyFileUtilMock);
    }

    /**
     * Convenience constructor that builds up any entity object using the provided list of headers/values for the object
     *
     * Example use:
     * Candidate myCandidate = TestUtils.createEntity(EntityInfo.CANDIDATE, "id,firstName,lastName,email", "101,Foo,Bar,foo@bar.com");
     *
     * @param entityInfo           the type of entity to create
     * @param headers              comma separated list of fields to fill out in the entity
     * @param values               comma separated list of field values to use when filling in the entity
     * @param propertyFileUtilMock the property file to use when constructing the record
     * @return the newly created entity with fields filled out
     */
    @SuppressWarnings("unchecked")
    public static <B extends BullhornEntity> B createEntity(EntityInfo entityInfo, String headers, String values,
                                                            PropertyFileUtil propertyFileUtilMock) throws Exception {
        B entity = (B) entityInfo.getEntityClass().newInstance();
        Record record = createRecord(entityInfo, headers, values, propertyFileUtilMock);
        for (Field field : record.getFields()) {
            field.populateFieldOnEntity(entity);
        }
        return entity;
    }

    /**
     * Given a comma separated list of IDs and Names, returns a Country list for testing.
     *
     * @param countryNames comma separated name list, like: "United States,Canada"
     * @param countryIDs   comma separated ID list, like: "1,2"
     * @return the country list that contains these values
     */
    public static List<Country> createCountryList(String countryNames, String countryIDs) throws IOException {
        String[] nameArray = countryNames.split(",");
        String[] idArray = countryIDs.split(",");

        if (nameArray.length != idArray.length) {
            throw new IOException("Test Setup Failure - createCountryList called with Names/IDs mismatching in " +
                "length: " + nameArray.length + " names, " + idArray.length + " IDs.");
        }

        List<Country> countries = new ArrayList<>();
        for (int i = 0; i < nameArray.length; ++i) {
            Country country = new Country();
            country.setId(Integer.valueOf(idArray[i]));
            country.setName(nameArray[i]);
            countries.add(country);
        }
        return countries;
    }

    /**
     * Convenience constructor that builds up the required Person object data.
     *
     * @param id        the id of the person object
     * @param subType   the string subtype, like 'Candidate' or 'CorporateUser'
     * @param isDeleted whether the person is soft-deleted
     * @return the new Person object
     */
    public static Person createPerson(Integer id, String subType, Boolean isDeleted) {
        Person person = new Person(id);
        person.setPersonSubtype(subType);
        person.setIsDeleted(isDeleted);
        return person;
    }

    /**
     * Convenience constructor that builds up the required Skill object data.
     *
     * @param id   the id of the skill
     * @param name the name of the skill
     * @return the new Skill object
     */
    public static Skill createSkill(Integer id, String name) {
        Skill skill = new Skill();
        skill.setId(id);
        skill.setName(name);
        return skill;
    }

    /**
     * Convenience constructor that builds up a meta Field object for testing.
     */
    public static com.bullhornsdk.data.model.entity.meta.Field createField(
        String name, String label, String description, String hint, String type, String dataType) {
        com.bullhornsdk.data.model.entity.meta.Field field = new com.bullhornsdk.data.model.entity.meta.Field();
        field.setName(name);
        field.setLabel(label);
        field.setDescription(description);
        field.setHint(hint);
        field.setType(type);
        field.setDataType(dataType);
        return field;
    }
}

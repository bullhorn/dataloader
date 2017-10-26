package com.bullhorn.dataloader;

import com.bullhorn.dataloader.data.ActionTotals;
import com.bullhorn.dataloader.data.Cell;
import com.bullhorn.dataloader.data.CsvFileWriter;
import com.bullhorn.dataloader.data.Result;
import com.bullhorn.dataloader.data.Row;
import com.bullhorn.dataloader.enums.Command;
import com.bullhornsdk.data.model.entity.core.standard.Country;
import com.bullhornsdk.data.model.entity.core.standard.Person;
import com.bullhornsdk.data.model.entity.core.type.BullhornEntity;
import com.bullhornsdk.data.model.enums.ChangeType;
import com.bullhornsdk.data.model.response.crud.AbstractCrudResponse;
import com.bullhornsdk.data.model.response.crud.Message;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.junit.Assert;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Utilities used in tests
 */
public class TestUtils {
    static private final String CSV = "csv";

    /**
     * Given a list of entity objects, this method constructs the listWrapper returned by SDK-REST
     *
     * @param entityList The list of entity objects
     * @return The listWrapper from the SDK-REST that contains the entities
     */
    @SafeVarargs
    public static <B extends BullhornEntity> List<B> getList(B... entityList) throws IllegalAccessException, InstantiationException {
        return new ArrayList<>(Arrays.asList(entityList));
    }

    /**
     * Given an entity type and list of ids, this method constructs the listWrapper returned by SDK-REST
     *
     * @param entityClass The type of entity to create a list wrapper for
     * @param idList      The array of IDs to assign to new entity objects
     * @return The listWrapper from the SDK-REST that contains the entities
     */
    public static <B extends BullhornEntity> List<B> getList(Class<B> entityClass, Integer... idList) throws IllegalAccessException, InstantiationException {
        List<B> list = new ArrayList<>();
        for (Integer id : idList) {
            B entity = entityClass.newInstance();
            entity.setId(id);
            list.add(entity);
        }
        return list;
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
     * Given a directory, this will replace all instances of the given text string with another text string for files
     * with the given extension.
     *
     * @param directory   The directory to recurse through
     * @param replaceText The text to find
     * @param findText    The text to replace the foundText with
     * @throws IOException In case the directory is null
     */
    public static void replaceTextInFiles(File directory, String replaceText, String findText) throws IOException {
        File[] directoryListing = directory.listFiles();
        if (directoryListing != null) {
            for (File file : directoryListing) {
                String extension = FilenameUtils.getExtension(file.getPath());
                if (extension.equalsIgnoreCase(CSV)) {
                    String content = FileUtils.readFileToString(file, "UTF-8");
                    content = content.replaceAll(findText, replaceText);
                    FileUtils.writeStringToFile(file, content, "UTF-8");
                }
            }
        } else {
            throw new IllegalArgumentException("Integration Test Failure: Cannot access the directory: '" + directory + "' for testing.");
        }
    }

    /**
     * Given a directory, this will check that all of the files in the directory have been successfully processed by
     * checking that success files exist in the results directory and failure files do not.
     *
     * @param directory The directory to recurse through
     * @param command   The command used when running DataLoader
     * @throws IOException In case the directory is null
     */
    public static void checkResultsFiles(File directory, Command command) throws IOException {
        File[] directoryListing = directory.listFiles();
        if (directoryListing != null) {
            for (File file : directoryListing) {
                String extension = FilenameUtils.getExtension(file.getPath());
                if (extension.equalsIgnoreCase(CSV)) {
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
     * @throws IOException In case the file is null
     */
    public static void checkResultsFile(File file, Command command) throws IOException {
        String successFilePath = CsvFileWriter.getResultsFilePath(file.getPath(), command, Result.Status.SUCCESS);
        String failureFilePath = CsvFileWriter.getResultsFilePath(file.getPath(), command, Result.Status.FAILURE);

        File successFile = new File(successFilePath);
        File failureFile = new File(failureFilePath);

        Assert.assertTrue("Verify " + successFilePath + " Exists Failed!", successFile.exists());
        Assert.assertFalse("Verify " + failureFilePath + " Does not Exist Failed!", failureFile.exists());
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

        Row row = new Row(1);
        for (int i = 0; i < headerArray.length; i++) {
            Cell cell = new Cell(headerArray[i], valueArray[i]);
            row.addCell(cell);
        }
        return row;
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
        for (int i = 0; i < nameArray.length; i++) {
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
     * @param id the id of the person object
     * @param subType the string subtype, like 'Candidate' or 'CorporateUser'
     * @param isDeleted whether the person is soft-deleted
     * @return the new Person object
     */
    public static Person createPerson(Integer id, String subType, Boolean isDeleted) {
        Person person = new Person(id);
        person.setPersonSubtype(subType);
        person.setIsDeleted(isDeleted);
        return person;
    }
}

package com.bullhorn.dataloader.util.validation;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import com.bullhorn.dataloader.util.PropertyFileUtil;

public class PropertyValidationTest {

	@Test(expected=IllegalArgumentException.class)
	public void testEmptyUserName() throws IOException {
		final String path = getFilePath("propertyValidationTest/PropertyValidationTest_EmptyUsername.properties");
		final PropertyFileUtil propertyFileUtil = new PropertyFileUtil(path);
	}

	@Test(expected=IllegalArgumentException.class)
	public void testEmptyPassword() throws IOException {
		final String path = getFilePath("propertyValidationTest/PropertyValidationTest_EmptyPassword.properties");
		final PropertyFileUtil propertyFileUtil = new PropertyFileUtil(path);
	}

	@Test(expected=IllegalArgumentException.class)
	public void testEmptyClientId() throws IOException {
		final String path = getFilePath("propertyValidationTest/PropertyValidationTest_EmptyClientId.properties");
		final PropertyFileUtil propertyFileUtil = new PropertyFileUtil(path);
	}

	@Test(expected=IllegalArgumentException.class)
	public void testEmptyClientSecret() throws IOException {
		final String path = getFilePath("propertyValidationTest/PropertyValidationTest_EmptyClientSecret.properties");
		final PropertyFileUtil propertyFileUtil = new PropertyFileUtil(path);
	}

	@Test(expected=IllegalArgumentException.class)
	public void testEmptyAuthorizeUrl() throws IOException {
		final String path = getFilePath("propertyValidationTest/PropertyValidationTest_EmptyAuthorizeUrl.properties");
		final PropertyFileUtil propertyFileUtil = new PropertyFileUtil(path);
	}

	@Test(expected=IllegalArgumentException.class)
	public void testEmptyTokenUrl() throws IOException {
		final String path = getFilePath("propertyValidationTest/PropertyValidationTest_EmptyTokenUrl.properties");
		final PropertyFileUtil propertyFileUtil = new PropertyFileUtil(path);
	}

	@Test(expected=IllegalArgumentException.class)
	public void testEmptyLoginUrl() throws IOException {
		final String path = getFilePath("propertyValidationTest/PropertyValidationTest_EmptyLoginUrl.properties");
		final PropertyFileUtil propertyFileUtil = new PropertyFileUtil(path);
	}

	@Test(expected=IllegalArgumentException.class)
	public void testFrontLoadedEntities_MispelledEntity() throws IOException {
		final String path = getFilePath("propertyValidationTest/PropertyValidationTest_EmptyFrontLoadedEntities.properties");
		final PropertyFileUtil propertyFileUtil = new PropertyFileUtil(path);
	}

	@Test(expected=IllegalArgumentException.class)
	public void testEmptyEntityExistFields() throws IOException {
		final String path = getFilePath("propertyValidationTest/PropertyValidationTest_EmptyEntityExistFields.properties");
		final PropertyFileUtil propertyFileUtil = new PropertyFileUtil(path);
	}

	@Test(expected=IllegalArgumentException.class)
	public void testEmptyListDelimiter() throws IOException {
		final String path = getFilePath("propertyValidationTest/PropertyValidationTest_EmptyListDelimiter.properties");
		final PropertyFileUtil propertyFileUtil = new PropertyFileUtil(path);
	}

	@Test(expected=IllegalArgumentException.class)
	public void testEmptyNumThreads() throws IOException {
		final String path = getFilePath("propertyValidationTest/PropertyValidationTest_EmptyNumThreads.properties");
		final PropertyFileUtil propertyFileUtil = new PropertyFileUtil(path);
	}

	private String getFilePath(String filename) {
		final ClassLoader classLoader = getClass().getClassLoader();
		return new File(classLoader.getResource(filename).getFile()).getAbsolutePath();
	}
}

package com.bullhorn.dataloader.data;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class RowTest {

    @Test
    public void testConstructor() {
        Row row = new Row("data/Candidate.csv", 1);

        Assert.assertEquals(row.getNumber(), new Integer(1));
        Assert.assertEquals(row.getCells().size(), 0);
    }

    @Test
    public void testHasValueTrue() {
        Row row = new Row("data/Candidate.csv", 1);
        row.addCell(new Cell("id", "1"));

        Assert.assertEquals(row.hasValue("ID"), true);
    }

    @Test
    public void testHasValueFalse() {
        Row row = new Row("data/Candidate.csv", 1);
        row.addCell(new Cell("id", "1"));

        Assert.assertEquals(row.hasValue("name"), false);
    }

    @Test
    public void testGetValueValid() {
        Row row = new Row("data/Candidate.csv", 1);
        row.addCell(new Cell("id", "1"));

        Assert.assertEquals(row.getValue("ID"), "1");
    }

    @Test
    public void testGetValueInvalid() {
        Row row = new Row("data/Candidate.csv", 1);
        row.addCell(new Cell("id", "1"));

        Assert.assertNull(row.getValue("name"));
    }

    @Test
    public void testGetNamesValid() {
        Row row = new Row("data/Candidate.csv", 1);
        row.addCell(new Cell("id", "1"));
        row.addCell(new Cell("firstName", "John"));
        row.addCell(new Cell("lastName", "Smith"));

        List<String> expected = Arrays.asList("id", "firstName", "lastName");
        Assert.assertEquals(row.getNames(), expected);
    }

    @Test
    public void testGetNamesEmpty() {
        Row row = new Row("data/Candidate.csv", 1);

        List<String> expected = Arrays.asList(new String[]{});
        Assert.assertEquals(row.getNames(), expected);
    }

    @Test
    public void testGetValuesValid() {
        Row row = new Row("data/Candidate.csv", 1);
        row.addCell(new Cell("id", "1"));
        row.addCell(new Cell("firstName", "John"));
        row.addCell(new Cell("lastName", "Smith"));

        List<String> expected = Arrays.asList("1", "John", "Smith");
        Assert.assertEquals(row.getValues(), expected);
    }

    @Test
    public void testGetValuesEmpty() {
        Row row = new Row("data/Candidate.csv", 1);

        List<String> expected = Arrays.asList(new String[]{});
        Assert.assertEquals(row.getValues(), expected);
    }
}

package com.bullhorn.dataloader.util;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.contains;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.PrintStream;

import org.junit.Test;
import org.mockito.Mockito;

public class PrintUtilTest {

    @Test
    public void testPrintUsage() {
        //arrange
        final PrintUtil printUtil = Mockito.spy(PrintUtil.class);
        final PrintStream out = Mockito.mock(PrintStream.class);
        final String usage = "Usage:";
        System.setOut(out);
        out.flush();

        //act
        printUtil.printUsage();

        //assert
        verify(out).println(contains(usage));

    }

    @Test
    public void testPrintEntityError() {
        //arrange
        final PrintUtil printUtil = Mockito.spy(PrintUtil.class);
        final String entity = "Candidate";
        final String warning = "WARNING";

        //act
        printUtil.printEntityError(entity, warning);

        //assert
        verify(printUtil, times(6)).printAndLog(anyString());
        verify(printUtil, times(1)).printAndLog("ERROR: " + warning + " entity: \"" + entity + "\"");
        verify(printUtil, times(1)).printAndLog("       The entity is " + warning + " in REST and cannot be changed by DataLoader.\"");

    }

    @Test
    public void testPrintUnknownEntityError() {
        //arrange
        final PrintUtil printUtil = Mockito.spy(PrintUtil.class);
        final String entity = "Candidate";

        //act
        printUtil.printUnknownEntityError(entity);

        //assert
        verify(printUtil, times(6)).printAndLog(anyString());
        verify(printUtil, times(1)).printAndLog("ERROR: Unknown entity: \"" + entity + "\"");

    }

    @Test
    public void testPrintActionTotals() {
        //arrange
        final PrintUtil printUtil = Mockito.spy(PrintUtil.class);
        ActionTotals totals = new ActionTotals();
        final Integer total = totals.getTotalDelete() + totals.getTotalError() + totals.getTotalInsert() + totals.getTotalUpdate();

        //act
        printUtil.printActionTotals(totals);

        //assert
        verify(printUtil, times(1)).printAndLog("Results of DataLoader run");
        verify(printUtil, times(1)).printAndLog("Total records processed: " + total);
        verify(printUtil, times(1)).printAndLog("Total records inserted: " + totals.getTotalInsert());
        verify(printUtil, times(1)).printAndLog("Total records updated: " + totals.getTotalUpdate());
        verify(printUtil, times(1)).printAndLog("Total records deleted: " + totals.getTotalDelete());
        verify(printUtil, times(1)).printAndLog("Total records failed: " + totals.getTotalError());

    }

    @Test
    public void testPrintAndLog() {
        //arrange
        final PrintUtil printUtil = Mockito.spy(PrintUtil.class);
        final PrintStream out = Mockito.mock(PrintStream.class);
        final String string = "Testing Print and Log";
        System.setOut(out);
        out.flush();

        //act
        printUtil.printAndLog(string);

        //assert
        verify(out).println(contains(string));

    }
}

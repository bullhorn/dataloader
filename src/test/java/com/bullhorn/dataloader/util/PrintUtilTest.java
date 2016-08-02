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
        final PrintUtil printUtil = Mockito.spy(PrintUtil.class);
        final PrintStream out = Mockito.mock(PrintStream.class);
        final String usage = "Usage:";
        System.setOut(out);
        out.flush();

        printUtil.printUsage();

        verify(out).println(contains(usage));
    }

    @Test
    public void testPrintEntityError() {
        final PrintUtil printUtil = Mockito.spy(PrintUtil.class);
        final String entity = "Candidate";
        final String warning = "WARNING";

        printUtil.printEntityError(entity, warning);

        verify(printUtil, times(6)).printAndLog(anyString());
        verify(printUtil, times(1)).printAndLog("ERROR: " + warning + " entityClass: \"" + entity + "\"");
        verify(printUtil, times(1)).printAndLog("       The entityClass is " + warning + " in REST and cannot be changed by DataLoader.\"");
    }

    @Test
    public void testPrintUnknownEntityError() {
        final PrintUtil printUtil = Mockito.spy(PrintUtil.class);
        final String entity = "Candidate";

        printUtil.printUnknownEntityError(entity);

        verify(printUtil, times(6)).printAndLog(anyString());
        verify(printUtil, times(1)).printAndLog("ERROR: Unknown entityClass: \"" + entity + "\"");
    }

    @Test
    public void testPrintActionTotals() {
        final PrintUtil printUtil = Mockito.spy(PrintUtil.class);
        ActionTotals totals = new ActionTotals();
        final Integer total = totals.getTotalDelete() + totals.getTotalError() + totals.getTotalInsert() + totals.getTotalUpdate();

        printUtil.printActionTotals(totals);

        verify(printUtil, times(1)).printAndLog("Results of DataLoader run");
        verify(printUtil, times(1)).printAndLog("Total records processed: " + total);
        verify(printUtil, times(1)).printAndLog("Total records inserted: " + totals.getTotalInsert());
        verify(printUtil, times(1)).printAndLog("Total records updated: " + totals.getTotalUpdate());
        verify(printUtil, times(1)).printAndLog("Total records deleted: " + totals.getTotalDelete());
        verify(printUtil, times(1)).printAndLog("Total records failed: " + totals.getTotalError());
    }

    @Test
    public void testPrintAndLog() {
        final PrintUtil printUtil = Mockito.spy(PrintUtil.class);
        final PrintStream out = Mockito.mock(PrintStream.class);
        final String string = "Testing Print and Log";
        System.setOut(out);
        out.flush();

        printUtil.printAndLog(string);

        verify(out).println(contains(string));
    }
}

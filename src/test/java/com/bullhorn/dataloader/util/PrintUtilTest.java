package com.bullhorn.dataloader.util;

import com.bullhorn.dataloader.service.Command;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.PrintStream;

import static org.mockito.Matchers.anyString;

public class PrintUtilTest {

    @Test
    public void testPrintUsage() {
        final PrintUtil printUtil = Mockito.spy(PrintUtil.class);
        final PrintStream out = Mockito.mock(PrintStream.class);
        final String usage = "Usage:";
        System.setOut(out);
        out.flush();

        printUtil.printUsage();

        Mockito.verify(out).println(Mockito.contains(usage));
    }

    @Test
    public void testPrintEntityError() {
        final PrintUtil printUtil = Mockito.spy(PrintUtil.class);
        final String entity = "Candidate";
        final String warning = "WARNING";

        printUtil.printEntityError(entity, warning);

        Mockito.verify(printUtil, Mockito.times(6)).printAndLog(Mockito.anyString());
        Mockito.verify(printUtil, Mockito.times(1)).printAndLog("ERROR: " + warning + " entity: \"" + entity + "\"");
        Mockito.verify(printUtil, Mockito.times(1)).printAndLog("       The entity is " + warning + " in REST and cannot be changed by DataLoader.\"");
    }

    @Test
    public void testPrintUnknownEntityError() {
        final PrintUtil printUtil = Mockito.spy(PrintUtil.class);
        final String entity = "Candidate";

        printUtil.printUnknownEntityError(entity);

        Mockito.verify(printUtil, Mockito.times(6)).printAndLog(anyString());
        Mockito.verify(printUtil, Mockito.times(1)).printAndLog("ERROR: Unknown entity: \"" + entity + "\"");
    }

    @Test
    public void testPrintActionTotals() {
        final PrintUtil printUtil = Mockito.spy(PrintUtil.class);
        ActionTotals totals = new ActionTotals();
        final Integer total = totals.getTotalDelete() + totals.getTotalError() + totals.getTotalInsert() + totals.getTotalUpdate();

        printUtil.printActionTotals(Command.LOAD, totals);

        Mockito.verify(printUtil, Mockito.times(1)).printAndLog("Results of DataLoader run");
        Mockito.verify(printUtil, Mockito.times(1)).printAndLog("Total records processed: " + total);
        Mockito.verify(printUtil, Mockito.times(1)).printAndLog("Total records inserted: " + totals.getTotalInsert());
        Mockito.verify(printUtil, Mockito.times(1)).printAndLog("Total records updated: " + totals.getTotalUpdate());
        Mockito.verify(printUtil, Mockito.times(1)).printAndLog("Total records deleted: " + totals.getTotalDelete());
        Mockito.verify(printUtil, Mockito.times(1)).printAndLog("Total records failed: " + totals.getTotalError());
    }

    @Test
    public void testPrintActionTotals_CONVERT_ATTACHMENTS() {
        final PrintUtil printUtil = Mockito.spy(PrintUtil.class);
        ActionTotals totals = new ActionTotals();
        final Integer total = totals.getTotalDelete() + totals.getTotalError() + totals.getTotalInsert() + totals.getTotalUpdate();

        printUtil.printActionTotals(Command.CONVERT_ATTACHMENTS, totals);

        Mockito.verify(printUtil, Mockito.times(1)).printAndLog("Results of DataLoader run");
        Mockito.verify(printUtil, Mockito.times(1)).printAndLog("Total records processed: " + total);
        Mockito.verify(printUtil, Mockito.times(1)).printAndLog("Total records converted: " + totals.getTotalConvert());
        Mockito.verify(printUtil, Mockito.times(1)).printAndLog("Total records failed: " + totals.getTotalError());
    }

    @Test
    public void testPrintAndLogString() {
        final PrintUtil printUtil = Mockito.spy(PrintUtil.class);
        final PrintStream out = Mockito.mock(PrintStream.class);
        final String expected = "Testing Print and Log";
        System.setOut(out);
        out.flush();

        printUtil.printAndLog(expected);

        Mockito.verify(out).println(Mockito.contains(expected));
    }

    @Test
    public void testPrintAndLogException() {
        final PrintUtil printUtil = Mockito.spy(PrintUtil.class);
        final Exception e = new Exception("Test Exception");
        final PrintStream out = Mockito.mock(PrintStream.class);
        final String expected = "ERROR: java.lang.Exception: Test Exception";
        System.setOut(out);
        out.flush();

        printUtil.printAndLog(e);

        Mockito.verify(out).println(Mockito.contains(expected));
    }

    @Test
    public void testLog() {
        final PrintUtil printUtil = Mockito.spy(PrintUtil.class);
        final PrintStream out = Mockito.mock(PrintStream.class);
        System.setOut(out);
        out.flush();

        printUtil.log("Test Log");

        Mockito.verify(out, Mockito.never()).println(Mockito.anyString());
    }
}

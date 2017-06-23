package com.bullhorn.dataloader.util;

import com.bullhorn.dataloader.data.ActionTotals;
import com.bullhorn.dataloader.data.Result;
import com.bullhorn.dataloader.enums.Command;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;

import java.io.PrintStream;

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
    public void testPrintActionTotals() {
        final PrintUtil printUtil = Mockito.spy(PrintUtil.class);
        ActionTotals totals = new ActionTotals();
        final Integer total = 0;
        final String[] args = {"load", "candidate.csv"};
        Mockito.doNothing().when(printUtil).printAndLog(Mockito.anyString());

        printUtil.recordStart(args);
        printUtil.printActionTotals(Command.LOAD, totals);

        Mockito.verify(printUtil, Mockito.times(1)).printAndLog("Results of DataLoader run");
        Mockito.verify(printUtil, Mockito.times(1)).printAndLog(Matchers.startsWith("Start time: "));
        Mockito.verify(printUtil, Mockito.times(1)).printAndLog(Matchers.startsWith("End time: "));
        Mockito.verify(printUtil, Mockito.times(1)).printAndLog("Args: load candidate.csv");
        Mockito.verify(printUtil, Mockito.times(1)).printAndLog("Total records processed: " + total);
        Mockito.verify(printUtil, Mockito.times(1)).printAndLog("Total records inserted: " + totals.getActionTotal(Result.Action.INSERT));
        Mockito.verify(printUtil, Mockito.times(1)).printAndLog("Total records updated: " + totals.getActionTotal(Result.Action.UPDATE));
        Mockito.verify(printUtil, Mockito.times(1)).printAndLog("Total records deleted: " + totals.getActionTotal(Result.Action.DELETE));
        Mockito.verify(printUtil, Mockito.times(1)).printAndLog("Total records failed: " + totals.getActionTotal(Result.Action.FAILURE));
    }

    @Test
    public void testPrintActionTotals_CONVERT_ATTACHMENTS() {
        final PrintUtil printUtil = Mockito.spy(PrintUtil.class);
        ActionTotals totals = new ActionTotals();
        final Integer total = 0;
        final String[] args = {"convertAttachments", "candidateAttachFile.csv"};
        Mockito.doNothing().when(printUtil).printAndLog(Mockito.anyString());

        printUtil.recordStart(args);
        printUtil.printActionTotals(Command.CONVERT_ATTACHMENTS, totals);

        Mockito.verify(printUtil, Mockito.times(1)).printAndLog("Results of DataLoader run");
        Mockito.verify(printUtil, Mockito.times(1)).printAndLog(Matchers.startsWith("Start time: "));
        Mockito.verify(printUtil, Mockito.times(1)).printAndLog(Matchers.startsWith("End time: "));
        Mockito.verify(printUtil, Mockito.times(1)).printAndLog("Args: convertAttachments candidateAttachFile.csv");
        Mockito.verify(printUtil, Mockito.times(1)).printAndLog("Total records processed: " + total);
        Mockito.verify(printUtil, Mockito.times(1)).printAndLog("Total records converted: " + totals.getActionTotal(Result.Action.CONVERT));
        Mockito.verify(printUtil, Mockito.times(1)).printAndLog("Total records skipped: " + totals.getActionTotal(Result.Action.SKIP));
        Mockito.verify(printUtil, Mockito.times(1)).printAndLog("Total records failed: " + totals.getActionTotal(Result.Action.FAILURE));
    }

    @Test
    public void testPrintActionTotals_noRecordStart() {
        final PrintUtil printUtil = Mockito.spy(PrintUtil.class);
        ActionTotals totals = new ActionTotals();

        try {
            printUtil.printActionTotals(Command.LOAD, totals);
            Assert.fail("Excepted Exception");
        } catch (IllegalStateException e) {
            Assert.assertEquals("recordStart() not called", e.getMessage());
        }
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
        Mockito.doNothing().when(printUtil).print(Mockito.anyString());
        Mockito.doNothing().when(printUtil).log(Mockito.any(), Mockito.anyString());

        printUtil.printAndLog(e);

        final String expected = "ERROR: java.lang.Exception: Test Exception";
        Mockito.verify(printUtil, Mockito.times(1)).print(Mockito.contains(expected));
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

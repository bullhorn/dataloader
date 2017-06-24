package com.bullhorn.dataloader.util;

import com.bullhorn.dataloader.data.ActionTotals;
import com.bullhorn.dataloader.data.Result;
import com.bullhorn.dataloader.enums.Command;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Matchers;

import java.io.PrintStream;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.contains;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class PrintUtilTest {

    @Test
    public void testPrintUsage() {
        final PrintUtil printUtil = spy(PrintUtil.class);
        final PrintStream out = mock(PrintStream.class);
        final String usage = "Usage:";
        System.setOut(out);
        out.flush();

        printUtil.printUsage();

        verify(out).println(contains(usage));
    }

    @Test
    public void testPrintActionTotals() {
        final PrintUtil printUtil = spy(PrintUtil.class);
        ActionTotals totals = new ActionTotals();
        final Integer total = 0;
        final String[] args = {"load", "candidate.csv"};
        doNothing().when(printUtil).printAndLog(anyString());

        printUtil.recordStart(args);
        printUtil.printActionTotals(Command.LOAD, totals);

        verify(printUtil, times(1)).printAndLog("Results of DataLoader run");
        verify(printUtil, times(1)).printAndLog(Matchers.startsWith("Start time: "));
        verify(printUtil, times(1)).printAndLog(Matchers.startsWith("End time: "));
        verify(printUtil, times(1)).printAndLog("Args: load candidate.csv");
        verify(printUtil, times(1)).printAndLog("Total records processed: " + total);
        verify(printUtil, times(1)).printAndLog("Total records inserted: " + totals.getActionTotal(Result.Action.INSERT));
        verify(printUtil, times(1)).printAndLog("Total records updated: " + totals.getActionTotal(Result.Action.UPDATE));
        verify(printUtil, times(1)).printAndLog("Total records deleted: " + totals.getActionTotal(Result.Action.DELETE));
        verify(printUtil, times(1)).printAndLog("Total records failed: " + totals.getActionTotal(Result.Action.FAILURE));
    }

    @Test
    public void testPrintActionTotals_CONVERT_ATTACHMENTS() {
        final PrintUtil printUtil = spy(PrintUtil.class);
        ActionTotals totals = new ActionTotals();
        final Integer total = 0;
        final String[] args = {"convertAttachments", "candidateAttachFile.csv"};
        doNothing().when(printUtil).printAndLog(anyString());

        printUtil.recordStart(args);
        printUtil.printActionTotals(Command.CONVERT_ATTACHMENTS, totals);

        verify(printUtil, times(1)).printAndLog("Results of DataLoader run");
        verify(printUtil, times(1)).printAndLog(Matchers.startsWith("Start time: "));
        verify(printUtil, times(1)).printAndLog(Matchers.startsWith("End time: "));
        verify(printUtil, times(1)).printAndLog("Args: convertAttachments candidateAttachFile.csv");
        verify(printUtil, times(1)).printAndLog("Total records processed: " + total);
        verify(printUtil, times(1)).printAndLog("Total records converted: " + totals.getActionTotal(Result.Action.CONVERT));
        verify(printUtil, times(1)).printAndLog("Total records skipped: " + totals.getActionTotal(Result.Action.SKIP));
        verify(printUtil, times(1)).printAndLog("Total records failed: " + totals.getActionTotal(Result.Action.FAILURE));
    }

    @Test
    public void testPrintActionTotals_noRecordStart() {
        final PrintUtil printUtil = spy(PrintUtil.class);
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
        final PrintUtil printUtil = spy(PrintUtil.class);
        final PrintStream out = mock(PrintStream.class);
        final String expected = "Testing Print and Log";
        System.setOut(out);
        out.flush();

        printUtil.printAndLog(expected);

        verify(out).println(contains(expected));
    }

    @Test
    public void testPrintAndLogException() {
        final PrintUtil printUtil = spy(PrintUtil.class);
        final Exception e = new Exception("Test Exception");
        doNothing().when(printUtil).print(anyString());
        doNothing().when(printUtil).log(any(), anyString());

        printUtil.printAndLog(e);

        final String expected = "ERROR: java.lang.Exception: Test Exception";
        verify(printUtil, times(1)).print(contains(expected));
    }

    @Test
    public void testLog() {
        final PrintUtil printUtil = spy(PrintUtil.class);
        final PrintStream out = mock(PrintStream.class);
        System.setOut(out);
        out.flush();

        printUtil.log("Test Log");

        verify(out, never()).println(anyString());
    }
}

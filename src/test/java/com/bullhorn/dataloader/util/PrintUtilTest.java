package com.bullhorn.dataloader.util;

import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.contains;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.PrintStream;

import org.junit.Assert;
import org.junit.Test;

import com.bullhorn.dataloader.data.ActionTotals;
import com.bullhorn.dataloader.data.Result;
import com.bullhorn.dataloader.enums.Command;
import com.bullhorn.dataloader.enums.ErrorInfo;

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
    public void testPrintActionTotalsConvertAttachments() {
        final PrintUtil printUtil = spy(PrintUtil.class);
        ActionTotals totals = new ActionTotals();
        final int total = 0;
        final String[] args = {"convertAttachments", "candidateAttachFile.csv"};
        doNothing().when(printUtil).printAndLog(anyString());

        printUtil.recordStart(args);
        printUtil.printActionTotals(Command.CONVERT_ATTACHMENTS, totals);

        verify(printUtil, times(1)).printAndLog("Results of DataLoader run");
        verify(printUtil, times(1)).printAndLog(startsWith("Start time: "));
        verify(printUtil, times(1)).printAndLog(startsWith("End time: "));
        verify(printUtil, times(1)).printAndLog("Args: convertAttachments candidateAttachFile.csv");
        verify(printUtil, times(1)).printAndLog("Total records processed: " + total);
        verify(printUtil, times(1)).printAndLog("Total records converted: " + totals.getActionTotal(Result.Action.CONVERT));
        verify(printUtil, times(1)).printAndLog("Total records skipped: " + totals.getActionTotal(Result.Action.SKIP));
        verify(printUtil, times(1)).printAndLog("Total records failed: " + totals.getActionTotal(Result.Action.FAILURE));
    }

    @Test
    public void testPrintActionTotalsDelete() {
        final PrintUtil printUtil = spy(PrintUtil.class);
        ActionTotals totals = new ActionTotals();
        final int total = 0;
        final String[] args = {"delete", "candidate.csv"};
        doNothing().when(printUtil).printAndLog(anyString());

        printUtil.recordStart(args);
        printUtil.printActionTotals(Command.DELETE, totals);

        verify(printUtil, times(1)).printAndLog("Results of DataLoader run");
        verify(printUtil, times(1)).printAndLog(startsWith("Start time: "));
        verify(printUtil, times(1)).printAndLog(startsWith("End time: "));
        verify(printUtil, times(1)).printAndLog("Args: delete candidate.csv");
        verify(printUtil, times(1)).printAndLog("Total records processed: " + total);
        verify(printUtil, times(1)).printAndLog("Total records deleted: " + totals.getActionTotal(Result.Action.DELETE));
        verify(printUtil, times(1)).printAndLog("Total records failed: " + totals.getActionTotal(Result.Action.FAILURE));
    }

    @Test
    public void testPrintActionTotalsDeleteAttachments() {
        final PrintUtil printUtil = spy(PrintUtil.class);
        ActionTotals totals = new ActionTotals();
        final int total = 0;
        final String[] args = {"deleteAttachments", "candidateAttachments.csv"};
        doNothing().when(printUtil).printAndLog(anyString());

        printUtil.recordStart(args);
        printUtil.printActionTotals(Command.DELETE_ATTACHMENTS, totals);

        verify(printUtil, times(1)).printAndLog("Results of DataLoader run");
        verify(printUtil, times(1)).printAndLog(startsWith("Start time: "));
        verify(printUtil, times(1)).printAndLog(startsWith("End time: "));
        verify(printUtil, times(1)).printAndLog("Args: deleteAttachments candidateAttachments.csv");
        verify(printUtil, times(1)).printAndLog("Total records processed: " + total);
        verify(printUtil, times(1)).printAndLog("Total records deleted: " + totals.getActionTotal(Result.Action.DELETE));
        verify(printUtil, times(1)).printAndLog("Total records failed: " + totals.getActionTotal(Result.Action.FAILURE));
    }

    @Test
    public void testPrintActionTotalsExport() {
        final PrintUtil printUtil = spy(PrintUtil.class);
        ActionTotals totals = new ActionTotals();
        final int total = 0;
        final String[] args = {"export", "candidate.csv"};
        doNothing().when(printUtil).printAndLog(anyString());

        printUtil.recordStart(args);
        printUtil.printActionTotals(Command.EXPORT, totals);

        verify(printUtil, times(1)).printAndLog("Results of DataLoader run");
        verify(printUtil, times(1)).printAndLog(startsWith("Start time: "));
        verify(printUtil, times(1)).printAndLog(startsWith("End time: "));
        verify(printUtil, times(1)).printAndLog("Args: export candidate.csv");
        verify(printUtil, times(1)).printAndLog("Total records processed: " + total);
        verify(printUtil, times(1)).printAndLog("Total records exported: " + totals.getActionTotal(Result.Action.CONVERT));
        verify(printUtil, times(1)).printAndLog("Total records failed: " + totals.getActionTotal(Result.Action.FAILURE));
    }

    @Test
    public void testPrintActionTotalsLoad() {
        final PrintUtil printUtil = spy(PrintUtil.class);
        ActionTotals totals = new ActionTotals();
        final int total = 0;
        final String[] args = {"load", "candidate.csv"};
        doNothing().when(printUtil).printAndLog(anyString());

        printUtil.recordStart(args);
        printUtil.printActionTotals(Command.LOAD, totals);

        verify(printUtil, times(1)).printAndLog("Results of DataLoader run");
        verify(printUtil, times(1)).printAndLog(startsWith("Start time: "));
        verify(printUtil, times(1)).printAndLog(startsWith("End time: "));
        verify(printUtil, times(1)).printAndLog("Args: load candidate.csv");
        verify(printUtil, times(1)).printAndLog("Total records processed: " + total);
        verify(printUtil, times(1)).printAndLog("Total records inserted: " + totals.getActionTotal(Result.Action.INSERT));
        verify(printUtil, times(1)).printAndLog("Total records updated: " + totals.getActionTotal(Result.Action.UPDATE));
        verify(printUtil, times(1)).printAndLog("Total records skipped: " + totals.getActionTotal(Result.Action.SKIP));
        verify(printUtil, times(1)).printAndLog("Total records failed: " + totals.getActionTotal(Result.Action.FAILURE));
    }

    @Test
    public void testPrintActionTotalsLoadAttachments() {
        final PrintUtil printUtil = spy(PrintUtil.class);
        ActionTotals totals = new ActionTotals();
        final int total = 0;
        final String[] args = {"loadAttachments", "candidateAttachments.csv"};
        doNothing().when(printUtil).printAndLog(anyString());

        printUtil.recordStart(args);
        printUtil.printActionTotals(Command.LOAD_ATTACHMENTS, totals);

        verify(printUtil, times(1)).printAndLog("Results of DataLoader run");
        verify(printUtil, times(1)).printAndLog(startsWith("Start time: "));
        verify(printUtil, times(1)).printAndLog(startsWith("End time: "));
        verify(printUtil, times(1)).printAndLog("Args: loadAttachments candidateAttachments.csv");
        verify(printUtil, times(1)).printAndLog("Total records processed: " + total);
        verify(printUtil, times(1)).printAndLog("Total records inserted: " + totals.getActionTotal(Result.Action.INSERT));
        verify(printUtil, times(1)).printAndLog("Total records updated: " + totals.getActionTotal(Result.Action.UPDATE));
        verify(printUtil, times(1)).printAndLog("Total records skipped: " + totals.getActionTotal(Result.Action.SKIP));
        verify(printUtil, times(1)).printAndLog("Total records failed: " + totals.getActionTotal(Result.Action.FAILURE));
    }

    @Test
    public void testPrintActionTotalsNoRecordStart() {
        final PrintUtil printUtil = spy(PrintUtil.class);
        ActionTotals totals = new ActionTotals();

        try {
            printUtil.printActionTotals(Command.LOAD, totals);
            Assert.fail("Excepted Exception");
        } catch (DataLoaderException e) {
            Assert.assertEquals(ErrorInfo.NULL_POINTER_EXCEPTION, e.getErrorInfo());
            Assert.assertEquals("printActionTotals() failed because recordStart() was never called", e.getMessage());
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

        final String expected = "ERROR: Test Exception";
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

package com.bullhorn.dataloader.util;

import com.bullhorn.dataloader.service.Command;
import com.bullhorn.dataloader.service.csv.Result;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Methods that provide feedback to the user on the command line.
 */
public class PrintUtil {

    private final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private Logger log = LogManager.getLogger(PrintUtil.class);
    private String[] args = null;
    private Date startTime = null;

    public void recordStart(String[] args) {
        this.args = args;
        this.startTime = new Date();
    }

    public void printUsage() {
        print("");
        print("Usage: <action> <parameter>");
        print("                Load: dataloader load path/to/<EntityName>.csv");
        print("                      dataloader load path/to/directory");
        print("              Delete: dataloader delete path/to/<EntityName>.csv");
        print("                      dataloader delete path/to/directory");
        print(" Convert Attachments: dataloader convertAttachments path/to/<EntityName>.csv");
        print("    Load Attachments: dataloader loadAttachments path/to/<EntityName>.csv");
        print("  Delete Attachments: dataloader deleteAttachments path/to/<EntityName>.csv");
        print("     Create Template: dataloader template <EntityName>");
        print("");
        print("where <EntityName> is one of the supported entities listed at:");
        print("                   https://github.com/bullhorn/dataloader/wiki/Supported-Entities");
        print("");
    }

    public void printEntityError(String entityName, String warningText) {
        printAndLog("");
        printAndLog("ERROR: " + warningText + " entity: \"" + entityName + "\"");
        printAndLog("       The entity is " + warningText + " in REST and cannot be changed by DataLoader.\"");
        printAndLog("       See the full list of DataLoader supported entities at:");
        printAndLog("       https://github.com/bullhorn/dataloader/wiki/Supported-Entities.");
        printAndLog("");
    }

    public void printUnknownEntityError(String entityName) {
        printAndLog("");
        printAndLog("ERROR: Unknown entity: \"" + entityName + "\"");
        printAndLog("       This entity is not known to DataLoader and cannot be used.");
        printAndLog("       Check your spelling and see the full list of DataLoader supported entities at:");
        printAndLog("       https://github.com/bullhorn/dataloader/wiki/Supported-Entities.");
        printAndLog("");
    }

    public void printActionTotals(Command command, ActionTotals actionTotals) {
        if (startTime == null || args == null) {
            throw new IllegalStateException("recordStart() not called");
        }
        final Date endTime = new Date();
        final Integer totalRecords = actionTotals.getAllActionsTotal();

        printAndLog("Results of DataLoader run");
        printAndLog("Start time: " + dateFormat.format(startTime));
        printAndLog("End time: " + dateFormat.format(endTime));
        printAndLog("Args: " + String.join(" ", args));
        printAndLog("Total records processed: " + totalRecords);
        if (command.equals(Command.CONVERT_ATTACHMENTS)) {
            printAndLog("Total records converted: " + actionTotals.getActionTotal(Result.Action.CONVERT));
            printAndLog("Total records skipped: " + actionTotals.getActionTotal(Result.Action.SKIP));
        } else {
            printAndLog("Total records inserted: " + actionTotals.getActionTotal(Result.Action.INSERT));
            printAndLog("Total records updated: " + actionTotals.getActionTotal(Result.Action.UPDATE));
            printAndLog("Total records deleted: " + actionTotals.getActionTotal(Result.Action.DELETE));
        }
        printAndLog("Total records failed: " + actionTotals.getActionTotal(Result.Action.FAILURE));
    }

    /**
     * Prints to the console and logs to the logfile
     */
    public void printAndLog(String line) {
        print(line);
        log(line);
    }

    /**
     * Prints an error to the console and logs the error with stacktrace to the logfile
     */
    public void printAndLog(Exception e) {
        print("ERROR: " + e.toString());
        StringWriter stackTrace = new StringWriter();
        e.printStackTrace(new PrintWriter(stackTrace));
        log(Level.ERROR, stackTrace.toString());
    }

    /**
     * Prints to the command line
     */
    public void print(String line) {
        System.out.println(line);
    }

    /**
     * Default log that uses the INFO level
     */
    public void log(String line) {
        log(Level.INFO, line);
    }

    /**
     * Logs to the logfile
     */
    public void log(Level level, String line) {
        log.log(level, line);
    }
}

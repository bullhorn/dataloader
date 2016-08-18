package com.bullhorn.dataloader.util;

import com.bullhorn.dataloader.service.Command;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Methods that provide feedback to the user on the command line.
 */
public class PrintUtil {

    private Logger log = LogManager.getLogger(PrintUtil.class);

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

    public void printActionTotals(Command command, ActionTotals result) {
        final Integer totalRecords = result.getTotalError() + result.getTotalInsert() + result.getTotalUpdate() + result.getTotalDelete() + result.getTotalConvert();

        printAndLog("Results of DataLoader run");
        printAndLog("Total records processed: " + totalRecords);
        if (command.equals(Command.CONVERT_ATTACHMENTS)){
            printAndLog("Total records converted: " + result.getTotalConvert());
        } else {
            printAndLog("Total records inserted: " + result.getTotalInsert());
            printAndLog("Total records updated: " + result.getTotalUpdate());
            printAndLog("Total records deleted: " + result.getTotalDelete());
        }
        printAndLog("Total records failed: " + result.getTotalError());
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

package com.bullhorn.dataloader.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Methods that provide feedback to the user on the command line.
 */
public class PrintUtil {

    private Logger log = LogManager.getLogger(PrintUtil.class);

    public void printUsage() {
        System.out.println("");
        System.out.println("Usage:");
        System.out.println("    Insert/Update: dataloader load path/to/<EntityName>.csv");
        System.out.println("           Delete: dataloader delete <EntityName> path/to/data.csv");
        System.out.println("  Create Template: dataloader template <EntityName>");
        System.out.println("  Load Attachment: dataloader loadAttachments <EntityName> path/to/AttachmentMap.csv");
        System.out.println("");
        System.out.println("where <EntityName> is one of the supported entities listed at:");
        System.out.println("                   https://github.com/bullhorn/dataloader/wiki/Supported-Entities");
        System.out.println("");
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

    public void printActionTotals(ActionTotals result) {
        final Integer totalRecords = result.getTotalError() + result.getTotalInsert() + result.getTotalUpdate() + result.getTotalDelete();

        printAndLog("Results of DataLoader run");
        printAndLog("Total records processed: " + totalRecords);
        printAndLog("Total records inserted: " + result.getTotalInsert());
        printAndLog("Total records updated: " + result.getTotalUpdate());
        printAndLog("Total records deleted: " + result.getTotalDelete());
        printAndLog("Total records failed: " + result.getTotalError());
    }

    /**
     * Prints to the console and logs to the logfile
     */
    public void printAndLog(String line) {
        System.out.println(line);
        log.info(line);
    }
}

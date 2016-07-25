package com.bullhorn.dataloader.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Methods that provide feedback to the user on the command line.
 */
public class PrintUtil {

    private Logger log = LogManager.getLogger(PrintUtil.class);

    public void printUsage() {
        printAndLog("");
        printAndLog("Usage:");
        printAndLog("    Insert/Update: dataloader <EntityName> path/to/data.csv");
        printAndLog("           Delete: dataloader delete <EntityName> path/to/data.csv");
        printAndLog("  Create Template: dataloader template <EntityName>");
        printAndLog("");
        printAndLog("where <EntityName> is one of the supported entities listed at:");
        printAndLog("                   https://github.com/bullhorn/dataloader/wiki/Supported-Entities");
        printAndLog("");
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

    /**
     * Prints to the console and logs to the logfile - only used internally
     */
    private void printAndLog(String line) {
        System.out.println(line);
        log.info(line);
    }
}

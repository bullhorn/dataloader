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
        System.out.println("    Insert/Update: dataloader <EntityName> path/to/data.csv");
        System.out.println("           Delete: dataloader delete <EntityName> path/to/data.csv");
        System.out.println("  Create Template: dataloader template <EntityName>");
        System.out.println("");
        System.out.println("where <EntityName> is one of the supported entities listed at:");
        System.out.println("                   https://github.com/bullhorn/dataloader/wiki/Supported-Entities");
        System.out.println("");
    }

    public void printEntityError(String entityName, String warningText) {
        System.out.println("");
        System.out.println("ERROR: " + warningText + " entity: \"" + entityName + "\"");
        System.out.println("       The entity is " + warningText + " in REST and cannot be changed by DataLoader.\"");
        System.out.println("       See the full list of DataLoader supported entities at:");
        System.out.println("       https://github.com/bullhorn/dataloader/wiki/Supported-Entities.");
        System.out.println("");
    }

    public void printUnknownEntityError(String entityName) {
        System.out.println("");
        System.out.println("ERROR: Unknown entity: \"" + entityName + "\"");
        System.out.println("       This entity is not known to DataLoader and cannot be used.");
        System.out.println("       Check your spelling and see the full list of DataLoader supported entities at:");
        System.out.println("       https://github.com/bullhorn/dataloader/wiki/Supported-Entities.");
        System.out.println("");
    }
}

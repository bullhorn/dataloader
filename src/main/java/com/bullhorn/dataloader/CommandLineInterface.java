package com.bullhorn.dataloader;

import com.bullhorn.dataloader.enums.Command;
import com.bullhorn.dataloader.service.Action;
import com.bullhorn.dataloader.service.ActionFactory;
import com.bullhorn.dataloader.util.PrintUtil;

class CommandLineInterface {

    private final PrintUtil printUtil;
    private final ActionFactory actionFactory;

    CommandLineInterface(PrintUtil printUtil, ActionFactory actionFactory) {
        this.printUtil = printUtil;
        this.actionFactory = actionFactory;
    }

    /**
     * Starts the Command Line Interface
     *
     * @param args The user's command line parameters
     */
    public void start(String[] args) {
        printUtil.log("Args: " + String.join(" ", args));

        try {
            if (args.length == 0) {
                printUtil.printAndLog("ERROR: Missing action");
                printUtil.printUsage();
                return;
            }

            // parse command from command line
            Command command = null;
            for (Command iter : Command.values()) {
                if (iter.getMethodName().equalsIgnoreCase(args[0])) {
                    command = iter;
                    break;
                }
            }

            if (command == null) {
                printUtil.printAndLog("ERROR: Unrecognized action: " + args[0]);
                printUtil.printUsage();
                return;
            }

            Action action = actionFactory.getAction(command);
            if (!action.isValidArguments(args)) {
                printUtil.printUsage();
                return;
            }

            action.run(args);
        } catch (Exception e) {
            printUtil.printAndLog(e);
        }
    }
}

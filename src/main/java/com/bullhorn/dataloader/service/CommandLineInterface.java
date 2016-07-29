package com.bullhorn.dataloader.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CommandLineInterface extends AbstractService {

    protected Logger log = LogManager.getLogger(CommandLineInterface.class);

    public CommandLineInterface() {
    }

    /**
     * Starts the Command Line Interface
     *
     * @param args The user's command line parameters
     */
    public void start(String[] args) {
        log.info("Args: " + String.join(" ", args));

        try {
        	if (args.length == 0) {
        		printUtil.printAndLog("ERROR: Missing action");
				printUtil.printUsage();
				return;
			}

        	// parse command from command line
        	Command command = null;
        	for (Command iter: Command.values()) {
        		if (iter.getMethodName().equalsIgnoreCase(args[0])) {
        			command = iter;
        			break;
        		}
        	}

        	if (command == null) {
        		printAndLog("ERROR: Unrecognized action: " + args[0]);
        		printUtil.printUsage();
        		return;
        	}

        	// get Action instance
        	Action action = command.getAction();

        	// check if arguments are correct
        	if (!action.isValidArguments(args)) {
        		action.printUsage();
        		return;
        	}

        	// run action
        	action.run(args);
        } catch (Exception e) {
            printAndLog(e.toString());
        }
    }
}

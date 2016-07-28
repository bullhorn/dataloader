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

        	Action service = command.getAction();

        	if (!service.isValidArguments(args)) {
        		service.printUsage();
        		return;
        	}

        	service.run(args);
        } catch (Exception e) {
            printAndLog(e.toString());
        }

    }

}

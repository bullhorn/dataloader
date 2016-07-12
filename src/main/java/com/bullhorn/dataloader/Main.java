package com.bullhorn.dataloader;

import com.bullhorn.dataloader.service.CommandLineInterface;

public class Main {

    public static void main(String[] args) {
        CommandLineInterface commandLineInterface = new CommandLineInterface();
        commandLineInterface.start(args);
    }
}

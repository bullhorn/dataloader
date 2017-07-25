## DataLoader Architecture

### Overview

DataLoader is a locally running java application that provides a command line interface for converting input CSV files to Bullhorn REST API calls for mass inserts, updates, and deletes. Like Data Mirror, it is built on top of the [SDK-REST](https://github.com/bullhorn/sdk-rest) java library. DataLoader has the following inputs and outputs:

 * Inputs
	 * dataloader.properties file
	 * Command line arguments (command and file/directory)
	 * CSV input file or directory
		 * Attachment files/resumes if converting or attaching files
 * Outputs
	 * Command Line Output
	 * Results files (success and/or failure files for each CSV input file)
	 * Logfile

#### Services

Services in DataLoader are responsible for handling user commands. For example the `LoadService` is responsible for handling the `load` command. 

#### Tasks

A Task in DataLoader is responsible for processing an individual row of data from a CSV input file. For example, the `LoadTask` is responsible for turning a row of data into PUT, POST, or DELETE calls to insert/update/delete data in Bullhorn based upon the DataLoader settings in the `dataloader.properties` file. Tasks are created upon startup and placed in a task pool that runs `n` number of tasks concurrently, where `n` is equal to the `numThreads` setting in the properties file. Once all tasks in the pool have completed, the program exits.

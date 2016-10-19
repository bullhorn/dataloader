##DataLoader Architecture
-This file takes advantage of advanced markdown features to draw [LaTeX](https://en.wikibooks.org/wiki/LaTeX/Mathematics) mathematical formulas. Unfortunately, GitHub markdown does not support these diagrams. To view this file using StackEdit.io, click [here](https://stackedit.io/viewer#!url=https://raw.githubusercontent.com/bullhorn/dataloader/master/ARCHITECTURE.md)

###Overview
At the highest level, DataLoader is a command line interface that has the following inputs and outputs:

 * Inputs
	 * dataloader.properties file
	 * Command line arguments (command and file/directory)
	 * CSV input file or directory
		 * Attachment files/resumes if converting or attaching files
 * Outputs
	 * Command Line Output
	 * Results files (success and/or failure files for each CSV input file)
	 * Logfile

####Services
Services in DataLoader are responsible for handling user commands. For example the `LoadService` is responsible for handling the `load` command. 

####Tasks
Tasks in DataLoader are responsible for processing an individual row in a CSV input file. For example, the `LoadTask` is responsible for loading an individual row of data. When the LoadService executes, it will use the `ConcurencyService` to create a thread pool of `LoadTask` runners, one for each row in the file, and then let all of the tasks run to completion. 

### Performance

####Total Time to Load a CSV File
Up until you reach the point of thread saturation in the client JVM, the total time it takes to load a CSV file can be described as:

$$T_{total} = \dfrac{T_{row} * N_{row}}{N_{thread}} $$

T<sub>total</sub> = total time to load all records in a CSV file
T<sub>row</sub> = time to load an individual row
N<sub>row</sub> = number of rows in a CSV file
N<sub>thread</sub> = number of threads running on the client JVM

#### Time to Load an individual row in a CSV File
The total time it takes to load an individual row can be described as:

Best Case (Lookup already exists):
$$T_{row} = T_{call} * (N_{a} + 1)$$

Worst Case (Lookup External to Internal ID each time):
$$T_{row} = T_{call} * ((N_{a} * 2) + 1)$$

T<sub>row</sub> = time to load an individual row
T<sub>call</sub> = average time to send and receive a response for a REST call
N<sub>a</sub> = number of columns in this row that are associated to another entity (will require a single rest call to associate)


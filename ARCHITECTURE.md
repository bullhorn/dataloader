##DataLoader Architecture
This file takes advantage of advanced markdown features to draw [flowchart](http://flowchart.js.org/) and [sequence](https://bramp.github.io/js-sequence-diagrams/) diagrams. Unfortunately, GitHub markdown does not support these diagrams. To view this file using StackEdit.io, click [here](https://stackedit.io/viewer#!url=https://raw.githubusercontent.com/bullhorn/dataloader/master/ARCHITECTURE.md)

####Overview
At the highest level, DataLoader is a command line interface that has the following inputs and outputs:

 * Inputs
	 * Command line arguments (keywords)
	 * CSV Input File(s)
	 * dataloader.properties file
 * Outputs
	 * Logfile
	 * Results files
	 * Command Line Output

####DataLoader Flow
This flowchart is based on the command line arguments provided.

```flow
start=>start: Command Line Interface
end=>end
template=>condition: template?
delete=>condition: delete?
templateOp=>operation: Create Template
deleteSub=>subroutine: Delete
loadSub=>subroutine: Load

start->template
template(yes)->templateOp
template(no)->delete
delete(yes)->deleteSub
delete(no)->loadSub
templateOp->end
deleteSub->end
loadSub->end
```

####Load Task Flow
The load task will upsert an individual row of data into a record (update a record if it exists, otherwise insert a new record). The `<Entity>ExistField` from the dataloader.properties file will be used to determine whether the entity exists or not. To-One relationships, To-Many relationships and the entity record itself will all be individually upsert'ed.

```flow
start=>start: Run
end=>end: Exit
toOneCond=>condition: To-One fields?
toManyCond=>condition: To-Many fields?
toOneOp=>operation: Upsert To-One
toManyOp=>operation: Upsert To-Many
loadOp=>operation: Upsert Direct and Composite Data Fields
outputOp=>operation: Output Results

start->toOneCond
toOneCond(yes)->toOneOp
toOneCond(no)->toManyCond
toOneOp->toManyOp
toManyCond(yes)->toManyOp
toManyCond(no)->loadOp
toManyOp->loadOp
loadOp->outputOp
outputOp->end
```

####Delete Task Flow
Entities that are hard deletable are deleted using the "DELETE" call. Entities that are soft deletable are deleted by setting their `isDeleted` flag to `true`.

```flow
start=>start: Run
end=>end: Exit
hardCond=>condition: Is Hard Deletable?
softCond=>condition: Is Soft Deletable?
hardOp=>operation: Hard Delete
softOp=>operation: Soft Delete
outputOp=>operation: Output Results

start->hardCond
hardCond(yes)->hardOp
hardCond(no)->softCond
softCond(yes)->softOp
softCond(no)->outputOp
hardOp->outputOp
softOp->outputOp
outputOp->end
```

####Startup Sequence
```sequence
CommandLineInterface->ValidationUtil: Validate Arguments
CommandLineInterface->PropertiesFileUtil: Read Properties File
CommandLineInterface->BullhornAPI: Create Session
Note right of BullhornAPI: Establish REST Session
CommandLineInterface->BullhornAPI: Front Load Entities
Note right of BullhornAPI: Load Maps of ExternalID -> BullhornID
CommandLineInterface->BullhornAPI: Get Meta Data for Entity
BullhornAPI-->CommandLineInterface: MetaMap (key: fieldName, value: data (type, label, association, etc.))
CommandLineInterface->CommandLineInterface: Create Loading Cache
Note right of CommandLineInterface: Key: EntityQuery, Value: BullhornID
CommandLineInterface->CsvFileReader: Initialize with MetaMap
CommandLineInterface->CsvFileWriter: Initialize with Header Columns
CommandLineInterface->ConcurrencyService: Initialize with BullhornAPI, entity, cache, reader, writer, etc.
```

####Thread Pool Creation Sequence
```sequence
ConcurrencyService->CsvFileReader: Read row
CsvFileReader-->ConcurrencyService: JsonRow
ConcurrencyService->ConcurrencyService: Create LoadTask
ConcurrencyService->ExecutorService: Add runnable LoadTask to thread pool
Note right of ConcurrencyService: Repeat for each row in CSV File
```

####DataLoader Runtime Sequence
```sequence
CommandLineInterface->ConcurrencyService: Initialize
ConcurrencyService->ExecutorService: Create Task Thread
Note right of CommandLineInterface: Exit Main Thread
ExecutorService->Task: Task:Run()
Task->Task: Make REST Calls
Note right of Task: Exit Thread
Note right of ExecutorService: Repeat for All Tasks
```

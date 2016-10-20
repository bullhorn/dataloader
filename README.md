![DataLoader Logo](dataloader.png)

A Bullhorn Platform SDK tool. Quickly import CSV data into your Bullhorn CRM.

[![Build Status](https://travis-ci.com/bullhorn/dataloader.svg?token=Ta7yXSf1ut1W7VuGXTKA&branch=master)](https://travis-ci.org/bullhorn/dataloader)
[![Coverage Status](https://coveralls.io/repos/github/bullhorn/dataloader/badge.svg?t=gVrMsY)](https://coveralls.io/github/bullhorn/dataloader)

## Quick Start Guide

 1. Install Java if you don't have it already (you won't need the development kit for Windows) - [Windows](http://javadl.oracle.com/webapps/download/AutoDL?BundleId=210182) | [Mac](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)
 
    1. Verify that you have the latest version of java on the command line by typing: `java -version`, which should show: `java version "1.8"`

 2. Download the dataloader.zip file from the downloads section of the [Latest Release](https://github.com/bullhorn/dataloader/releases/latest)
 
 3. Extract contents of dataloader.zip file (will extract into a `dataloader` folder)
 
 4. Edit the file `dataloader/dataloader.properties` to enter your account information
 
 5. Place your source CSV files in the `dataloader/data` folder. Rename them to be the name of the entity being loaded, for example: `Candidate.csv`, `Placement.csv`, etc.

 6. Open a command prompt in the `dataloader` folder

 7. Get familiar with the available fields (column header names) from the example files in the `dataloader/examples` folder or use the `template` command to generate an example file containing all possible fields for the entity, for example: `> dataloader template Candidate`

 8. Ensure that the column names in your source CSV exist in the example and/or template CSV files

 9. Run dataloader on the command line, for example: `> dataloader load data/Candidate.csv`
 
 10. Check for status on the command line, check for error records in the file: `dataloader/results/<InputFileName>_<Timestamp>_failure.csv`, check for successful records in the file: `dataloader/results/<InputFileName>_<Timestamp>_success.csv`, and check for full output in the `dataloader/log` directory
 
## Releases

* **[Latest Release](https://github.com/bullhorn/dataloader/releases/latest)**
* **[All Releases](https://github.com/bullhorn/dataloader/releases)**

## Documentation

*  **[DataLoader Wiki](https://github.com/bullhorn/dataloader/wiki)**
*  **[Bullhorn Open Source](http://bullhorn.github.io)**
*  **[Bullhorn Website](http://www.bullhorn.com)**

## Requirements

* 8GB RAM Minimum, 16GB RAM Recommended

## Configure

Edit the file: `dataloader.properties` to specify the login credentials, data configuration and more.

## Generate CSV Template

```
dataloader template <EntityName>
```

This will generate the file: `<EntityName>Example.csv` in the current directory. This file will contain all of the available fields in the entity record as columns in the CSV. The first row of data will contain the data type (string, integer, etc). The first row data must be replaced with the data of the first record. For more information, see the [wiki](https://github.com/bullhorn/dataloader/wiki/Commands#template)

## Load

```
dataloader load path/to/<EntityName>.csv
```

Inserts or updates records. Requires a column for each data field to load. These column names must match the names in the entity template. The provided CSV file must start with the name of the entity. If the `<EntityName>ExistField` property in the `dataloader.properties` file is enabled, that field (column) will be used to determine if the record already exists. If a record with the `ExistField` value exists, that record will be updated, otherwise a new record will be inserted. If the `<EntityName>ExistField` property is disabled, a new record will always be created. ExistFields are disabled by default in the properties file. For more information, see the [wiki](https://github.com/bullhorn/dataloader/wiki/Commands#load).

```
dataloader load path/to/directory
```

Performs load for every valid CSV file in the given directory. The order that entities are loaded in will respect dependencies. The provided directory must contain valid CSV files that start with the name of the entity.

## Delete

```
dataloader delete path/to/<EntityName>.csv
```

The provided CSV file must contain an `id` column. This column will contain the Bullhorn internal IDs of the records to delete. The provided CSV file must start with the name of the entity.

```
dataloader delete path/to/directory
```

Performs delete for every valid CSV file in the given directory. The order that entities are deleted in will respect dependencies. The provided directory must contain valid CSV files that start with the name of the entity. For more information, see the [wiki](https://github.com/bullhorn/dataloader/wiki/Commands#delete).

## Convert Attachments

```
dataloader convertAttachments path/to/<EntityName>.csv
```

Converts locally stored files (txt, doc/x, opend, odt, rtf, html or pdf) to html. Writes out converted attachments to: `convertedAttachments\<EntityName>\<ExternalID>` in the current working directory. ExternalID is the value of the `<entityName>.externalID` column in the CSV input file. This action is to be followed by the `loadAttachments` command, which will check the `convertedAttachments` folder that was created and load any converted attachments that match the `externalID` in the CSV input file. All converted attachments will be set as the description field for the entity. Currently supports Candidate, ClientContact, and ClientCorporation. For more information, see the [wiki](https://github.com/bullhorn/dataloader/wiki/Commands#convertattachments).

## Load Attachments

```
dataloader loadAttachments path/to/<EntityName>.csv
```

Attaches files to preexisting records. Attachment file paths are mapped with provided CSV file. Only attaches files for one entity at a time. The provided CSV file must start with the name of the entity. For more information, see the [wiki](https://github.com/bullhorn/dataloader/wiki/Commands#loadattachments).

## Delete Attachments

```
dataloader deleteAttachments <path/to/<EntityName>.csv
```

The provided CSV file must contain an `id` column and a `parentEntityID` column. The `id` column will contain the Bullhorn internal IDs of the files to delete and the `parentEntityID` column will contain the Bullhorn internal IDs of the entity the file is attached to. The provided CSV file must start with the name of the entity. For more information, see the [wiki](https://github.com/bullhorn/dataloader/wiki/Commands#deleteattachments).

## Console Output

DataLoader provides run process status for the console. Every 111 records, there will be an output of `Processed: # records.` 
When the run is complete, total status of all actions will display. Such as:
```
Results of DataLoader run
Total records processed: #
Total records inserted: #
Total records updated: #
Total records deleted: #
Total records failed: #
```

## Results Files

DataLoader divides all processed rows from the given CSV file into two output files, one for sucessfully uploaded rows and one for failures. Results files are saved in the folder `results/` inside the current working directory. If no `results/` directory exists, one will be created at runtime. The success file will be named: `results/<InputFileName>_<Timestamp>_success.csv` and each row will be a copy of the original row prepended with two extra columns: an `id` column with the bullhorn ID of the record and an `action` column that will be either `INSERT` or `UPDATE`. The failure file will be named: `results/<InputFileName>_<Timestamp>_failure.csv` and each row will be a copy of the original row prepended with a `reason` column containing the error text.

## Log File Output

DataLoader logs all operations to file. Logfiles are saved in the folder `log/` inside the current working directory. If no `log/` directory exists, one will be created at runtime. The name of the most recently generated logfile will be `dataloader.log`. All historical logfiles will have timestamps appended to the filename, such as: `dataloader_yyyy-MM-dd_HH:MM.ss.log`.

## Examples

Minimal example files for learning purposes for High-Level Entities are provided in the [examples](https://github.com/bullhorn/dataloader/tree/master/examples) folder and are covered in detail on the [Examples Wiki Page](https://github.com/bullhorn/dataloader/wiki/Examples).

## Property File Input *(Optional)*

By default DataLoader will attempt to load the file `dataloader.properties` from the current working directory. To use a different properties file, use the `-Dpropertyfile` argument.

```
java -Dpropertyfile=path/to/my/dataloader.properties -jar target/dataloader-{version}.jar template <EntityName>
```

## Contribute

There are multiple ways to contribute to Bullhorn DataLoader:
 * **[Submit bugs](https://github.com/bullhorn/dataloader/issues)** and help us verify fixes as they are checked in.
 * Review **[source code changes](https://github.com/bullhorn/dataloader/pulls)**.
 * **[Contribute bug fixes](https://github.com/bullhorn/dataloader/issues)**.

## Building From Source Code

1. Install Prerequisites
 * [Apache Maven 3.0](https://maven.apache.org/)
 * [Java SDK 1.8](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)

2. Clone the repo:

  ```
  git clone https://github.com/bullhorn/dataloader.git
  ```

3. Change to the DataLoader directory:

  ```
  cd dataloader
  ```

4. Build the jar:

  ```
  mvn clean package
  ```

This will produce `dataloader-{version}.jar` in `/target`, which includes all required dependencies.

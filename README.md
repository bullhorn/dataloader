![DataLoader Logo](dataloader.png)

Bullhorn DataLoader is a downloadable tool for quickly importing and updating data from a CSV file to Bullhorn.

## Releases

* **[Latest Release](https://github.com/bullhorn/dataloader/releases/latest)**
* **[All Releases](https://github.com/bullhorn/dataloader/releases)**

## Documentation

*  **[DataLoader Wiki](https://github.com/bullhorn/dataloader/wiki)**
*  **[Bullhorn Open Source](http://bullhorn.github.io)**
*  **[Bullhorn Website](http://www.bullhorn.com)**

## Contribute

There are multiple ways to contribute to Bullhorn Data Loader:
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

3. Change to the Data Loader directory:

  ```
  cd dataloader
  ```

4. Build the jar:

  ```
  mvn clean package
  ```

This will produce `dataloader-{version}.jar` in `/target`, which includes all required dependencies.

## Configure

Edit the file: `dataloader.properties` to specify the login credentials, data configuration and more.

## Generate CSV Template *(Optional)*

```
java -jar target/dataloader-{version}.jar template <EntityName>
```

This will generate the file: `<EntityName>Example.csv` in the current directory. This file will contain all of the available fields in the entity record as columns in the CSV. The first row of data will contain the data type (string, integer, etc).

## Run

```
java -jar target/dataloader-{version}.jar <EntityName> /path/to/file.csv
```

## Results Files

DataLoader divides all processed rows from the given CSV file into two output files, one for sucessfully uploaded rows and one for failures. Results files are saved in the folder `results/` inside the current working directory. If no `results/` directory exists, one will be created at runtime. The success file will be named: `results/<EntityName>_<Timestamp>_success.csv` and each row will be a copy of the original row prepended with two extra columns: an `id` column with the bullhorn ID of the record and an `action` column that will be either `INSERT` or `UPDATE`. The failure file will be named: `results/<EntityName>_<Timestamp>_failure.csv` and each row will be a copy of the original row prepended with a `reason` column containing the error text.

## Log File Output

DataLoader logs all operations to file. Logfiles are saved in the folder `log/` inside the current working directory. If no `log/` directory exists, one will be created at runtime. The name of the most recently generated logfile will be `dataloader.log`. All historical logfiles will have timestamps appended to the filename, such as: `dataloader_yyyy-MM-dd_HH:MM.ss.log`.

## Examples

Minimal example files for learning purposes for High-Level Entities are provided in the [examples](https://github.com/bullhorn/dataloader/tree/master/examples) folder and are covered in detail on the [Examples Wiki Page](https://github.com/bullhorn/dataloader/wiki/Examples).

## Property File Input *(Optional)*

By default DataLoader will attempt to load the file `dataloader.properties` from the current working directory. To use a different properties file, use the `-Dpropertyfile` argument.

```
java -Dpropertyfile=path/to/my/dataloader.properties -jar target/dataloader-{version}.jar template <EntityName>
```
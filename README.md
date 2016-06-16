![DataLoader Logo](dataloader.png)

Bullhorn DataLoader is a downloadable tool for quickly importing and updating data from a CSV file to Bullhorn.

## Releases

* **[Latest Release](https://github.com/bullhorn/dataloader/releases/latest)**
* **[All Releases](https://github.com/bullhorn/dataloader/releases)**

## Contribute

There are multiple ways to contribute to Bullhorn Data Loader:
* **[Submit bugs](https://github.com/bullhorn/dataloader/issues)** and help us verify fixes as they are checked in.
* Review **[source code changes](https://github.com/bullhorn/dataloader/pulls)**.
* **[Contribute bug fixes](https://github.com/bullhorn/dataloader/issues)**.

## Documentation

*  **[DataLoader Wiki](https://github.com/bullhorn/dataloader/wiki)**
*  **[Bullhorn Open Source](http://bullhorn.github.io)**
*  **[Bullhorn Website](http://www.bullhorn.com)**

## Building

1. Install Prerequisites
 * [Apache Maven 3.0](https://maven.apache.org/)
 * [Java 1.8](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)

2. Clone a copy of the repo:

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
java -Dpropertyfile=dataloader.properties -jar target/dataloader-{version}.jar template <EntityName>
```

This will generate the file: `<EntityName>Example.csv` in the current directory. This file will contain all of the available fields in the entity record as columns in the CSV. The first row of data will contain the data type (string, integer, etc).

## Run

```
java -Dpropertyfile=dataloader.properties -jar target/dataloader-{version}.jar <EntityName> /path/to/file.csv
```

![DataLoader Logo](dataloader.png)

**[Bullhorn DataLoader](http://www.bullhorn.com)** is a downloadable tool used to quickly import and/or update Bullhorn CRM data from a CSV file.

## Releases

* **[Latest Release](https://github.com/bullhorn/dataloader/releases/latest)**
* **[All Releases](https://github.com/bullhorn/dataloader/releases)**

## Contribute

There are many ways to **[contribute](https://github.com/bullhorn/dataloader/blob/master/CONTRIBUTING.md)** to Bullhorn Data Loader.
* **[Submit bugs](https://github.com/bullhorn/dataloader/issues)** and help us verify fixes as they are checked in.
* Review **[source code changes](https://github.com/bullhorn/dataloader/pulls)**.
* **[Contribute bug fixes](https://github.com/bullhorn/dataloader/blob/master/CONTRIBUTING.md)**.

## Documentation

*  **[DataLoader Wiki](https://github.com/bullhorn/dataloader/wiki)**
*  **[Bullhorn Platform](http://bullhorn.github.io/platform)**
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

This will produce `dataloader-importer-{version}.jar` in `/target`, which includes all required dependencies.

## Configure

Edit the file: `dataloader.properties` to specify the login credentials, data configuration and more.

## Run

```
java -Dpropertyfile=dataloader.properties -jar target/dataloader-importer-{version}.jar <EntityName> /path/to/data.csv
```

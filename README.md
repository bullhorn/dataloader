![DataLoader Logo](dataloader.svg)

**[Bullhorn DataLoader](http://www.bullhorn.com)** is a tool used to quickly import and/or update Bullhorn CRM data via a CSV file.

## Releases

* **[Latest Release](https://github.com/bullhorn/dataloader/releases/latest)**
* **[All Releases](https://github.com/bullhorn/dataloader/releases)**

## Contribute

There are many ways to **[contribute](https://github.com/bullhorn/dataloader/blob/master/CONTRIBUTING.md)** to Bullhorn Data Loader.
* **[Submit bugs](https://github.com/bullhorn/dataloader/issues)** and help us verify fixes as they are checked in.
* Review **[source code changes](https://github.com/bullhorn/dataloader/pulls)**.
* **[Contribute bug fixes](https://github.com/bullhorn/dataloader/blob/master/CONTRIBUTING.md)**.

## Documentation

*  **[Bullhorn Platform](http://bullhorn.github.io/platform)**
*  **[Bullhorn Website](http://www.bullhorn.com)**

## Building

In order to build **Bullhorn Data Loader**, ensure that you have **[Apache Maven 3.0](https://maven.apache.org/)** and
**[Java 1.8](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)** installed.

Clone a copy of the repo:

```
git clone https://github.com/bullhorn/dataloader.git
```

Change to the Data Loader directory:

```
cd dataloader
```

Build the jar:

```
mvn clean package
```

This will produce `dataloader-importer-{version}.jar` in `/target`, which includes all required dependencies.

## Usage

The configuration for **Bullhorn Data Loader** must be placed in a file called `dataloader.properties`.

The property file can be overridden by passing in an extra argument when running the jar.
`-Dpropertyfile=C:\\path\\to\\file.properties`.

A sample configuration is given below:

```
candidateExistField=id
clientContactExistField=id
clientCorporationExistField=id
jobOrderExistField=title,name
opportunityExistField=id
leadExistField=id
businessSectorExistField=name
categoryExistField=occupation
skillExistField=name

frontLoadedEntities=BusinessSector,Skill,Category
numThreads=1
dateFormat=MM/dd/yyyy
listDelimiter=|
pageSize=500
cacheSize=10000

username=
password=

authorizeUrl=https://auth.bullhornstaffing.com/oauth/authorize
tokenUrl=https://auth.bullhornstaffing.com/oauth/token
loginUrl=https://rest.bullhornstaffing.com/rest-services/login

clientId=
clientSecret=
```

The Entity ExistField properties refer to the fields that will be used when searching for entities. If **Bullhorn Data Loader** finds
one result by the search-call then it will not alter that entity. If an ID is specified for that entity and a field
specified as an ExistField has been altered, then **Bullhorn Data Loader** will attempt to update the existing entity.

Multiple ExistFields can be specified as comma-separated values. E.g.

```
clientContactExistField=firstName,lastName
```

The jar file is executed with

```
java -jar dataloader-importer-{version}.jar <Entity> /path/to/csv
```

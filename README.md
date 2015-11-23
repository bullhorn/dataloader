```
    /     _/_      //           /
 __/ __.  /  __.  // __ __.  __/ _  __
(_/_(_/|_<__(_/|_</_(_)(_/|_(_/_</_/ (_
```

## Build

Dataloader builds with Apache Maven 3.0 and Java 1.8. Running `mvn clean package`
will produce `dataloader-importer-{version}.jar` in dataloader/target.
This has all of the dependencies necessary to run it.

## Usage

The configuration for Dataloader must be placed in a file called `dataloader.properties`.
The default folder that the file must be placed in is `C:\\bullhorn\\conf\\` for Windows or `/usr/local/bullhorn/conf/`
for Linux and Mac.

The folder path can be overridden by passing in an extra argument when running the jar.
`-Ddataloader.configuration.path=C:\\path\\to\\folder\\`. The trailing slash is necessary.

A sample configuration is given below.

```
numThreads=20
cacheSize=10000
dateFormat=MM/dd/yyyy
candidateExistField=name
clientContactExistField=lastName
opportunityExistField=id
clientCorporationExistField=name
leadExistField=phone
username=
password=
authorizeUrl=https://auth9.bullhornstaffing.com/oauth/authorize
tokenUrl=https://auth9.bullhornstaffing.com/oauth/token
loginUrl=https://rest9.bullhornstaffing.com/rest-services/login
clientId=
clientSecret=
```

The Entity ExistField properties refer to the fields that will be used when searching for entities. If Dataloader finds
one result by the search-call then it will not alter that entity. If an ID is specified for that entity and a field
specified as an ExistField has been altered, then Dataloader will attempt to update the existing entity.

Multiple ExistFields can be specified as comma-separated values. E.g.

```
clientContactExistField=firstName,lastName
```

The jar file is executed with

```java -jar dataloader-importer-{version}.jar <Entity> /path/to/csv```

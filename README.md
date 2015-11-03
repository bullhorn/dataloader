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

The properties for dataloader must be placed in
`C:\\bullhorn\\conf\\dataloader.properties` for Windows or
`/usr/local/bullhorn/conf/dataloader.properties` for Linux.

A sample configuration is given below.

```
numThreads=20
dateFormat=MM/dd/yyyy
candidateExistField=candidateID
clientContactExistField=clientContactID
opportunityExistField=opportunityID
clientCorporationExistField=clientCorporationID
leadExistField=leadID
username=
password=
authorizeUrl=https://auth9.bullhornstaffing.com/oauth/authorize
tokenUrl=https://auth9.bullhornstaffing.com/oauth/token
loginUrl=https://rest9.bullhornstaffing.com/rest-services/login
clientId=
clientSecret=
```

The jar file is executed with

```java -jar dataloader-importer-{version}.jar <Entity> /path/to/csv```
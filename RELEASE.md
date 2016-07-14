##### DataLoader Developer Documentation

## How to Create a Release

DataLoader provides a release binary which allows end users to use DataLoader without having to build from source using the JDK. We use the Maven Assembly plugin to package the DataLoader jar file along with the directory structure, README.md, LICENSE, and other supporting files into `target/dataloader.zip`.

 1. Bump Version
  
    1. Change the pom.xml `<version>x.y.z</version>` tag as part of your pull request
 
 1. Create Release Package (after pull request is merged to master)

    1. Start with a clean checkout of the master branch
  
        1. Remove any local uncommitted changes to files
     
        1. Remove any local changes to the dataloader.properties file
     
        1. Remove all local files from the log and data folders
 
    1. Run package command: `mvn package`

    1. Run assembly command: `mvn assembly:single`

 1. Create release in GitHub

    1. From the [Releases Page](https://github.com/bullhorn/dataloader/releases) click Create a New Release.
    
    1. The name of the release is the version number prepended with a `v`, like: `v1.2.3`.
    
 1. In the Attach Binaries section of the release page, attach the release package file: `target/dataloader.zip`
 
 1. Test the new release by stepping through the Quick Start Guide on both Windows and Mac/Linux

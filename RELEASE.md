##### DataLoader Developer Documentation

## How to Create a Release

DataLoader provides a release binary which allows end users to use DataLoader without having to build from source using the JDK. We use the Maven Assembly plugin to package the DataLoader jar file along with the directory structure, README.md, LICENSE, and other supporting files into `target/dataloader.zip`.

 1. Create release branch: `release/vX.Y.Z`
  
    1. Remove the `-SNAPSHOT` from the pom.xml `<version>X.Y.Z-SNAPSHOT</version>` tag
    
 2. Create release package

    1. Start with a clean checkout of the release branch
  
        1. Remove any local uncommitted changes to files
     
        2. Remove any local changes to the dataloader.properties file
     
        3. Remove all local files from the log and data folders
 
    2. Run package command: `mvn clean package`

    3. Run assembly command: `mvn assembly:single` to generate the release zip file: `target/dataloader.zip`

 3. Test the new release package by stepping through the Quick Start Guide on both Windows and Mac/Linux

 4. Merge the release branch

 5. Create the wiki zipfile which includes PDFs of the wiki pages
 
    1. Clone the wiki repo: `git clone https://github.com/bullhorn/dataloader.wiki.git`
     
    2. Download conversion utilities: `npm install`

    3. Create the wiki.zip file: `npm start`

 6. Create release in GitHub

    1. From the [Releases Page](https://github.com/bullhorn/dataloader/releases) click [Draft a New Release](https://github.com/bullhorn/dataloader/releases/new).
    
    2. Set the Tag Version and Release Title to the version number prepended with a `v`, like: `v1.2.3`.
    
    3. In the Attach Binaries section of the release page, attach the release package file: `target/dataloader.zip`
 
    4. In the Attach Binaries section of the release page, attach the wiki package file: `wiki.zip`
 
 7. Make a new commit to master to bump the version and add `-SNAPSHOT` to the pom.xml version

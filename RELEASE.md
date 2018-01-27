##### DataLoader Developer Documentation

### Standard Commit Messages on Master

Commits to master use the [Conventional Commits Specification](https://conventionalcommits.org/). Admins select the _Squash and Merge_ option when merging in Pull Requests and rename the commit to follow the format: _type_(_scope_): _description_. 

__Examples:__ 

`feat(Candidate): Added field 'xyz'`

`fix(To-Many Associations): fixed xyz`

`chore(cleanup): cleaned up xyz`

### Creating a Release

[standard-version](https://www.npmjs.com/package/standard-version) is used to calculate the version number based on the standard commits on master since the last tagged version. It also updates the `pom.xml` version, generates the `CHANGELOG.md` file, and pushes tags to master, which tells TravisCI to generate the build artifacts and publish them to the GitHub release.

 1. Checkout master
  
 2. Run release script: `npm run release`. This will create a new commit and push to master.
     
 3. Create wiki zipfile (PDFs of the wiki pages)
 
    1. Clone the wiki repo: `git clone https://github.com/bullhorn/dataloader.wiki.git`
     
    2. Download conversion utilities: `npm install`

    3. Create wiki.zip file: `npm start`

 4. Update GitHub Release

    1. Copy the generated release notes for the latest version from `CHANGELOG.md`.

    2. On the [Latest Release](https://github.com/bullhorn/dataloader/releases/latest) click `Edit release` and paste in the copied release notes.

    3. In the Attach Binaries section of the release page, attach the wiki package file: `wiki.zip`

 5. Post a slack message in `#dataloader-community` that the new release is available with the release notes.

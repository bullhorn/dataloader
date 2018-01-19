##### DataLoader Developer Documentation

### Standard Commit Messages on Master

Commits to master use the [Conventional Commits Specification](https://conventionalcommits.org/). Admins select the _Squash and Merge_ option when merging in Pull Requests and rename the commit to follow the format: _type_(_scope_): _description_. 

__Examples:__ 

`feat(Candidate): Added field 'xyz'`

`fix(To-Many Associations): fixed xyz`

`chore(cleanup): cleaned up xyz`

`chore(release): X.Y.Z`

### Creating a Release

[standard-version](https://www.npmjs.com/package/standard-version) is used to calculate the version number, update the `package.json` version, 
generate the `CHANGELOG.md` file, generate a GitHub release, and push tags to Master, which allows TravisCI to generate the build artifacts and upload them to a GitHub release.

 1. Checkout master
  
 2. Run release script: `npm run release`
 
 3. Create the wiki zipfile which includes PDFs of the wiki pages
 
    1. Clone the wiki repo: `git clone https://github.com/bullhorn/dataloader.wiki.git`
     
    2. Download conversion utilities: `npm install`

    3. Create the wiki.zip file: `npm start`

 4. Create release in GitHub

    1. From the [Releases Page](https://github.com/bullhorn/dataloader/releases) click [Draft a New Release](https://github.com/bullhorn/dataloader/releases/new).
    
    2. Set the Tag Version and Release Title to the version number prepended with a `v`, like: `v1.2.3`.
    
    3. In the Attach Binaries section of the release page, attach the release package file: `target/dataloader.zip`
 
    4. In the Attach Binaries section of the release page, attach the wiki package file: `wiki.zip`

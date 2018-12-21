## Examples Directory



### Prerequisites

These example CSV files reference several reference only entities that must exist in the CRM Corp already for these examples to load. Here is the list:

* CorporateUser
    * Recruiter CorporateUser
    * Manager CorporateUser
    * Sales CorporateUser
    * Disabled CorporateUser (Disable this user)

* BusinessSector
    * BusinessSector1
    * BusinessSector2
    * BusinessSector3

* Category
    * Category1
        * Specialty1
        * Specialty2
    * Category2
        * Skill1
        * Skill2
        * Skill3
        * Skill4
    * Category3
    * 1001 Skills
        * Skill_1 ... Skill_1001 (copy/paste from associationsOver500 integration test)

* Certification
    * Certification1
    * Certification2

* Corp Settings
    * Enable leadAndOpportunityEnabled
    
* Custom Objects
    * Enable ClientCorporation Custom Object Instance 1
    * Enable JobOrder Custom Object Instance 1
    * Enable Opportunity Custom Object Instance 1
    * Enable Person Custom Object Instance 1
    * Enable Placement Custom Object Instance 1
    
* Action Entitlements
    * Enable View for all Entities
    * Enable Add for all Entities
    * Enable Edit for all Entities
        * Do Not Enable `View/EditMyOwnRecord - <Entity>` entitlements, because they will lock down edit/delete for records owned by someone else.
    * Enable Edit Owner for all Entities
    * Enable Edit Confidential Data

### About These Example Files

The maximal number of fields have been filled out in these examples, for as many entities as can be loaded. These files are as interconnected as possible, making use of as many association fields as possible. The `externalID` field is used if present, otherwise `customText1` is used to denote the external unique identifier for data that is being loaded. These are the same default exist fields that are used in section 3 of the `dataloader.properties` file, so that updating instead of inserting using these example files requires simply uncommenting the commented out exist fields in the properties file.  

In order to ensure that all fields have been utilized, after updating the SDK-REST, run the command: `dataloader template <InputFile.csv>` for each example file in order to see which fields are missing or should be removed from the example file. For example: `dataloader template examples/load/Candidate.csv`. Not all fields can be added to the example file, but most can be.

### Integration Test 

To perform a manual integration test (testing the integration between the DataLoader and the actual Rest API), do the following:

 1. Do a find/replace in the entire `examples` folder to replace the text: `-ext-1` with `-ext-<some unique number>`. This will ensure that all files will load and properly reference each other without conflicting. If run multiple times with the same external ids, the associations will fail because of the duplicates that exist.
 2. Load from directory: `dataloader load examples/load`
 3. Ensure that there are no errors and all entities have loaded successfully
 4. Delete all of the loaded records using the results files: `dataloader delete results`

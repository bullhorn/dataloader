const { execSync } = require('child_process');
const log = console.log;

let package_json = require('./package');
let version = package_json.version;

log(`Updating pom.xml version to latest version in package.json: ${version}`);
execSync('mvn versions:set -DnewVersion=' + version);
execSync('mvn versions:commit');

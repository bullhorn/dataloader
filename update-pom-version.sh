#!/usr/bin/env bash
str=`cat package.json`;
version="$(node -pe "JSON.parse(\`$str\`)['version']")"
echo Updating pom.xml version to latest version in package.json: $version
mvn versions:set -DnewVersion=$version

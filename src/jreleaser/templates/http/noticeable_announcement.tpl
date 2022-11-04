JReleaser {{projectVersion}} is now available! This is a point fix release mostly related to Maven concerns.

**Deploy:** Fix a timing issue with the Nexus2 Maven deployer that hindered publication to Maven Central. JReleaser now queries the state of a transitioning repository before marking the operation as completed.

**Maven:** The Maven plugin required an explicit dependency on `commons-io` causing a classloading issue it that wasn't the case.

[🚀Changelog](https://github.com/jreleaser/jreleaser/releases/tag/{{tagName}})
JReleaser {{projectVersion}} is now available! This release brings the following features:

This is mostly a fix release with a handful of enhancements. We'd like to thank our most recent [contributors](https://github.com/jreleaser/jreleaser/blob/main/CONTRIBUTORS.md)

**Deploy:** Updates to Maven Central deployer, allowing further customizations in staged builds. Let all Maven deployers configure if they support snapshots or not. Default is set to _false_.

**Catalog:** Support GitHub attestation by generating a file with all files that should be attested {{projectLinkDocumentation}}/reference/catalog/github.html

**Assemble:** Java Archive and Jlink assemblers support defining environment variables and System properties in custom launch scripts.

[🚀 Changelog](https://github.com/jreleaser/jreleaser/releases/tag/{{tagName}}) [📝 Milestone]({{projectLinkVcsBrowser}}/milestone/31?closed=1)

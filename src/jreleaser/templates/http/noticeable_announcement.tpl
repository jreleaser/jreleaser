JReleaser {{projectVersion}} is now available! This release brings the following features:

This is mostly a fix release with a handful of enhancements. We'd like to thank our most recent [contributors](https://github.com/jreleaser/jreleaser/blob/main/CONTRIBUTORS.md)

**Deploy:** Support Sonatype's improved Portal Publisher API {{projectLinkDocumentation}}/reference/deploy/maven/maven-central.html
Handle Maven 4's build & consumer POM files, transparently.

**Package:** Support projectIdentifier on GitLab repositories related to packagers.

**Core:** Log network calls made via Feign with a new magic property named "jreleaser.feign.logger.level".

[🚀 Changelog](https://github.com/jreleaser/jreleaser/releases/tag/{{tagName}}) [📝 Milestone]({{projectLinkVcsBrowser}}/milestone/31?closed=1)

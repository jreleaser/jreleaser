JReleaser {{projectVersion}} is now available! This release brings plenty of features and bug fixes

**Deploy:** Adjustments to Nexus2 support now let you deploy snapshot artifacts to Nexus2 servers and Maven Central.

**Mastodon:** Message threads can now be posted to Mastodon. You may set a list of messages or a path to a file that
contains the messages.

**Docker*:** Multi-platform support is now available via buildx. For the time being only JAVA_BINARY and SINGLE_JAR
distributions are supported.

**Assemble:** The new `java-archive` assembler may be used to create JAVA_BINARY distrirbutions instead of using Maven's
appassembler/assembly plugins or Gradle's application/distribution plugins.

**Changelog:** Conventional-Commit preset can handle BREAKING CHANGE footer, referenced issues, and additional trailing
elements. Be ware that it now also applies a custom format by default.

**GitLab:** Update existing assets when posting an updated release. This aligns GitLab support with GitHub/Gitea.

[🚀Changelog](https://github.com/jreleaser/jreleaser/releases/tag/{{tagName}})
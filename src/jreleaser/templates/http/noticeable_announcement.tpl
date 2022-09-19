JReleaser {{projectVersion}} is now available! This release adds the much awaited publication to Maven Central feature, and much more.

**Deploy:** New [deploy]({{projectLinkDocumentation}}/configuration/deploy.html) step lets you deploy JARs & POMs to: Sonatype Nexus2, JFrog Artifactory, Github Packages, Gitlab Packages, Gitea Packages. You may also deploy to Maven Central using the Nexus2 deployer.

**Extensions:** Extensions are finally available! [Extensions]({{projectLinkDocumentation}}/configuration/extensions.html) let you further customize how releases are handled.

**Release:** Apply milestone, label, and post a comment on all matching [issues]({{projectLinkDocumentation}}/configuration/release/github.html#_issues) belonging to a release.

**Announce:** New generic [HTTP announcer]({{projectLinkDocumentation}}/configuration/announce/http.html), useful with XWiki, Confluence, and other services.

**Templates:** Additional [template functions]({{projectLinkDocumentation}}/configuration/name-templates.html#_functions) such as json formatting and text processing.

[🚀Changelog](https://github.com/jreleaser/jreleaser/releases/tag/{{tagName}})
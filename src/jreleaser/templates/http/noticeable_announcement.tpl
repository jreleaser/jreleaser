JReleaser {{projectVersion}} is now available! This release brings the following features:

**Announce:** Post announcements to [OpenCollective](https://opencollective.com/).

**Catalog:** [cyclonedx-cli](https://github.com/CycloneDX/cyclonedx-cli) may be used to generate SBOMs.

**Package:** Several improvements and fixes applied to [Flatpak]{{projectLinkDocumentation}}/reference/packagers/flatpak.html. `SINGLE_JAR`
distributions are now supported by [Macports]({{projectLinkDocumentation}}/reference/packagers/macports.html). Zip
based distributions are now supported by [Winget]({{projectLinkDocumentation}}/reference/packagers/winget.html).
Jib is now supported as a packager; you may use it to build container images {{projectLinkDocumentation}}/reference/packagers/jib.html

**Deploy:** Several updates to [Maven]({{projectLinkDocumentation}}/reference/deploy/maven/index.html) deployers. You may now individually
override all checks defined by the `applyMavenCentralRules` property, skip checking for `-sources` and `-javadoc` JARs on a given
artifact. Nexus2 now checks if GPG keys are publicly available before deployment.

**Hooks:** Scripts may be defined directly in the model via [Script Hooks]({{projectLinkDocumentation}}/reference/hooks/script.html).
Command and script hooks may supply structured output that feeds into the resolved model {{projectLinkDocumentation}}/reference/hooks/command.html#_output.
They may also be conditionally executed {{projectLinkDocumentation}}/reference/hooks/command.html#_conditions.

[🚀 Changelog](https://github.com/jreleaser/jreleaser/releases/tag/{{tagName}}) [📝 Milestone]({{projectLinkVcsBrowser}}/milestone/25?closed=1)

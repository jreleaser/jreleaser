JReleaser {{projectVersion}} is now available! This release brings the following features:

**Core:** The [matrix]({{projectLinkDocumentation}}/reference/matrix.html) section can be used to further parameterize
[hooks]({{projectLinkDocumentation}}/reference/hooks/index.html) and [assemblers]({{projectLinkDocumentation}}/reference/assemble/index.html).
Quite useful for projects that support cross-compilation such as Go, Rust, and Zig.

**Assemble**: A new assembler, [debian]({{projectLinkDocumentation}}/reference/assemble/deb.html), lets you create `.deb`
archives from several inputs regardless of the source language. Works on all platforms and does not bundle a Java runtime like
the [jpackage]({{projectLinkDocumentation}}/reference/assemble/jpackage.html) assembler does.

**Packagers:** Updates to the [Winget]({{projectLinkDocumentation}}/reference/packagers/winget.html) templates to smooth
out publication. JReleaser itself is now available via Winget.

**Deploy:** Several updates to Maven deployers, enabling further customizations and additional checks.

[🚀 Changelog](https://github.com/jreleaser/jreleaser/releases/tag/{{tagName}}) [📝 Milestone]({{projectLinkVcsBrowser}}/milestone/36?closed=1)

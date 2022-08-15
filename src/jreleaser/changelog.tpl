## Changelog

{{changelogChanges}}
{{changelogContributors}}

## Binaries

### 🌟 Universal

These binaries require an external Java runtime.

* {{#f_release_download_url}}jreleaser-{{projectEffectiveVersion}}.zip{{/f_release_download_url}} (requires Java 8+)
* {{#f_release_download_url}}jreleaser-{{projectEffectiveVersion}}.tar{{/f_release_download_url}} (requires Java 8+)
* {{#f_release_download_url}}jreleaser-ant-tasks-{{projectEffectiveVersion}}.zip{{/f_release_download_url}} (requires Java 8+)
* {{#f_release_download_url}}jreleaser-tool-provider-{{projectEffectiveVersion}}.jar{{/f_release_download_url}} (requires Java 11+)

### ☕️ Bundled Java Runtimes

These binaries provide their own Java runtime.

|Platform | Intel | Arm |
| ------- | ----- | --- |
| MacOS   | {{#f_release_download_url}}jreleaser-standalone-{{projectEffectiveVersion}}-osx-x86_64.zip{{/f_release_download_url}} | {{#f_release_download_url}}jreleaser-standalone-{{projectEffectiveVersion}}-osx-aarch64.zip{{/f_release_download_url}} |
| Linux (glibc) | {{#f_release_download_url}}jreleaser-standalone-{{projectEffectiveVersion}}-linux-x86_64.zip{{/f_release_download_url}} | {{#f_release_download_url}}jreleaser-standalone-{{projectEffectiveVersion}}-linux-aarch64.zip{{/f_release_download_url}} |
| Linux (musl) | {{#f_release_download_url}}jreleaser-standalone-{{projectEffectiveVersion}}-linux_musl-x86_64.zip{{/f_release_download_url}} | {{#f_release_download_url}}jreleaser-standalone-{{projectEffectiveVersion}}-linux_musl-aarch64.zip{{/f_release_download_url}} |
| Windows | {{#f_release_download_url}}jreleaser-standalone-{{projectEffectiveVersion}}-windows-x86_64.zip{{/f_release_download_url}} | {{#f_release_download_url}}jreleaser-standalone-{{projectEffectiveVersion}}-windows-aarch64.zip{{/f_release_download_url}} |

### 📦 Installers

These binaries provide their own Java runtime.

* {{#f_release_download_url}}jreleaser-installer-{{projectEffectiveVersion}}-osx-x86_64.pkg{{/f_release_download_url}}
* {{#f_release_download_url}}jreleaser-installer-{{projectEffectiveVersion}}-1_amd64.deb{{/f_release_download_url}}
* {{#f_release_download_url}}jreleaser-installer-{{projectEffectiveVersion}}-1.x86_64.rpm{{/f_release_download_url}}
* {{#f_release_download_url}}jreleaser-installer-{{projectEffectiveVersion}}-windows-x86_64.msi{{/f_release_download_url}}

### 💻 Native Executables

* {{#f_release_download_url}}jreleaser-native-{{projectEffectiveVersion}}-osx-x86_64.zip{{/f_release_download_url}}
* {{#f_release_download_url}}jreleaser-native-{{projectEffectiveVersion}}-linux-x86_64.zip{{/f_release_download_url}}

### Checksums

```
{{#f_trim}}{{#f_file_read}}{{checksumDirectory}}/checksums_sha256.txt{{/f_file_read}}{{/f_trim}}
```
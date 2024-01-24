Published: {{timestamp}}

## Release Notes

[{{releaseNotesUrl}}]({{releaseNotesUrl}})

## Binaries

### 🌟 Universal

These binaries require an external Java runtime.

* {{#f_release_download_url}}jreleaser-{{projectEffectiveVersion}}.zip{{/f_release_download_url}} ({{#f_release_download_url}}asc:jreleaser-{{projectEffectiveVersion}}.zip.asc{{/f_release_download_url}}) **Requires Java 8+**
  sha256:jreleaser-{{projectEffectiveVersion}}.zip
* {{#f_release_download_url}}jreleaser-{{projectEffectiveVersion}}.tar{{/f_release_download_url}} ({{#f_release_download_url}}asc:jreleaser-{{projectEffectiveVersion}}.tar.asc{{/f_release_download_url}}) **Requires Java 8+**
  sha256:jreleaser-{{projectEffectiveVersion}}.tar
* {{#f_release_download_url}}jreleaser-ant-tasks-{{projectEffectiveVersion}}.zip{{/f_release_download_url}} ({{#f_release_download_url}}asc:jreleaser-ant-tasks-{{projectEffectiveVersion}}.zip.asc{{/f_release_download_url}}) **Requires Java 8+**
  sha256:jreleaser-ant-tasks-{{projectEffectiveVersion}}.zip
* {{#f_release_download_url}}jreleaser-tool-provider-{{projectEffectiveVersion}}.jar{{/f_release_download_url}} ({{#f_release_download_url}}asc:jreleaser-tool-provider-{{projectEffectiveVersion}}.jar.asc{{/f_release_download_url}}) **Requires Java 11+**
  sha256:jreleaser-tool-provider-{{projectEffectiveVersion}}.jar

### ☕️ Bundled Java Runtimes

These binaries provide their own Java runtime.

|Platform              | Artifact |
| -------------------- | -------- |
| MacOS x86_64         | {{#f_release_download_url}}jreleaser-standalone-{{projectEffectiveVersion}}-osx-x86_64.zip{{/f_release_download_url}} ({{#f_release_download_url}}asc:jreleaser-standalone-{{projectEffectiveVersion}}-osx-x86_64.zip.asc{{/f_release_download_url}})<br/>sha256:jreleaser-standalone-{{projectEffectiveVersion}}-osx-x86_64.zip |
| MacOS Arm64          | {{#f_release_download_url}}jreleaser-standalone-{{projectEffectiveVersion}}-osx-aarch64.zip{{/f_release_download_url}} ({{#f_release_download_url}}asc:jreleaser-standalone-{{projectEffectiveVersion}}-osx-aarch64.zip.asc{{/f_release_download_url}})<br/>sha256:jreleaser-standalone-{{projectEffectiveVersion}}-osx-aarch64.zip |
| Linux x86_64 (musl)  | {{#f_release_download_url}}jreleaser-standalone-{{projectEffectiveVersion}}-linux_musl-x86_64.zip{{/f_release_download_url}} ({{#f_release_download_url}}asc:jreleaser-standalone-{{projectEffectiveVersion}}-linux_musl-x86_64.zip.asc{{/f_release_download_url}})<br/>sha256:jreleaser-standalone-{{projectEffectiveVersion}}-linux_musl-x86_64.zip |
| Linux Arm64 (musl)   | {{#f_release_download_url}}jreleaser-standalone-{{projectEffectiveVersion}}-linux_musl-aarch64.zip{{/f_release_download_url}} ({{#f_release_download_url}}asc:jreleaser-standalone-{{projectEffectiveVersion}}-linux_musl-aarch64.zip.asc{{/f_release_download_url}})<br/>sha256:jreleaser-standalone-{{projectEffectiveVersion}}-linux_musl-aarch64.zip |
| Linux x86_64 (glibc) | {{#f_release_download_url}}jreleaser-standalone-{{projectEffectiveVersion}}-linux-x86_64.zip{{/f_release_download_url}} ({{#f_release_download_url}}asc:jreleaser-standalone-{{projectEffectiveVersion}}-linux-x86_64.zip.asc{{/f_release_download_url}})<br/>sha256:jreleaser-standalone-{{projectEffectiveVersion}}-linux-x86_64.zip |
| Linux Arm64 (glibc)  | {{#f_release_download_url}}jreleaser-standalone-{{projectEffectiveVersion}}-linux-aarch64.zip{{/f_release_download_url}} ({{#f_release_download_url}}asc:jreleaser-standalone-{{projectEffectiveVersion}}-linux-aarch64.zip.asc{{/f_release_download_url}})<br/>sha256:jreleaser-standalone-{{projectEffectiveVersion}}-linux-aarch64.zip |
| Windows x86_64       | {{#f_release_download_url}}jreleaser-standalone-{{projectEffectiveVersion}}-windows-x86_64.zip{{/f_release_download_url}} ({{#f_release_download_url}}asc:jreleaser-standalone-{{projectEffectiveVersion}}-windows-x86_64.zip.asc{{/f_release_download_url}})<br/>sha256:jreleaser-standalone-{{projectEffectiveVersion}}-windows-x86_64.zip |
| Windows Arm64        | {{#f_release_download_url}}jreleaser-standalone-{{projectEffectiveVersion}}-windows-aarch64.zip{{/f_release_download_url}} ({{#f_release_download_url}}asc:jreleaser-standalone-{{projectEffectiveVersion}}-windows-aarch64.zip.asc{{/f_release_download_url}})<br/>sha256:jreleaser-standalone-{{projectEffectiveVersion}}-windows-aarch64.zip |

### 📦 Installers

These binaries provide their own Java runtime.

* {{#f_release_download_url}}jreleaser-installer-{{projectEffectiveVersion}}-osx-x86_64.pkg{{/f_release_download_url}} ({{#f_release_download_url}}asc:jreleaser-installer-{{projectEffectiveVersion}}-osx-x86_64.pkg.asc{{/f_release_download_url}})
  sha256:jreleaser-installer-{{projectEffectiveVersion}}-osx-x86_64.pkg
* {{#f_release_download_url}}jreleaser-installer_{{projectEffectiveVersion}}-1_amd64.deb{{/f_release_download_url}} ({{#f_release_download_url}}asc:jreleaser-installer_{{projectEffectiveVersion}}-1_amd64.deb.asc{{/f_release_download_url}})
  sha256:jreleaser-installer_{{projectEffectiveVersion}}-1_amd64.deb
* {{#f_release_download_url}}jreleaser-installer-{{projectEffectiveVersion}}-1.x86_64.rpm{{/f_release_download_url}} ({{#f_release_download_url}}asc:jreleaser-installer-{{projectEffectiveVersion}}-1.x86_64.rpm.asc{{/f_release_download_url}})
  sha256:jreleaser-installer-{{projectEffectiveVersion}}-1.x86_64.rpm
* {{#f_release_download_url}}jreleaser-installer-{{projectEffectiveVersion}}-windows-x86_64.msi{{/f_release_download_url}} ({{#f_release_download_url}}asc:jreleaser-installer-{{projectEffectiveVersion}}-windows-x86_64.msi.asc{{/f_release_download_url}})
  sha256:jreleaser-installer-{{projectEffectiveVersion}}-windows-x86_64.msi

### 💻 Native Executables

* {{#f_release_download_url}}jreleaser-native-{{projectEffectiveVersion}}-osx-x86_64.zip{{/f_release_download_url}} ({{#f_release_download_url}}asc:jreleaser-native-{{projectEffectiveVersion}}-osx-x86_64.zip.asc{{/f_release_download_url}})
  sha256:jreleaser-native-{{projectEffectiveVersion}}-osx-x86_64.zip
* {{#f_release_download_url}}jreleaser-native-{{projectEffectiveVersion}}-linux-x86_64.zip{{/f_release_download_url}} ({{#f_release_download_url}}asc:jreleaser-native-{{projectEffectiveVersion}}-linux-x86_64.zip.asc{{/f_release_download_url}})
  sha256:jreleaser-native-{{projectEffectiveVersion}}-linux-x86_64.zip

## Verify Provenance
{{#includeSboms}}

### SBOMS

* {{#f_release_download_url}}jreleaser-{{projectEffectiveVersion}}-sboms.zip{{/f_release_download_url}} ({{#f_release_download_url}}asc:jreleaser-{{projectEffectiveVersion}}-sboms.zip.asc{{/f_release_download_url}})
  sha256:jreleaser-{{projectEffectiveVersion}}-sboms.zip
{{/includeSboms}}

### SLSA

1. Install or build the [slsa-verifier](https://github.com/slsa-framework/slsa-verifier) binary.
2. Download {{#f_release_download_url}}jreleaser-all-{{projectEffectiveVersion}}.intoto.jsonl{{/f_release_download_url}}
3. Download the binary or binary files you'd like to verify.
4. Run the verifier against the binary. For example

```sh
$ slsa-verifier verify-artifact jreleaser-{{projectEffectiveVersion}}.zip \
   --provenance-path jreleaser-all-{{projectEffectiveVersion}}.intoto.jsonl \
   --source-uri github.com/jreleaser/jreleaser
Verified signature against tlog entry index 8865454 at URL: https://rekor.sigstore.dev/api/v1/log/entries/24296fb24b8ad77acceaa92d35076867e961260048db8f9ee7726329e5a14ae3a6cfd678aeacad11
Verified build using builder https://github.com/slsa-framework/slsa-github-generator/.github/workflows/generator_generic_slsa3.yml@refs/tags/v1.4.0 at commit caa516c7c52ca72a352f97e4153334080f8b7f43
PASSED: Verified SLSA provenance
```

### PGP

1. Download the [public key](http://keyserver.ubuntu.com/pks/lookup?op=get&search=0xf1d5f6a91c86b0702cd0734bccc55c5167419adb)
2. Verify the fingerprint matches the following:
```sh
$ gpg --show-keys aalmiray.asc
pub   rsa4096 2021-02-10 [SC] [expires: 2031-02-08]
      F1D5F6A91C86B0702CD0734BCCC55C5167419ADB
uid                      Andres Almiray <aalmiray@gmail.com>
sub   rsa4096 2021-02-10 [E] [expires: 2031-02-08]
```
3. Import the key with `gpg --import aalmiray.asc`.
4. Verify the chosen artifact with:
```sh
$ gpg --verify jreleaser-{{projectEffectiveVersion}}.zip.asc jreleaser-{{projectEffectiveVersion}}.zip
gpg: Signature made Tue Dec 13 06:51:49 2022 CET
gpg:                using RSA key CCC55C5167419ADB
gpg: Good signature from "Andres Almiray <aalmiray@gmail.com>" [ultimate]
```

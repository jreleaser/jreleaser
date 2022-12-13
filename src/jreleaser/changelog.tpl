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
* {{#f_release_download_url}}jreleaser-native-{{projectEffectiveVersion}}-windows-x86_64.zip{{/f_release_download_url}}

## Verify Provenance

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

---

![JReleaser Banner](https://raw.githubusercontent.com/jreleaser/jreleaser/main/src/media/banner_2100x500.png)

Examples and reference may be found at [https://jreleaser.org](https://jreleaser.org).
Would you like to help? You can [sponsor](https://jreleaser.org/guide/early-access/sponsors.html) or [contribute](https://jreleaser.org/guide/early-access/contributing.html).
You can reach us at [Twitter](https://twitter.com/jreleaser) and [Mastodon](https://fosstodon.org/@jreleaser).

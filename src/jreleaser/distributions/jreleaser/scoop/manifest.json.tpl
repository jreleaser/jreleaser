{
    "version": "{{projectVersion}}",
    "description": "{{projectDescription}}",
    "homepage": "{{projectLinkHomepage}}",
    "license": "{{projectLicense}}",
    "suggest": {
        "JDK": [
            "java/oraclejdk",
            "java/openjdk"
        ]
    },
    "url": "{{distributionUrl}}",
    "hash": "sha256:{{distributionChecksumSha256}}",
    "extract_dir": "{{distributionArtifactRootEntryName}}",
    "bin": "bin\\{{distributionExecutableWindows}}",
    "checkver": {
        "github": "{{projectLinkVcsBrowser}}"
    },
    "autoupdate": {
        "url": "{{scoopAutoupdateUrl}}",
        "extract_dir": "{{scoopAutoupdateExtractDir}}",
        "hash": {
            "url": "$url.sha256"
        }
    }
}
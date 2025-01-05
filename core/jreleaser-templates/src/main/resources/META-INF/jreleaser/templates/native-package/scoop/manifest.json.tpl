{
    "version": "{{projectVersion}}",
    "description": "{{projectDescription}}",
    "homepage": "{{projectLinkHomepage}}",
    "license": "{{projectLicense}}",
    "url": "{{distributionUrl}}",
    "hash": "sha256:{{distributionChecksumSha256}}",
    "extract_dir": "{{distributionArtifactRootEntryName}}",
    "bin": "{{distributionName}}.exe",
    "checkver": {
        "url": "{{scoopCheckverUrl}}"
    },
    "autoupdate": {
        "url": "{{scoopAutoupdateUrl}}",
        "hash": {
            "url": "$url.sha256"
        }
    }
}
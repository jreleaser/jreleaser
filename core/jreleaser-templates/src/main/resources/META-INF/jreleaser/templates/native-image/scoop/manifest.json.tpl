{
    "version": "{{projectVersion}}",
    "description": "{{projectDescription}}",
    "homepage": "{{projectLinkHomepage}}",
    "license": "{{projectLicense}}",
    "url": "{{distributionUrl}}",
    "hash": "sha256:{{distributionChecksumSha256}}",
    "extract_dir": "{{distributionArtifactRootEntryName}}",
    "env_add_path": "bin",
    "checkver": {
        "url": "{{scoopCheckverUrl}}",
        "re": "v([\\d.]+).zip"
    },
    "autoupdate": {
        "url": "{{scoopAutoupdateUrl}}",
        "extract_dir": "{{scoopAutoupdateExtractDir}}",
        "hash": {
            "url": "$url.sha256"
        }
    }
}
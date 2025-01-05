{
    "version": "{{projectVersion}}",
    "description": "{{projectDescription}}",
    "homepage": "{{projectLinkHomepage}}",
    "license": "{{projectLicense}}",
    "url": "{{distributionUrl}}",
    "hash": "sha256:{{distributionChecksumSha256}}",
    "bin": "{{distributionExecutableWindows}}",
    "checkver": {
        "url": "{{scoopCheckverUrl}}",
        "re": "v([\\d.]+)"
    },
    "autoupdate": {
        "url": "{{scoopAutoupdateUrl}}",
        "hash": {
            "url": "$url.sha256"
        }
    }
}
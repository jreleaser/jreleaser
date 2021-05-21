{
  "homepage": "{{projectWebsite}}",
  "description": "{{projectDescription}}",
  "version": "{{projectVersion}}",
  "license": "{{projectLicense}}",
  "url": "{{distributionUrl}}",
  "hash": "sha256:{{distributionChecksumSha256}}",
  "bin": "{{artifactFileName}}",
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
{
  "homepage": "{{projectWebsite}}",
  "description": "{{projectDescription}}",
  "version": "{{projectVersion}}",
  "license": "{{projectLicense}}",
  "url": "{{distributionUrl}}",
  "hash": "sha256:{{distributionChecksumSha256}}",
  "extract_dir": "{{distributionArtifactFileName}}",
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
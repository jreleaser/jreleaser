{
  "version": "{{projectVersion}}",
  "description": "{{projectDescription}}",
  "homepage": "{{projectWebsite}}",
  "license": "{{projectLicense}}",
  "url": "{{distributionUrl}}",
  "hash": "sha256:{{distributionChecksumSha256}}",
  "extract_dir": "{{distributionArtifactFileName}}",
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
{
  "homepage": "{{projectWebsite}}",
  "description": "{{projectDescription}}",
  "version": "{{projectVersion}}",
  "license": "{{projectLicense}}",
  "url": "{{distributionUrl}}",
  "hash": "sha256:{{distributionChecksumSha256}}",
  "extract_dir": "{{distributionArtifactFileName}}",
  "env_add_path": "bin",
  "suggest": {
    "JDK": [
      "java/oraclejdk",
      "java/openjdk"
    ]
  },
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
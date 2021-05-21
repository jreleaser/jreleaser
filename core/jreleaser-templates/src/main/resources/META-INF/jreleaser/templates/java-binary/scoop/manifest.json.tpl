{
  "homepage": "{{projectWebsite}}",
  "description": "{{projectDescription}}",
  "version": "{{projectVersion}}",
  "license": "{{projectLicense}}",
  "url": "{{distributionUrl}}",
  "hash": "sha256:{{distributionChecksumSha256}}",
  "extract_dir": "{{projectName}}-{{projectVersion}}",
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
    "extract_dir": "{{projectName}}-$version",
    "hash": {
      "url": "$url.sha256"
    }
  }
}
{
  "homepage": "{{projectWebsite}}",
  "description": "{{projectDescription}}",
  "version": "{{projectVersion}}",
  "license": "{{projectLicense}}",
  "url": "{{distributionUrl}}",
  "hash": "sha256:{{distributionChecksumSha256}}",
  "suggest": {
    "JDK": [
      "java/oraclejdk",
      "java/openjdk"
    ]
  },
  "pre_install": "\"@java -jar \"\"$dir\\{{distributionArtifactFile}}\"\" %*\" | out-file -en oem \"$dir\\{{distributionExecutable}}.{{distributionExecutableExtension}}\"",
  "bin": "{{distributionExecutable}}.{{distributionExecutableExtension}}",
  "checkver": {
    "url": "{{scoopCheckverUrl}}",
    "re": "v([\\d.]+).jar"
  },
  "autoupdate": {
    "url": "{{scoopAutoupdateUrl}}",
    "hash": {
      "url": "$url.sha256"
    }
  }
}
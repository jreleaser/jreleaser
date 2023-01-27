{
  "version": "{{projectVersion}}",
  "description": "{{projectDescription}}",
  "homepage": "{{projectLinkHomepage}}",
  "license": "{{projectLicense}}",
  "url": "{{distributionUrl}}",
  "hash": "sha256:{{distributionChecksumSha256}}",
  "suggest": {
    "JDK": [
      "java/oraclejdk",
      "java/openjdk"
    ]
  },
  "pre_install": "\"@java -jar \"\"$dir\\{{distributionArtifactFile}}\"\" %*\" | out-file -en oem \"$dir\\{{distributionExecutableWindows}}\"",
  "bin": "{{distributionExecutableWindows}}",
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
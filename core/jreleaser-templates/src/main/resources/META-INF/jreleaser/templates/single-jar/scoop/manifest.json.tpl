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
    {{#distributionJavaMainModule}}
    "pre_install": "\"@java -p \"\"$dir\\{{distributionArtifactFile}}\"\" -m \"{{distributionJavaMainModule}}/{{distributionJavaMainClass}}\" %*\" | out-file -en oem \"$dir\\{{distributionExecutableWindows}}\"",
    {{/distributionJavaMainModule}}
    {{^distributionJavaMainModule}}
    "pre_install": "\"@java -jar \"\"$dir\\{{distributionArtifactFile}}\"\" %*\" | out-file -en oem \"$dir\\{{distributionExecutableWindows}}\"",
    {{/distributionJavaMainModule}}
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
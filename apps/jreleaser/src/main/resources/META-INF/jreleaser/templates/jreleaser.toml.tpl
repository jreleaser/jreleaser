[project]
  name = "app"
  version = "1.0.0-SNAPSHOT"
  description = "Awesome App"
  longDescription = "Awesome App"
  website = "https://acme.com/app"
  authors = ["Duke"]
  license = "Apache-2"
  java.groupId = "com.acme"
  java.version = "8"
  extraProperties.inceptionYear = "@year@"

[release.github]
  owner = "duke"

[distributions.app]
  artifacts = [
    { path = "path/to/{{distributionName}}-{{projectVersion}}.zip" }
  ]
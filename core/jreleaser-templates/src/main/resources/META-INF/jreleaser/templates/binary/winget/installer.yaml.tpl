# {{jreleaserCreationStamp}}
# yaml-language-server: $schema=https://aka.ms/winget-manifest.installer.1.4.0.schema.json

PackageIdentifier: {{wingetPackageIdentifier}}
PackageVersion: {{wingetPackageVersion}}
ReleaseDate: {{wingetReleaseDate}}
Installers:
  - Architecture: neutral
    InstallerUrl: {{distributionUrl}}
    InstallerSha256: {{distributionChecksumSha256}}
    InstallerType: zip
    NestedInstallerType: portable
    NestedInstallerFiles:
      - RelativeFilePath: {{distributionArtifactRootEntryName}}\bin\{{distributionExecutableWindows}}
        PortableCommandAlias: {{distributionExecutableName}}
    {{#wingetHasDependencies}}
    Dependencies:
      {{#wingetHasWindowsFeatures}}
      WindowsFeatures:
        {{#wingetWindowsFeatures}}- {{.}}{{/wingetWindowsFeatures}}
      {{/wingetHasWindowsFeatures}}
      {{#wingetHasWindowsLibraries}}
      WindowsLibraries:
        {{#wingetWindowsLibraries}}- {{.}}{{/wingetWindowsLibraries}}
      {{/wingetHasWindowsLibraries}}
      {{#wingetHasExternalDependencies}}
      ExternalDependencies:
        {{#wingetExternalDependencies}}- {{.}}{{/wingetExternalDependencies}}
      {{/wingetHasExternalDependencies}}
      {{#wingetHasPackageDependencies}}
      PackageDependencies:
        {{#wingetPackageDependencies}}
        - PackageIdentifier: {{packageIdentifier}}
          {{#minimumVersion}}MinimumVersion: {{.}}{{/minimumVersion}}
        {{/wingetPackageDependencies}}
      {{/wingetHasPackageDependencies}}
    {{/wingetHasDependencies}}
ManifestType: installer
ManifestVersion: 1.6.0

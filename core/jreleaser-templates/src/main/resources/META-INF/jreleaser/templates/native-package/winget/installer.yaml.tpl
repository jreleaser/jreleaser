# {{jreleaserCreationStamp}}
# yaml-language-server: $schema=https://aka.ms/winget-manifest.installer.1.9.0.schema.json

PackageIdentifier: {{wingetPackageIdentifier}}
PackageVersion: {{wingetPackageVersion}}
MinimumOSVersion: {{wingetMinimumOsVersion}}
InstallerType: {{wingetInstallerType}}
Scope: {{wingetScope}}
InstallModes:
{{#wingetInstallModes}}
  - {{.}}
{{/wingetInstallModes}}
UpgradeBehavior: {{wingetUpgradeBehavior}}
Commands:
  - {{distributionExecutableName}}
ReleaseDate: {{wingetReleaseDate}}
Installers:
  - Architecture: {{wingetInstallerArchitecture}}
    InstallerUrl: {{distributionUrl}}
    InstallerSha256: {{distributionChecksumSha256}}
    ProductCode: '{{wingetProductCode}}'
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
ManifestVersion: 1.9.0

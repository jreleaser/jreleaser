# {{jreleaserCreationStamp}}
# yaml-language-server: $schema=https://aka.ms/winget-manifest.installer.1.1.0.schema.json

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
ManifestType: installer
ManifestVersion: 1.1.0

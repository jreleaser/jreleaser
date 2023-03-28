# {{jreleaserCreationStamp}}
# yaml-language-server: $schema=https://aka.ms/winget-manifest.installer.1.4.0.schema.json

PackageIdentifier: {{wingetPackageIdentifier}}
PackageVersion: {{wingetPackageVersion}}
ReleaseDate: {{wingetReleaseDate}}
Installers:
{{#wingetInstallers}}
  - Architecture: {{architecture}}
    InstallerUrl: {{url}}
    InstallerSha256: {{checksum}}
    InstallerType: zip
    NestedInstallerType: portable
    NestedInstallerFiles:
      - RelativeFilePath: {{executablePath}}
        PortableCommandAlias: {{executableName}}
{{/wingetInstallers}}
ManifestType: installer
ManifestVersion: 1.4.0

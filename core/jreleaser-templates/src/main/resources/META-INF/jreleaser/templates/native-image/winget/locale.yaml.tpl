# {{jreleaserCreationStamp}}
# yaml-language-server: $schema=https://aka.ms/winget-manifest.defaultLocale.1.4.0.schema.json

PackageIdentifier: {{wingetPackageIdentifier}}
PackageVersion: {{wingetPackageVersion}}
PackageLocale: {{wingetPackageLocale}}
Publisher: {{wingetPackagePublisher}}
PublisherUrl: {{wingetPublisherUrl}}
PublisherSupportUrl: {{wingetPublisherSupportUrl}}
Author: {{wingetAuthor}}
PackageName: {{wingetPackageName}}
PackageUrl: {{wingetPackageUrl}}
License: {{projectLicense}}
LicenseUrl: {{projectLinkLicense}}
Copyright: {{projectCopyright}}
ShortDescription: {{projectDescription}}
Description: {{projectLongDescription}}
Moniker: {{wingetMoniker}}
{{#wingetHasTags}}
Tags:
  {{#wingetTags}}
  - {{.}}
  {{/wingetTags}}
{{/wingetHasTags}}
ReleaseNotesUrl: {{releaseNotesUrl}}
ManifestType: {{wingetManifestType}}
ManifestVersion: 1.4.0

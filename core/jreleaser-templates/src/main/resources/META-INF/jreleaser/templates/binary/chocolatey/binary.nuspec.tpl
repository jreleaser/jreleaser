<?xml version="1.0" encoding="utf-8"?>
<!-- {{jreleaserCreationStamp}} -->
<!-- Do not remove this test for UTF-8: if “Ω” doesn’t appear as greek uppercase omega letter enclosed in quotation marks, you should use an editor that supports UTF-8, not this one. -->
<package xmlns="http://schemas.microsoft.com/packaging/2015/06/nuspec.xsd">
  <metadata>
    <!-- required -->
    <id>{{chocolateyPackageName}}</id>
    <version>{{chocolateyPackageVersion}}</version>
    <authors>{{projectAuthorsByComma}}</authors>
    <description>{{projectLongDescription}}</description>
    <!-- optional -->
    <title>{{chocolateyTitle}}</title>
    <projectUrl>{{projectLinkHomepage}}</projectUrl>
    <copyright>{{projectCopyright}}</copyright>
    <licenseUrl>{{projectLinkLicense}}</licenseUrl>
    <requireLicenseAcceptance>false</requireLicenseAcceptance>
    <tags>{{distributionTagsBySpace}}</tags>
    <summary>{{projectDescription}}</summary>
    <projectSourceUrl>{{repoUrl}}</projectSourceUrl>
    <packageSourceUrl>{{chocolateyPackageSourceUrl}}</packageSourceUrl>
    <docsUrl>{{projectLinkDocumentation}}</docsUrl>
    <bugTrackerUrl>{{projectLinkBugTracker}}</bugTrackerUrl>
    <releaseNotes>{{releaseNotesUrl}}</releaseNotes>
    {{#chocolateyIconUrl}}<iconUrl>{{.}}</iconUrl>{{/chocolateyIconUrl}}
  </metadata>
  <files>
    <file src="tools\**" target="tools" />
  </files>
</package>

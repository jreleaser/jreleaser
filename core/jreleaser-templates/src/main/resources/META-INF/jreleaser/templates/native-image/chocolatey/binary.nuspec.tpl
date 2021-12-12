<?xml version="1.0" encoding="utf-8"?>
<!-- [JRELEASER_VERSION] -->
<!-- Do not remove this test for UTF-8: if “Ω” doesn’t appear as greek uppercase omega letter enclosed in quotation marks, you should use an editor that supports UTF-8, not this one. -->
<package xmlns="http://schemas.microsoft.com/packaging/2015/06/nuspec.xsd">
  <metadata>
    <!-- required -->
    <id>{{chocolateyPackageName}}</id>
    <version>{{projectVersion}}</version>
    <authors>{{projectAuthorsByComma}}</authors>
    <description>{{projectLongDescription}}</description>
    <!-- optional -->
    <title>{{chocolateyTitle}}</title>
    <projectUrl>{{projectWebsite}}</projectUrl>
    <copyright>{{projectCopyright}}</copyright>
    <licenseUrl>{{projectLicenseUrl}}</licenseUrl>
    <requireLicenseAcceptance>false</requireLicenseAcceptance>
    <tags>{{distributionTagsBySpace}}</tags>
    <summary>{{projectDescription}}</summary>
    <projectSourceUrl>{{repoUrl}}</projectSourceUrl>
    <packageSourceUrl>{{chocolateyBucketRepoUrl}}</packageSourceUrl>
    <docsUrl>{{projectDocsUrl}}</docsUrl>
    <bugTrackerUrl>{{issueTrackerUrl}}</bugTrackerUrl>
    <releaseNotes>{{releaseNotesUrl}}</releaseNotes>
    {{#chocolateyIconUrl}}<iconUrl>{{.}}</iconUrl>{{/chocolateyIconUrl}}
  </metadata>
  <files>
    <file src="tools\**" target="tools" />
  </files>
</package>

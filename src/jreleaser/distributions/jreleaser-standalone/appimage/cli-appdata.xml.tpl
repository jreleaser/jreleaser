<?xml version="1.0" encoding="UTF-8"?>
<!-- Generated with JReleaser 1.2.0-SNAPSHOT at 2022-08-13T23:17:43.641+02:00 -->
<component type="console-application">
  <id>{{appImageComponentId}}</id>
  <name>JReleaser</name>
  <summary>{{projectDescription}}</summary>
  <metadata_license>CC0-1.0</metadata_license>
  <project_license>{{projectLicense}}</project_license>
  <description>
    {{#f_md2html}}{{projectLongDescription}}{{/f_md2html}}
  </description>
  <categories>
    {{#appImageCategories}}
    <category>{{.}}</category>
    {{/appImageCategories}}
  </categories>
  <provides>
    <binary>{{distributionExecutableName}}</binary>
  </provides>
  <releases>
    {{#appImageReleases}}
    <release version="{{version}}" date="{{date}}">
      <url>{{url}}</url>
    </release>
    {{/appImageReleases}}
  </releases>
  <developer_name>{{appImageDeveloperName}}</developer_name>
  <screenshots>
     {{#appImageScreenshots}}
     <screenshot{{#primary}} type="default"{{/primary}}>
       {{#caption}}<caption>{{caption}}</caption>{{/caption}}
       <image type="{{type}}"{{#width}} width="{{width}}"{{/width}}{{#height}} height="{{height}}"{{/height}}>{{url}}</image>
     </screenshot>
     {{/appImageScreenshots}}
  </screenshots>
  <content_rating type="oars-1.1" />
  {{#appImageUrls}}
  <url type="{{type}}">{{url}}</url>
  {{/appImageUrls}}
</component>

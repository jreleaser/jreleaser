<?xml version="1.0" encoding="UTF-8"?>
<!-- {{jreleaserCreationStamp}} -->
<component type="desktop-application">
  <id>{{appImageComponentId}}</id>
  <name>{{projectName}}</name>
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
  <developer>
    <id>{{appImageDeveloperId}</id>
    <name>{{appImageDeveloperName}}</name>
  </developer>
  <launchable type="desktop-id">{{distributionExecutableName}}.desktop</launchable>
  <screenshots>
     {{#appImageScreenshots}}
     {{#caption}}
     <screenshot{{#primary}} type="default"{{/primary}}>
       <caption>{{caption}}</caption>
       <image type="{{type}}"{{#width}} width="{{width}}"{{/width}}{{#height}} height="{{height}}"{{/height}}>{{url}}</image>
     </screenshot>
     {{/caption}}
     {{^caption}}
     <screenshot{{#primary}} type="default"{{/primary}}>
       <image type="{{type}}"{{#width}} width="{{width}}"{{/width}}{{#height}} height="{{height}}"{{/height}}>{{url}}</image>
     </screenshot>
     {{/caption}}
     {{/appImageScreenshots}}
  </screenshots>
  <content_rating type="oars-1.1" />
  {{#appImageUrls}}
  <url type="{{type}}">{{url}}</url>
  {{/appImageUrls}}
</component>

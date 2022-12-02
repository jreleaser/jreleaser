<?xml version="1.0" encoding="UTF-8"?>
<!-- {{jreleaserCreationStamp}} -->
<component type="console-application">
  <id>{{flatpakComponentId}}</id>
  <name>{{projectName}}</name>
  <summary>{{projectDescription}}</summary>
  <metadata_license>CC0-1.0</metadata_license>
  <project_license>{{projectLicense}}</project_license>
  <description>
    {{#f_md2html}}{{projectLongDescription}}{{/f_md2html}}
  </description>
  <categories>
    {{#flatpakCategories}}
    <category>{{.}}</category>
    {{/flatpakCategories}}
  </categories>
  <provides>
    <binary>{{distributionExecutableName}}</binary>
  </provides>
  <releases>
    {{#flatpakReleases}}
    <release version="{{version}}" date="{{date}}">
      <url>{{url}}</url>
    </release>
    {{/flatpakReleases}}
  </releases>
  <developer_name>{{flatpakDeveloperName}}</developer_name>
  <screenshots>
     {{#flatpakScreenshots}}
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
     {{/flatpakScreenshots}}
  </screenshots>
  <content_rating type="oars-1.1" />
  {{#flatpakUrls}}
  <url type="{{type}}">{{url}}</url>
  {{/flatpakUrls}}
</component>

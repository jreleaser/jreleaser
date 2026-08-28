# {{jreleaserCreationStamp}}
name: {{snapPackageName}}
version: "{{projectVersion}}"
summary: {{projectDescription}}
description: {{projectLongDescription}}

title: {{projectNameCapitalized}}
license: {{projectLicense}}
{{#projectLinkContact}}
contact: {{.}}
{{/projectLinkContact}}
{{#projectLinkBugTracker}}
issues: {{.}}
{{/projectLinkBugTracker}}
{{#projectLinkVcsBrowser}}
source-code: {{.}}
{{/projectLinkVcsBrowser}}
{{#projectLinkHomepage}}
website: {{.}}
{{/projectLinkHomepage}}

grade: {{snapGrade}}
confinement: {{snapConfinement}}
base: {{snapBase}}
type: app

{{#snapHasPlatforms}}
platforms:
  {{#snapPlatforms}}
  {{key}}:
    build-on: {{value.buildOn}}
    {{#value.hasRunOn}}build-for: {{value.runOn}}{{/value.hasRunOn}}
  {{/snapPlatforms}}
{{/snapHasPlatforms}}
{{#snapHasArchitectures}}
architectures:
  {{#snapArchitectures}}
  - build-on: {{buildOn}}
    {{#hasRunOn}}run-on: {{runOn}}{{/hasRunOn}}
    {{#ignoreError}}build-error: ignore{{/ignoreError}}
  {{/snapArchitectures}}
{{/snapHasArchitectures}}
apps:
  {{distributionExecutableName}}:
    command: bin/{{distributionExecutableUnix}}
    {{#snapHasLocalPlugs}}
    plugs:
      {{#snapLocalPlugs}}
      - {{.}}
      {{/snapLocalPlugs}}
    {{/snapHasLocalPlugs}}
    {{#snapHasLocalSlots}}
    slots:
      {{#snapLocalSlots}}
      - {{.}}
      {{/snapLocalSlots}}
    {{/snapHasLocalSlots}}

{{#snapHasPlugs}}
plugs:
  {{#snapPlugs}}
  {{name}}:
    {{#attrs}}
    {{key}}: {{value}}
    {{/attrs}}
    {{#hasReads}}
    read:
      {{#reads}}
      - {{.}}
      {{/reads}}
    {{/hasReads}}
    {{#hasWrites}}
    write:
      {{#writes}}
      - {{.}}
      {{/writes}}
    {{/hasWrites}}
  {{/snapPlugs}}
{{/snapHasPlugs}}
{{#snapHasSlots}}
slots:
  {{#snapSlots}}
  {{name}}:
    {{#attrs}}
    {{key}}: {{value}}
    {{/attrs}}
    {{#hasReads}}
    reads:
      {{#reads}}
      - {{.}}
      {{/reads}}
    {{/hasReads}}
    {{#hasWrites}}
    writes:
      {{#writes}}
      - {{.}}
      {{/writes}}
    {{/hasWrites}}
  {{/snapSlots}}
{{/snapHasSlots}}
parts:
  {{distributionExecutableName}}:
    plugin: dump
    source: {{distributionUrl}}
    source-type: {{distributionArtifactFileFormat}}
    source-checksum: sha256/{{distributionChecksumSha256}}
    stage-packages:
      - curl

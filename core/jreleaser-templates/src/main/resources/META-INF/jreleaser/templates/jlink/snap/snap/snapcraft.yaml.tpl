# {{jreleaserCreationStamp}}
name: {{snapPackageName}}
version: {{projectVersion}}
summary: {{projectDescription}}
description: {{projectLongDescription}}

grade: {{snapGrade}}
confinement: {{snapConfinement}}
base: {{snapBase}}
type: app

{{#hasArchitectures}}
architectures:
  {{#snapArchitectures}}
  - build-on: {{buildOn}}
    {{#hasRunOn}}run-on: {{runOn}}{{/hasRunOn}}
    {{#ignoreError}}build-error: ignore{{/ignoreError}}
  {{/snapArchitectures}}
{{/hasArchitectures}}
apps:
  {{distributionExecutable}}:
    command: $SNAP/bin/{{distributionExecutable}}
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
    {{#attributes}}
    {{key}}: {{value}}
    {{/attributes}}
  {{/snapPlugs}}
{{/snapHasPlugs}}
{{#snapHasSlots}}
slots:
  {{#snapSlots}}
  {{name}}:
    {{#attributes}}
    {{key}}: {{value}}
    {{/attributes}}
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
  {{/snapSlots }}
{{/snapHasSlots}}
parts:
  {{distributionExecutable}}:
    plugin: dump
    source: {{distributionUrl}}
    source-checksum: sha256/{{distributionChecksumSha256}}
    stage-packages:
      - curl
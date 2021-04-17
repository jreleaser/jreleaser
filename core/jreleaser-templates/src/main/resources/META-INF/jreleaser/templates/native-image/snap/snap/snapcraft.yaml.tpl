name: {{distributionName}}
base: {{snapBase}}
version: {{projectVersion}}
license: {{projectLicense}}
grade: {{snapGrade}}
type: app
confinement: {{snapConfinement}}
summary: {{projectDescription}}
description: {{projectLongDescription}}

apps:
  {{distributionExecutable}}:
    command: $SNAP/{{artifactFileName}}
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
    plugin: nil
    override-build: |
      wget -O $SNAPCRAFT_PART_INSTALL/{{artifactFileName}} {{distributionUrl}}
      snapcraftctl set-version "{{projectVersion}}"
    build-packages:
      - wget

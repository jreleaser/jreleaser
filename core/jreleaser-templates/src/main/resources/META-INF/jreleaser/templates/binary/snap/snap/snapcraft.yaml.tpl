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
    command: bin/{{distributionExecutable}}
    environment:
      JAVA_HOME: $SNAP/usr/lib/jvm/java

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
    source-checksum: sha256/{{distributionSha256}}
    stage-packages:
      - openjdk-{{javaVersion}}-jdk
    organize:
      usr/lib/jvm/java-{{javaVersion}}-openjdk*: usr/lib/jvm/java
    {{#snapHasLocalPlugs}}
    plugs:
      {{#snapLocalPlugs}}
      - {{.}}
      {{/snapLocalPlugs}}
    {{/snapHasLocalPlugs}}
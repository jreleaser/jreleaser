# {{jreleaserCreationStamp}}
name: {{snapPackageName}}
version: "{{projectVersion}}"
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
  {{distributionExecutableName}}:
    {{#distributionJavaMainModule}}
    command: ${JAVA_HOME}/bin/java -p $SNAP/{{distributionArtifactFile}} -m {{distributionJavaMainModule}}/{{distributionJavaMainClass}}
    {{/distributionJavaMainModule}}
    {{^distributionJavaMainModule}}
    command: ${JAVA_HOME}/bin/java -jar $SNAP/{{distributionArtifactFile}}
    {{/distributionJavaMainModule}}
    environment:
      JAVA_HOME: "$SNAP/usr/lib/jvm/java"
      PATH: "$PATH:$JAVA_HOME/bin"
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
  {{/snapSlots }}
{{/snapHasSlots}}
parts:
  {{distributionExecutableName}}:
    plugin: nil
    override-build: |
      wget -O $SNAPCRAFT_PART_INSTALL/{{distributionArtifactFile}} {{distributionUrl}}
      snapcraftctl set-version "{{projectVersion}}"
    build-packages:
      - wget
    stage-packages:
      - openjdk-{{distributionJavaVersion}}-jre
      - ca-certificates
      - ca-certificates-java
    organize:
      'usr/lib/jvm/java-*': 'usr/lib/jvm/java'
    prime:
      - -usr/lib/jvm/java/lib/security/cacerts

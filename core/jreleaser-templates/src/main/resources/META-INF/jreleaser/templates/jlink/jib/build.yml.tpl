# {{jreleaserCreationStamp}}
apiVersion: jib/v1alpha1
kind: BuildFile

from:
  image: {{jibBaseImage}}

{{#jibCreationTime}}
creationTime: "{{.}}"
{{/jibCreationTime}}

format: {{jibFormat}}

environment:
  "JAVA_HOME": "{{jibWorkingDirectory}}/{{distributionArtifactRootEntryName}}"
{{#jibEnvironment}}
  {{.}}
{{/jibEnvironment}}

labels:
{{#jibLabels}}
  {{.}}
{{/jibLabels}}

{{#hasJibVolumes}}
volumes:
{{#jibVolumes}}
  - "{{.}}"
{{/jibVolumes}}
{{/hasJibVolumes}}

{{#hasJibExposedPorts}}
exposedPorts:
{{#jibExposedPorts}}
  - "{{.}}"
{{/jibExposedPorts}}
{{/hasJibExposedPorts}}

{{#jibUser}}
user: "{{.}}"
{{/jibUser}}

workingDirectory: "{{jibWorkingDirectory}}"

entrypoint: ["{{jibWorkingDirectory}}/{{distributionArtifactRootEntryName}}/bin/{{distributionExecutableUnix}}"]

layers:
  entries:
    - name: {{distributionName}}
      files:
        - src: assembly/{{distributionArtifactRootEntryName}}/bin
          dest: {{jibWorkingDirectory}}/{{distributionArtifactRootEntryName}}/bin
          properties:
            filePermissions: 755
        - src: assembly/{{distributionArtifactRootEntryName}}
          dest: {{jibWorkingDirectory}}/{{distributionArtifactRootEntryName}}
          excludes:
            - "bin/*"
        - src: assembly
          dest: {{jibWorkingDirectory}}
          excludes:
            - "{{distributionArtifactRootEntryName}}"

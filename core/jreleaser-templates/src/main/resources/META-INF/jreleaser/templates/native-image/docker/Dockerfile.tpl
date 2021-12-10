# [JRELEASER_VERSION]
FROM {{dockerBaseImage}}

{{#dockerLabels}}
LABEL {{.}}
{{/dockerLabels}}

{{#dockerPreCommands}}
{{.}}
{{/dockerPreCommands}}

COPY assembly/ /
RUN chmod +x {{distributionArtifactName}}/bin/{{distributionExecutable}}

{{#dockerPostCommands}}
{{.}}
{{/dockerPostCommands}}

ENV PATH="${PATH}:/{{distributionArtifactName}}/bin"

ENTRYPOINT ["/{{distributionArtifactName}}/bin/{{distributionExecutable}}"]
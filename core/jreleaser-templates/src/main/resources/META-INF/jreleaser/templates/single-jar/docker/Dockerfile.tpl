# {{jreleaserCreationStamp}}
# Multi-stage build to avoid duplicate layers

FROM alpine:latest AS assembler

COPY assembly/ /assembly/

RUN mkdir -p /opt/{{distributionExecutableName}}/bin && \
    mkdir -p /opt/{{distributionExecutableName}}/lib && \
    mv /assembly/{{distributionExecutableUnix}} /opt/{{distributionExecutableName}}/bin && \
    chmod +x /opt/{{distributionExecutableName}}/bin/{{distributionExecutableUnix}} && \
    mv /assembly/{{distributionArtifactFile}} /opt/{{distributionExecutableName}}/lib

FROM {{dockerBaseImage}}

{{#dockerLabels}}
LABEL {{.}}
{{/dockerLabels}}

{{#dockerPreCommands}}
{{.}}
{{/dockerPreCommands}}

COPY --from=assembler /assembly /
COPY --from=assembler /opt/{{distributionExecutableName}} /{{distributionExecutableName}}

ENV PATH="${PATH}:/{{distributionExecutableName}}/bin"

{{#dockerPostCommands}}
{{.}}
{{/dockerPostCommands}}

ENTRYPOINT {{dockerEntrypoint}}
{{#dockerCmd}}
CMD {{dockerCmd}}
{{/dockerCmd}}

# {{jreleaserCreationStamp}}
# Multi-stage build to avoid duplicate layers

FROM alpine:latest AS extractor

RUN apk add --no-cache unzip

COPY assembly/ /tmp/

RUN unzip /tmp/{{distributionArtifactFile}} -d /opt && \
    chmod +x /opt/{{distributionArtifactRootEntryName}}/bin/{{distributionExecutableUnix}}

FROM {{dockerBaseImage}}

{{#dockerLabels}}
LABEL {{.}}
{{/dockerLabels}}

{{#dockerPreCommands}}
{{.}}
{{/dockerPreCommands}}

COPY --from=extractor /opt/{{distributionArtifactRootEntryName}} /{{distributionArtifactRootEntryName}}

{{#dockerPostCommands}}
{{.}}
{{/dockerPostCommands}}

ENV PATH="${PATH}:/{{distributionArtifactRootEntryName}}/bin"

ENTRYPOINT {{dockerEntrypoint}}
{{#dockerCmd}}
CMD {{dockerCmd}}
{{/dockerCmd}}

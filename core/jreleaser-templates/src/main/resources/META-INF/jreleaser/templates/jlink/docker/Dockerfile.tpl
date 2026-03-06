# {{jreleaserCreationStamp}}
# Multi-stage build to avoid duplicate layers

FROM alpine:latest AS extractor

RUN apk add --no-cache unzip

COPY assembly/ /tmp/

RUN unzip /tmp/{{distributionArtifactFile}} -d /opt && \
    chmod +x /opt/{{distributionArtifactRootEntryName}}/bin/{{distributionExecutableUnix}} && \
    chmod +x /opt/{{distributionArtifactRootEntryName}}/bin/java

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

ENV JAVA_HOME="/{{distributionArtifactRootEntryName}}"
ENV PATH="${PATH}:${JAVA_HOME}/bin"

ENTRYPOINT {{dockerEntrypoint}}
{{#dockerCmd}}
CMD {{dockerCmd}}
{{/dockerCmd}}

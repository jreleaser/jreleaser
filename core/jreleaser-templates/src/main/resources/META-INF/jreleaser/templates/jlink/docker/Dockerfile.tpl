# {{jreleaserCreationStamp}}
# Multi-stage build to avoid duplicate layers

FROM alpine:latest AS extractor

RUN apk add --no-cache unzip

COPY assembly/ /assembly/

RUN unzip /assembly/{{distributionArtifactFile}} -d /opt && \
    rm /assembly/{{distributionArtifactFile}} && \
    chmod +x /opt/{{distributionArtifactRootEntryName}}/bin/{{distributionExecutableUnix}} && \
    chmod +x /opt/{{distributionArtifactRootEntryName}}/bin/java

FROM {{dockerBaseImage}}

{{#dockerLabels}}
LABEL {{.}}
{{/dockerLabels}}

{{#dockerPreCommands}}
{{.}}
{{/dockerPreCommands}}

COPY --from=extractor /assembly /
COPY --from=extractor /opt/{{distributionArtifactRootEntryName}} /{{distributionExecutableName}}

{{#dockerPostCommands}}
{{.}}
{{/dockerPostCommands}}

ENV JAVA_HOME="/{{distributionExecutableName}}"
ENV PATH="${PATH}:${JAVA_HOME}/bin"

ENTRYPOINT {{dockerEntrypoint}}
{{#dockerCmd}}
CMD {{dockerCmd}}
{{/dockerCmd}}

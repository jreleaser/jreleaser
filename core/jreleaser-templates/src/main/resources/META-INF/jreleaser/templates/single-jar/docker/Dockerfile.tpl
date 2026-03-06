# {{jreleaserCreationStamp}}
# Multi-stage build to avoid duplicate layers

FROM alpine:3.21 AS extractor

COPY assembly/ /tmp/

RUN mkdir -p /opt/{{distributionName}}-{{projectVersion}}/bin && \
    mkdir -p /opt/{{distributionName}}-{{projectVersion}}/lib && \
    mv /tmp/{{distributionExecutableUnix}} /opt/{{distributionName}}-{{projectVersion}}/bin && \
    chmod +x /opt/{{distributionName}}-{{projectVersion}}/bin/{{distributionExecutableUnix}} && \
    mv /tmp/{{distributionArtifactFile}} /opt/{{distributionName}}-{{projectVersion}}/lib

FROM {{dockerBaseImage}}

{{#dockerLabels}}
LABEL {{.}}
{{/dockerLabels}}

{{#dockerPreCommands}}
{{.}}
{{/dockerPreCommands}}

COPY --from=extractor /opt/{{distributionName}}-{{projectVersion}} /{{distributionName}}-{{projectVersion}}

ENV PATH="${PATH}:/{{distributionName}}-{{projectVersion}}/bin"

{{#dockerPostCommands}}
{{.}}
{{/dockerPostCommands}}

ENTRYPOINT ["/{{distributionName}}-{{projectVersion}}/bin/{{distributionExecutableUnix}}"]
{{#dockerCmd}}
CMD {{dockerCmd}}
{{/dockerCmd}}

# {{jreleaserCreationStamp}}
# Multi-stage build to avoid duplicate layers

FROM alpine:latest AS assembler

COPY assembly/ /assembly/

RUN mkdir -p /opt/{{distributionName}}-{{projectVersion}}/bin && \
    mkdir -p /opt/{{distributionName}}-{{projectVersion}}/lib && \
    mv /assembly/{{distributionExecutableUnix}} /opt/{{distributionName}}-{{projectVersion}}/bin && \
    chmod +x /opt/{{distributionName}}-{{projectVersion}}/bin/{{distributionExecutableUnix}} && \
    mv /assembly/{{distributionArtifactFile}} /opt/{{distributionName}}-{{projectVersion}}/lib

FROM {{dockerBaseImage}}

{{#dockerLabels}}
LABEL {{.}}
{{/dockerLabels}}

{{#dockerPreCommands}}
{{.}}
{{/dockerPreCommands}}

COPY --from=assembler /assembly /
COPY --from=assembler /opt/{{distributionName}}-{{projectVersion}} /{{distributionName}}-{{projectVersion}}

ENV PATH="${PATH}:/{{distributionName}}-{{projectVersion}}/bin"

{{#dockerPostCommands}}
{{.}}
{{/dockerPostCommands}}

ENTRYPOINT ["/{{distributionName}}-{{projectVersion}}/bin/{{distributionExecutableUnix}}"]
{{#dockerCmd}}
CMD {{dockerCmd}}
{{/dockerCmd}}

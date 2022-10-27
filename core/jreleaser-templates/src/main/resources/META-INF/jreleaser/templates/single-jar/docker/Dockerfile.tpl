# {{jreleaserCreationStamp}}
FROM {{dockerBaseImage}}

{{#dockerLabels}}
LABEL {{.}}
{{/dockerLabels}}

{{#dockerPreCommands}}
{{.}}
{{/dockerPreCommands}}

COPY assembly/ /

RUN mkdir -p /{{distributionName}}-{{projectVersion}}/bin && \
    mkdir -p /{{distributionName}}-{{projectVersion}}/lib && \
    mv /{{distributionExecutableUnix}} /{{distributionName}}-{{projectVersion}}/bin && \
    chmod +x /{{distributionName}}-{{projectVersion}}/bin/{{distributionExecutableUnix}} && \
    mv /{{distributionArtifactFile}} /{{distributionName}}-{{projectVersion}}/lib

ENV PATH="${PATH}:/{{distributionName}}-{{projectVersion}}/bin"

{{#dockerPostCommands}}
{{.}}
{{/dockerPostCommands}}

ENTRYPOINT ["/{{distributionName}}-{{projectVersion}}/bin/{{distributionExecutableUnix}}"]
CMD ["{{distributionExecutableUnix}}"]
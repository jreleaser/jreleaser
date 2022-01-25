# {{jreleaserCreationStamp}}
FROM {{dockerBaseImage}}

{{#dockerLabels}}
LABEL {{.}}
{{/dockerLabels}}

{{#dockerPreCommands}}
{{.}}
{{/dockerPreCommands}}

COPY assembly/ /

RUN mkdir -p /{{distributionName}}/bin && \
    mkdir -p /{{distributionName}}/lib && \
    mv /{{distributionExecutableUnix}} /{{distributionName}}/bin && \
    chmod +x /{{distributionName}}/bin/{{distributionExecutableUnix}} && \
    mv /{{distributionArtifactFile}} /{{distributionName}}/lib

ENV PATH="${PATH}:/{{distributionName}}/bin"

{{#dockerPostCommands}}
{{.}}
{{/dockerPostCommands}}

ENTRYPOINT ["/{{distributionName}}/bin/{{distributionExecutableUnix}}"]
CMD ["{{distributionExecutableUnix}}"]
# {{jreleaserCreationStamp}}
FROM {{dockerBaseImage}}

{{#dockerLabels}}
LABEL {{.}}
{{/dockerLabels}}

{{#dockerPreCommands}}
{{.}}
{{/dockerPreCommands}}

RUN curl -Ls "{{distributionUrl}}" --output {{distributionArtifactFile}} && \
    unzip {{distributionArtifactFile}} && \
    rm {{distributionArtifactFile}} && \
    chmod +x {{distributionArtifactFileName}}/bin/{{distributionExecutableUnix}}

{{#dockerPostCommands}}
{{.}}
{{/dockerPostCommands}}

ENV PATH="${PATH}:/{{distributionArtifactFileName}}/bin"

ENTRYPOINT ["/{{distributionArtifactFileName}}/bin/{{distributionExecutableUnix}}"]
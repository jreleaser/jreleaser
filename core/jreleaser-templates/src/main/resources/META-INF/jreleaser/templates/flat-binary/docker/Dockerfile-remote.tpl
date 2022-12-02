# {{jreleaserCreationStamp}}
FROM {{dockerBaseImage}}

{{#dockerLabels}}
LABEL {{.}}
{{/dockerLabels}}

{{#dockerPreCommands}}
{{.}}
{{/dockerPreCommands}}

RUN curl -Ls "{{distributionUrl}}" --output {{distributionArtifactFile}} && \
    mv {{distributionArtifactFile}} {{distributionExecutableName}} &&
    chmod +x {{distributionExecutableName}}

{{#dockerPostCommands}}
{{.}}
{{/dockerPostCommands}}

ENV PATH="${PATH}:/"

ENTRYPOINT ["/{{distributionExecutableName}}"]
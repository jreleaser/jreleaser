FROM {{dockerBaseImage}}

{{#dockerLabels}}
LABEL {{.}}
{{/dockerLabels}}

{{#dockerPreCommands}}
{{.}}
{{/dockerPreCommands}}

RUN curl -Ls "{{distributionUrl}}" --output {{distributionArtifactFileName}} && \
    unzip {{distributionArtifactFileName}} && \
    rm {{distributionArtifactFileName}} && \
    chmod +x {{distributionArtifactName}}/bin/{{distributionExecutable}}

{{#dockerPostCommands}}
{{.}}
{{/dockerPostCommands}}

ENV PATH="${PATH}:/{{distributionArtifactName}}/bin"

ENTRYPOINT ["/{{distributionArtifactName}}/bin/{{distributionExecutable}}"]

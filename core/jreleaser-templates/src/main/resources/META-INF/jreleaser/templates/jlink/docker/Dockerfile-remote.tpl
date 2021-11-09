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
    chmod +x {{distributionArtifactName}}/bin/{{distributionExecutable}} && \
    chmod +x {{distributionArtifactName}}/bin/java

{{#dockerPostCommands}}
{{.}}
{{/dockerPostCommands}}

ENV JAVA_HOME="/{{distributionArtifactName}}"
ENV PATH="${PATH}:${JAVA_HOME}/bin"

ENTRYPOINT ["/{{distributionArtifactName}}/bin/{{distributionExecutable}}"]

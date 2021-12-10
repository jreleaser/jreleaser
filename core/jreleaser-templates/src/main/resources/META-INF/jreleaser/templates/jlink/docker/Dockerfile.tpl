# [JRELEASER_VERSION]
FROM {{dockerBaseImage}}

{{#dockerLabels}}
LABEL {{.}}
{{/dockerLabels}}

{{#dockerPreCommands}}
{{.}}
{{/dockerPreCommands}}

COPY assembly/ /

RUN unzip {{distributionArtifactFileName}} && \
    rm {{distributionArtifactFileName}} && \
    chmod +x {{distributionArtifactName}}/bin/{{distributionExecutable}} && \
    chmod +x {{distributionArtifactName}}/bin/java

{{#dockerPostCommands}}
{{.}}
{{/dockerPostCommands}}

ENV JAVA_HOME="/{{distributionArtifactName}}"
ENV PATH="${PATH}:${JAVA_HOME}/bin"

ENTRYPOINT ["/{{distributionArtifactName}}/bin/{{distributionExecutable}}"]

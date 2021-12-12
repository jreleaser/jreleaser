# [JRELEASER_VERSION]
FROM {{dockerBaseImage}}

{{#dockerLabels}}
LABEL {{.}}
{{/dockerLabels}}

{{#dockerPreCommands}}
{{.}}
{{/dockerPreCommands}}

COPY assembly/ /

RUN unzip {{distributionArtifactFile}} && \
    rm {{distributionArtifactFile}} && \
    chmod +x {{distributionArtifactFileName}}/bin/{{distributionExecutable}} && \
    chmod +x {{distributionArtifactFileName}}/bin/java

{{#dockerPostCommands}}
{{.}}
{{/dockerPostCommands}}

ENV JAVA_HOME="/{{distributionArtifactFileName}}"
ENV PATH="${PATH}:${JAVA_HOME}/bin"

ENTRYPOINT ["/{{distributionArtifactFileName}}/bin/{{distributionExecutable}}"]

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
    chmod +x {{distributionArtifactFileName}}/bin/{{distributionExecutableUnix}} && \
    chmod +x {{distributionArtifactFileName}}/bin/java

{{#dockerPostCommands}}
{{.}}
{{/dockerPostCommands}}

ENV JAVA_HOME="/{{distributionArtifactFileName}}"
ENV PATH="${PATH}:${JAVA_HOME}/bin"

ENTRYPOINT ["/{{distributionArtifactFileName}}/bin/{{distributionExecutableUnix}}"]
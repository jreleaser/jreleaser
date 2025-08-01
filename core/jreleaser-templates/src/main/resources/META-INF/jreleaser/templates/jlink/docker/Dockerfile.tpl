# {{jreleaserCreationStamp}}
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
    chmod +x {{distributionArtifactRootEntryName}}/bin/{{distributionExecutableUnix}} && \
    chmod +x {{distributionArtifactRootEntryName}}/bin/java

{{#dockerPostCommands}}
{{.}}
{{/dockerPostCommands}}

ENV JAVA_HOME="/{{distributionArtifactRootEntryName}}"
ENV PATH="${PATH}:${JAVA_HOME}/bin"

ENTRYPOINT {{dockerEntrypoint}}
{{#dockerCmd}}
CMD {{dockerCmd}}
{{/dockerCmd}}

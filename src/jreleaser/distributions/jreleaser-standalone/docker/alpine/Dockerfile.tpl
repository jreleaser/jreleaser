# {{jreleaserCreationStamp}}
FROM {{dockerBaseImage}}

{{#dockerLabels}}
LABEL {{.}}
{{/dockerLabels}}

{{#dockerPreCommands}}
{{.}}
{{/dockerPreCommands}}

COPY assembly/* /

RUN unzip {{distributionArtifactFile}} && \
    rm {{distributionArtifactFile}} && \
    chmod +x {{distributionArtifactRootEntryName}}/bin/{{distributionExecutableUnix}} && \
    chmod +x {{distributionArtifactRootEntryName}}/bin/java && \
    mv /{{distributionExecutableName}}-entrypoint.sh /{{distributionArtifactRootEntryName}}/bin && \
    chmod +x /{{distributionArtifactRootEntryName}}/bin/{{distributionExecutableName}}-entrypoint.sh && \
    mv /{{distributionArtifactRootEntryName}} /{{distributionExecutableName}}

ENV PATH="${PATH}:/{{distributionExecutableName}}/bin"

{{#dockerPostCommands}}
{{.}}
{{/dockerPostCommands}}

ENTRYPOINT ["/{{distributionExecutableName}}/bin/{{distributionExecutableName}}-entrypoint.sh"]
CMD ["/{{distributionExecutableName}}/bin/{{distributionExecutableUnix}}"]

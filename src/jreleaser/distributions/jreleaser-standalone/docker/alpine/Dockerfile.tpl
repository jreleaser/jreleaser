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
    chmod +x {{distributionArtifactFileName}}/bin/{{distributionExecutableUnix}} && \
    chmod +x {{distributionArtifactFileName}}/bin/java && \
    mv /{{distributionExecutableName}}-entrypoint.sh /{{distributionArtifactFileName}}/bin && \
    chmod +x /{{distributionArtifactFileName}}/bin/{{distributionExecutableName}}-entrypoint.sh && \
    mv /{{distributionArtifactFileName}} /{{distributionExecutableName}}

ENV PATH="${PATH}:/{{distributionExecutableName}}/bin"

{{#dockerPostCommands}}
{{.}}
{{/dockerPostCommands}}

ENTRYPOINT ["/{{distributionExecutableName}}/bin/{{distributionExecutableName}}-entrypoint.sh"]
CMD ["/{{distributionExecutableName}}/bin/{{distributionExecutableUnix}}"]

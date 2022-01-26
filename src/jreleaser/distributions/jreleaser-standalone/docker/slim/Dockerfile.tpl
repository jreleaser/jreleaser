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
    chmod +x /{{distributionArtifactFileName}}/bin/{{distributionExecutableName}}-entrypoint.sh

ENV PATH="${PATH}:/{{distributionArtifactFileName}}/bin"

{{#dockerPostCommands}}
{{.}}
{{/dockerPostCommands}}

ENTRYPOINT ["/{{distributionArtifactFileName}}/bin/{{distributionExecutableName}}-entrypoint.sh"]
CMD ["/{{distributionArtifactFileName}}/bin/{{distributionExecutableUnix}}"]

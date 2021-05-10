FROM {{dockerBaseImage}}

{{#dockerLabels}}
LABEL {{.}}
{{/dockerLabels}}

{{#dockerPreCommands}}
{{.}}
{{/dockerPreCommands}}

COPY assembly/* /

RUN unzip {{distributionArtifactFileName}} && \
    rm {{distributionArtifactFileName}} && \
    chmod +x {{distributionArtifactName}}/bin/{{distributionExecutable}} && \
    chmod +x {{distributionArtifactName}}/bin/java && \
    mv /{{distributionExecutable}}-entrypoint.sh /{{distributionArtifactName}}/bin && \
    chmod +x /{{distributionArtifactName}}/bin/{{distributionExecutable}}-entrypoint.sh

ENV PATH="${PATH}:/{{distributionArtifactName}}/bin"

{{#dockerPostCommands}}
{{.}}
{{/dockerPostCommands}}

ENTRYPOINT ["/{{distributionArtifactName}}/bin/{{distributionExecutable}}-entrypoint.sh"]
CMD ["/{{distributionArtifactName}}/bin/{{distributionExecutable}}"]

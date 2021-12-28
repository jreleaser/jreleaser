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
    chmod +x {{distributionArtifactFileName}}/bin/{{distributionExecutable}} && \
    chmod +x {{distributionArtifactFileName}}/bin/java && \
    mv /{{distributionExecutable}}-entrypoint.sh /{{distributionArtifactFileName}}/bin && \
    chmod +x /{{distributionArtifactFileName}}/bin/{{distributionExecutable}}-entrypoint.sh

ENV PATH="${PATH}:/{{distributionArtifactFileName}}/bin"

{{#dockerPostCommands}}
{{.}}
{{/dockerPostCommands}}

ENTRYPOINT ["/{{distributionArtifactFileName}}/bin/{{distributionExecutable}}-entrypoint.sh"]
CMD ["/{{distributionArtifactFileName}}/bin/{{distributionExecutable}}"]

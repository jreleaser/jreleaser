FROM {{dockerBaseImage}}

{{#dockerLabels}}
LABEL {{.}}
{{/dockerLabels}}

{{#dockerPreCommands}}
{{.}}
{{/dockerPreCommands}}

COPY assembly/* /

RUN mkdir -p /{{distributionName}}/bin && \
    mkdir -p /{{distributionName}}/lib && \
    mv /{{distributionExecutable}} /{{distributionName}}/bin && \
    chmod +x /{{distributionName}}/bin/{{distributionExecutable}} && \
    mv /{{distributionExecutable}}-entrypoint.sh /{{distributionName}}/bin && \
    chmod +x /{{distributionName}}/bin/{{distributionExecutable}}-entrypoint.sh && \
    mv /{{artifactFileName}} /{{distributionName}}/lib

ENV PATH="${PATH}:/{{distributionName}}/bin"

{{#dockerPostCommands}}
{{.}}
{{/dockerPostCommands}}

ENTRYPOINT ["/{{distributionName}}/bin/{{distributionExecutable}}-entrypoint.sh"]
CMD ["/{{distributionName}}/bin/{{distributionExecutable}}"]

FROM {{dockerBaseImage}}

{{#dockerLabels}}
LABEL {{.}}
{{/dockerLabels}}

COPY assembly/{{artifactFileName}} /{{artifactFileName}}

RUN unzip {{artifactFileName}} && \
    rm {{artifactFileName}} && \
    mv {{distributionName}}-* {{distributionName}} && \
    chmod +x {{distributionName}}/bin/{{distributionExecutable}}

ENV PATH="${PATH}:/{{distributionName}}/bin"

ENTRYPOINT ["/{{distributionName}}/bin/{{distributionExecutable}}"]

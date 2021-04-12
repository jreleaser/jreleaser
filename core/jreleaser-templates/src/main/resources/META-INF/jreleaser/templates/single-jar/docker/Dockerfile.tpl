FROM {{dockerBaseImage}}

{{#dockerLabels}}
LABEL {{.}}
{{/dockerLabels}}

COPY assembly/* /

RUN chmod +x {{distributionExecutable}}

ENTRYPOINT ["/{{distributionExecutable}}"]
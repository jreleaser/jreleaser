# {{jreleaserCreationStamp}}
# Multi-stage build to avoid duplicate layers

FROM alpine:latest AS assembler

COPY assembly/ /assembly/

RUN chmod +x /assembly/{{distributionExecutableName}}

FROM {{dockerBaseImage}}

{{#dockerLabels}}
LABEL {{.}}
{{/dockerLabels}}

{{#dockerPreCommands}}
{{.}}
{{/dockerPreCommands}}

COPY --from=assembler /assembly/ /

{{#dockerPostCommands}}
{{.}}
{{/dockerPostCommands}}

ENV PATH="${PATH}:/"

ENTRYPOINT {{dockerEntrypoint}}
{{#dockerCmd}}
CMD {{dockerCmd}}
{{/dockerCmd}}

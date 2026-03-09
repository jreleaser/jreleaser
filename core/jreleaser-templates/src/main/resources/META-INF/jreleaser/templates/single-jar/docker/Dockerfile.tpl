# {{jreleaserCreationStamp}}
FROM {{dockerBaseImage}}

{{#dockerLabels}}
LABEL {{.}}
{{/dockerLabels}}

{{#dockerPreCommands}}
{{.}}
{{/dockerPreCommands}}

COPY assembly/ /

{{#dockerPostCommands}}
{{.}}
{{/dockerPostCommands}}

ENTRYPOINT {{dockerEntrypoint}}
{{#dockerCmd}}
CMD {{dockerCmd}}
{{/dockerCmd}}

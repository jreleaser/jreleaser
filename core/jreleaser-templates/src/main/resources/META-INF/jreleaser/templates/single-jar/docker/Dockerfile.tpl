# {{jreleaserCreationStamp}}
# For Docker, we copy the JAR directly without the bin/lib directory structure.
# The launcher script is useful for bare-metal deployments but unnecessary in containers
# where the ENTRYPOINT defines how to run the application.
FROM {{dockerBaseImage}}

{{#dockerLabels}}
LABEL {{.}}
{{/dockerLabels}}

{{#dockerPreCommands}}
{{.}}
{{/dockerPreCommands}}

COPY assembly/{{distributionArtifactFile}} /app/{{distributionArtifactFile}}

{{#dockerPostCommands}}
{{.}}
{{/dockerPostCommands}}

ENTRYPOINT {{dockerEntrypoint}}
{{#dockerCmd}}
CMD {{dockerCmd}}
{{/dockerCmd}}

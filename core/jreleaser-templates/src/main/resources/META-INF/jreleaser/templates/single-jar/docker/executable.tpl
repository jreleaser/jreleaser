#!/bin/sh
# {{jreleaserCreationStamp}}

$JAVA_HOME/bin/java $JAVA_OPTS -jar /{{distributionName}}-{{projectVersion}}/lib/{{distributionArtifactFile}} "$@"
#!/bin/sh
# [JRELEASER_VERSION]

$JAVA_HOME/bin/java $JAVA_OPTS -jar /{{distributionName}}/lib/{{distributionArtifactFile}} "$@"
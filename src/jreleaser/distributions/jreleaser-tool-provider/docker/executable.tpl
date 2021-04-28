#!/bin/sh

# if not running inside GitLab CI then set the default basedir
if [[ -z "${GITLAB_CI}" ]]; then
    ARGS="--basedir /workspace"
else
    ARGS=""
fi

$JAVA_HOME/bin/java $JAVA_OPTS -jar /{{distributionName}}/lib/{{artifactFileName}} "$@" $ARGS

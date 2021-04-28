#!/bin/sh

# if not running inside GitLab CI then specify the `jreleaser` command as default
if [[ -z "${GITLAB_CI}" ]]; then
    ARGS="jreleaser"
else
    ARGS=""
fi

exec $ARGS "$@"
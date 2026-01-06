#!/bin/bash

# if not running inside GitLab CI then specify the `jreleaser` command as default
if [[ -z "${GITLAB_CI}" ]]; then
    ARGS="jreleaser"
    WORKING_DIR="/workspace"
else
    ARGS=""
    WORKING_DIR="/"
fi

cd $WORKING_DIR
exec $ARGS "$@"
#!/usr/bin/env sh
# {{jreleaserCreationStamp}}

##############################################################################
##
##  {{distributionExecutable}} start up script for UN*X
##
##############################################################################

die () {
    echo
    echo "$*"
    echo
    exit 1
} >&2

# Determine the Java command to use to start the JVM.
if [ -n "$JAVA_HOME" ] ; then
    if [ -x "$JAVA_HOME/jre/sh/java" ] ; then
        # IBM's JDK on AIX uses strange locations for the executables
        JAVACMD=$JAVA_HOME/jre/sh/java
    else
        JAVACMD=$JAVA_HOME/bin/java
    fi
    if [ ! -x "$JAVACMD" ] ; then
        die "ERROR: JAVA_HOME is set to an invalid directory: $JAVA_HOME

Please set the JAVA_HOME variable in your environment to match the
location of your Java installation."
    fi
else
    JAVACMD=java
    which java >/dev/null 2>&1 || die "ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH.

Please set the JAVA_HOME variable in your environment to match the
location of your Java installation."
fi

# Collect all arguments for the java command;
#   * $JAVA_OPTS can contain fragments of
#     shell script including quotes and variable substitutions, so put them in
#     double quotes to make sure that they get re-expanded; and
#   * put everything else in single quotes, so that it's not re-expanded.

{{#distributionJavaMainModule}}
set -- \
        -p "{{distributionArtifactFile}}" \
        -m {{distributionJavaMainModule}}/{{distributionJavaMainClass}} \
        "$@"
{{/distributionJavaMainModule}}
{{^distributionJavaMainModule}}
set -- \
        -jar {{distributionArtifactFile}} \
        "$@"
{{/distributionJavaMainModule}}

eval "set -- $(
        printf '%s\n' "$JAVA_OPTS" |
        xargs -n1 |
        sed ' s~[^-[:alnum:]+,./:=@_]~\\&~g; ' |
        tr '\n' ' '
    )" '"$@"'

exec "$JAVACMD" "$@"

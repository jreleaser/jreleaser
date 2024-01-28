#!/usr/bin/env sh
# {{jreleaserCreationStamp}}

##############################################################################
##
##  {{distributionExecutable}} start up script for UN*X
##
##############################################################################

# Attempt to set APP_HOME

# Resolve links: $0 may be a link
app_path=$0

# Need this for daisy-chained symlinks.
while
    APP_HOME=${app_path%"${app_path##*/}"}  # leaves a trailing /; empty if no leading path
    [ -h "$app_path" ]
do
    ls=$( ls -ld "$app_path" )
    link=${ls#*' -> '}
    case $link in
      /*)   app_path=$link ;;
      *)    app_path=$APP_HOME$link ;;
    esac
done

APP_HOME=$( cd "${APP_HOME:-./}.." && pwd -P ) || exit

APP_NAME="{{distributionExecutable}}"

# Use the maximum available, or set MAX_FD != -1 to use that value.
MAX_FD=maximum

warn () {
    echo "$*"
} >&2

die () {
    echo
    echo "$*"
    echo
    exit 1
} >&2

# OS specific support (must be 'true' or 'false').
cygwin=false
msys=false
darwin=false
nonstop=false
case "$( uname )" in
  CYGWIN* )         cygwin=true  ;;
  Darwin* )         darwin=true  ;;
  MSYS* | MINGW* )  msys=true    ;;
  NONSTOP* )        nonstop=true ;;
esac

JAVA_HOME=$APP_HOME
JAVACMD="$JAVA_HOME/bin/java"
JARSDIR="$APP_HOME/jars"
{{#distributionJavaMainModule}}
CLASSPATH="$JARSDIR"
{{/distributionJavaMainModule}}
{{^distributionJavaMainModule}}
CLASSPATH="$JARSDIR/*"
{{/distributionJavaMainModule}}
JAVA_OPTS="$JAVA_OPTS {{distributionJavaOptions}}"

# Increase the maximum file descriptors if we can.
if ! "$cygwin" && ! "$darwin" && ! "$nonstop" ; then
    case $MAX_FD in #(
      max*)
        MAX_FD=$( ulimit -H -n ) ||
            warn "Could not query maximum file descriptor limit"
    esac
    case $MAX_FD in  #(
      '' | soft) :;; #(
      *)
        ulimit -n "$MAX_FD" ||
            warn "Could not set maximum file descriptor limit to $MAX_FD"
    esac
fi

# For Darwin, add options to specify how the application appears in the dock
if $darwin; then
    JAVA_OPTS="$JAVA_OPTS -Xdock:name=$APP_NAME"
fi

# Collect all arguments for the java command, stacking in reverse order:
#   * args from the command line
#   * the main class name
#   * -classpath
#   * -Dkey settings
#   * DEFAULT_JAVA_OPTS and $JAVA_OPTS environment variables.

# For Cygwin or MSYS, switch paths to Windows format before running java
if "$cygwin" || "$msys" ; then
    APP_HOME=$( cygpath --path --mixed "$APP_HOME" )
    CLASSPATH=$( cygpath --path --mixed "$CLASSPATH" )

    JAVACMD=$( cygpath --unix "$JAVACMD" )

    # Now convert the arguments - kludge to limit ourselves to /bin/sh
    for arg do
        if
            case $arg in
              -*)   false ;;
              /?*)  t=${arg#/} t=/${t%%/*}
                    [ -e "$t" ] ;;
              *)    false ;;
            esac
        then
            arg=$( cygpath --path --ignore --mixed "$arg" )
        fi

        shift                   # remove old arg
        set -- "$@" "$arg"      # push replacement arg
    done
fi

# Collect all arguments for the java command;
#   * $DEFAULT_JAVA_OPTS and $JAVA_OPTS can contain fragments of
#     shell script including quotes and variable substitutions, so put them in
#     double quotes to make sure that they get re-expanded; and
#   * put everything else in single quotes, so that it's not re-expanded.

{{#distributionJavaMainModule}}
set -- \
        -p "$CLASSPATH" \
        -m {{distributionJavaMainModule}}/{{distributionJavaMainClass}} \
        "$@"
{{/distributionJavaMainModule}}
{{^distributionJavaMainModule}}
{{#distributionJavaMainClass}}
set -- \
        -classpath "$CLASSPATH" \
        {{distributionJavaMainClass}} \
        "$@"
{{/distributionJavaMainClass}}
{{^distributionJavaMainClass}}
set -- \
        -classpath "$CLASSPATH" \
        -jar "${JARSDIR}/{{distributionJavaMainJar}}" \
        "$@"
{{/distributionJavaMainClass}}
{{/distributionJavaMainModule}}

if ! command -v xargs >/dev/null 2>&1
then
    die "xargs is not available"
fi

eval "set -- $(
        printf '%s\n' "$DEFAULT_JAVA_OPTS $JAVA_OPTS" |
        xargs -n1 |
        sed ' s~[^-[:alnum:]+,./:=@_]~\\&~g; ' |
        tr '\n' ' '
    )" '"$@"'

exec "$JAVACMD" "$@"
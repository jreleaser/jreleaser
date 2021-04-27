#!/usr/bin/env sh

# resolve links - $0 may be a softlink
PRG="$0"
PRGDIR=`dirname "$PRG"`

BASEDIR=`cd "$PRGDIR/.." >/dev/null; pwd`

JAVA_HOME=$BASEDIR

JAVACMD="$JAVA_HOME/bin/java"

JARSDIR="$BASEDIR/jars"

CLASSPATH="$JARSDIR/*"

exec "$JAVACMD" $JAVA_OPTS -cp "$CLASSPATH" {{distributionJavaMainClass}} "$@"
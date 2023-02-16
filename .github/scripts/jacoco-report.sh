#!/bin/bash

set -e

JACOCO_VERSION="0.8.8"
ASM_VERSION="9.2"
ARGS4J_VERSION="2.0.28"

echo "⬇️  Downloading JaCoCo CLI"
mkdir -p libs
curl -s https://repo1.maven.org/maven2/org/jacoco/org.jacoco.cli/${JACOCO_VERSION}/org.jacoco.cli-${JACOCO_VERSION}.jar --output libs/org.jacoco.cli-${JACOCO_VERSION}.jar
curl -s https://repo1.maven.org/maven2/org/jacoco/org.jacoco.report/${JACOCO_VERSION}/org.jacoco.report-${JACOCO_VERSION}.jar --output libs/org.jacoco.report-${JACOCO_VERSION}.jar
curl -s https://repo1.maven.org/maven2/org/jacoco/org.jacoco.core/${JACOCO_VERSION}/org.jacoco.core-${JACOCO_VERSION}.jar --output libs/org.jacoco.core-${JACOCO_VERSION}.jar
curl -s https://repo1.maven.org/maven2/org/ow2/asm/asm-tree/${ASM_VERSION}/asm-tree-${ASM_VERSION}.jar --output libs/asm-tree-${ASM_VERSION}.jar
curl -s https://repo1.maven.org/maven2/org/ow2/asm/asm-commons/${ASM_VERSION}/asm-commons-${ASM_VERSION}.jar --output libs/asm-commons-${ASM_VERSION}.jar
curl -s https://repo1.maven.org/maven2/org/ow2/asm/asm-analysis/${ASM_VERSION}/asm-analysis-${ASM_VERSION}.jar --output libs/asm-analysis-${ASM_VERSION}.jar
curl -s https://repo1.maven.org/maven2/org/ow2/asm/asm/${ASM_VERSION}/asm-${ASM_VERSION}.jar --output libs/asm-${ASM_VERSION}.jar
curl -s https://repo1.maven.org/maven2/args4j/args4j/${ARGS4J_VERSION}/args4j-${ARGS4J_VERSION}.jar --output libs/args4j-${ARGS4J_VERSION}.jar

CLASSPATH="libs/org.jacoco.cli-${JACOCO_VERSION}.jar:libs/org.jacoco.report-${JACOCO_VERSION}.jar:libs/org.jacoco.core-${JACOCO_VERSION}.jar:libs/asm-tree-${ASM_VERSION}.jar:libs/asm-commons-${ASM_VERSION}.jar:libs/asm-analysis-${ASM_VERSION}.jar:libs/asm-${ASM_VERSION}.jar:libs/args4j-${ARGS4J_VERSION}.jar"

PWD=$(pwd)
CLASSES=""
SOURCES=""

echo "🔍 Collecting sources & classes"
for group in api core plugins sdks
do
    for project in $(ls $group)
    do
      echo " - $group/$project"
      sourcesDir="${PWD}/${group}/${project}/src/main/groovy"
      if [ ! -d "$sourcesDir" ];
      then
        sourcesDir="${PWD}/${group}/${project}/src/main/java"
      fi

      classesDir="${PWD}/${group}/${project}/build/classes/groovy/main"
      if [ ! -d "$classesDir" ];
      then
        classesDir="${PWD}/${group}/${project}/build/classes/java/main"
      fi

      if [ -z "$CLASSES" ];
      then
        CLASSES="--classfiles ${classesDir}"
      else
        CLASSES="${CLASSES} --classfiles ${classesDir}"
      fi

      if [ -z "$SOURCES" ];
      then
        SOURCES="--sourcefiles ${sourcesDir}"
      else
        SOURCES="${SOURCES} --sourcefiles ${sourcesDir}"
      fi
    done
done

mkdir -p build/reports/jacoco/aggregate

echo "🚀 Launching JaCoCo"
java -cp ${CLASSPATH} org.jacoco.cli.internal.Main report jacoco-merged.exec --xml build/reports/jacoco/aggregate/jacocoTestReport.xml ${CLASSES} ${SOURCES}

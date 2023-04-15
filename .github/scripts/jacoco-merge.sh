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

echo "🚀 Launching JaCoCo"
java -cp ${CLASSPATH} org.jacoco.cli.internal.Main merge jacoco/*.exec --destfile jacoco-merged.exec

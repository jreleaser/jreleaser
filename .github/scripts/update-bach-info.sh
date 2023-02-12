#!/bin/sh

# env vars:
# VERSION: x.y.z or early-access
# TAG: vx.y.z or early-access
# GH_BOT_EMAIL

set -e

EFFECTIVE_VERSION=$VERSION
REPOSITORY_OWNER="jreleaser"
REPOSITORY_NAME="jreleaser"
TOOL_NAME="jreleaser"
TOOL_DESC="JReleaser"
TOOL_FILENAME="jreleaser-tool-provider"

if [ "${TAG}" = "early-access" ]; then
  EFFECTIVE_VERSION=$TAG
fi

JAR_FILENAME="${TOOL_FILENAME}-${EFFECTIVE_VERSION}.jar"
JAR_URL="https://github.com/${REPOSITORY_OWNER}/${REPOSITORY_NAME}/releases/download/${TAG}/${JAR_FILENAME}.jar"
DOC_URL="https://github.com/${REPOSITORY_OWNER}/${REPOSITORY_NAME}/raw/${TAG}/README.adoc"

mkdir download
curl -sL "${JAR_URL}" --output "download/${JAR_FILENAME}"
curl -sL "${DOC_URL}" --output "download/README.adoc"
JAR_SIZE=$(ls -l "download/${JAR_FILENAME}" | awk '{print $5}')
JAR_CSUM=$(shasum -a 256 "download/${JAR_FILENAME}" | awk '{print $1}')
DOC_SIZE=$(ls -l "download/README.adoc" | awk '{print $5}')

TARGET_FILE=".bach/external-tools/${TOOL_NAME}@${TAG}.tool-directory.properties"
echo "@description ${TOOL_DESC} ${TAG} (${VERSION})" > $TARGET_FILE
echo " " >> $TARGET_FILE
echo "${JAR_FILENAME}=\\" >> $TARGET_FILE
echo "  ${JAR_URL}\\" >> $TARGET_FILE
echo "  #SIZE=${JAR_SIZE}&SHA-256=${JAR_CSUM}" >> $TARGET_FILE
echo "README.adoc=\\" >> $TARGET_FILE
echo "  ${DOC_URL}\\" >> $TARGET_FILE
echo "  #SIZE=${DOC_SIZE}" >> $TARGET_FILE
echo "" >> $TARGET_FILE

git add $TARGET_FILE
git config --global user.email "${GH_BOT_EMAIL}"
git config --global user.name "GitHub Action"
git commit -a -m "Releasing ${TAG} (${VERSION})"
git push origin main

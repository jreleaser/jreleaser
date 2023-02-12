#!/bin/sh

# env vars:
# VERSION: x.y.z or early-access
# TAG: vx.y.z or early-access
# GH_BOT_EMAIL
# JAVA_HOME

set -e

mv "wiki/Releases/wiki-release-page.md" "wiki/Releases/Release-${TAG}.md"
CMD="${JAVA_HOME}/bin/java checksums_sha256.txt wiki/Releases/Release-${TAG}.md"
exec CMD

cd wiki
git add "Releases/Release-${TAG}.md"
git config --global user.email "${GH_BOT_EMAIL}"
git config --global user.name "GitHub Action"
git commit -a -m "Releasing ${TAG} (${VERSION})"
git push origin main

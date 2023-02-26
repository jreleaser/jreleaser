#!/bin/bash

# env vars:
# VERSION: x.y.z or early-access
# TAG: vx.y.z or early-access
# GH_BOT_EMAIL
# JAVA_HOME

set -e

echo "📝 Updating release page"
PAGE="wiki/Releases/Release-${TAG}.md"
mv "wiki/Releases/wiki-release-page.md" "${PAGE}"
java .github/scripts/update_release_page.java checksums_sha256.txt ${PAGE}

if [ "${TAG}" != "early-access" ]; then
    echo "🚀️ Updating releases"
    RELEASES="wiki/Releases/Releases.md"
    HEAD=$(head -n 3 "${RELEASES}")
    TAIL=$(tail -n +4 "${RELEASES}")
    echo "${HEAD}" > "${RELEASES}"
    echo "* [JReleaser ${TAG}](https://github.com/jreleaser/jreleaser/wiki/Release-${TAG})" >> "${RELEASES}"
    echo "${TAIL}" >> "${RELEASES}"
fi

echo "⬆️ Updating wiki"
cd wiki
git add --verbose .
git config --global user.email "${GH_BOT_EMAIL}"
git config --global user.name "GitHub Action"
git commit -a -m "Releasing ${TAG} (${VERSION})"
git push origin master

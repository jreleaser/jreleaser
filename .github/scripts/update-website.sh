#!/bin/bash

# env vars:
# VERSION
# GH_BOT_EMAIL

set -e

echo "⚙️ Refreshing working copy"
git merge origin/development

echo "📝 Updating versions"
sed -i -e "s/^version\:\ early-access.*/version: latest/g" docs/antora.yml
sed -i -e "s/jreleaser-version\:\ .*/jreleaser-version: ${VERSION}/g" docs/antora.yml
sed -i -e "s/jreleaser-effective-version\:\ .*/jreleaser-effective-version: ${VERSION}/g" docs/antora.yml
sed -i -e "s/jreleaser-tag\:\ .*/jreleaser-tag: ${TAG}/g" docs/antora.yml
echo "${VERSION}" > VERSION

echo "📝 Updating release history"
ANCHOR_START="RELEASE-ANCHOR-START"
ANCHOR_END="RELEASE-ANCHOR-END"
PAGE="docs/modules/ROOT/pages/release-history.adoc"

AS=$(grep -hn "${ANCHOR_START}" ${PAGE} | awk -F: '{print $1}')
AE=$(grep -hn "${ANCHOR_END}" ${PAGE} | awk -F: '{print $1}')
AE=$((AE + 1))
PREVIOUS_RELEASE=$(grep -h -A5 "${ANCHOR_START}" ${PAGE} | tail -n 5)

HEAD=$(head -n "${AS}" "${PAGE}")
TAIL=$(tail -n +"${AE}" "${PAGE}")
ATAG="${TAG//\./-}"

cat << EOF > "${PAGE}"
${HEAD}
| $(date +%F)
| ${VERSION}
| link:https://jreleaser.noticeable.news/publications/release-${ATAG}[announcement],
  link:https://github.com/jreleaser/jreleaser/releases/tag/${TAG}[release notes],
  link:https://github.com/jreleaser/jreleaser/wiki/Release-${TAG}[binaries]
// ${ANCHOR_END}

${PREVIOUS_RELEASE}
${TAIL}
EOF

echo "📝 Updating schema"
java -jar jreleaser-cli.jar json-schema
cp "jreleaser-schema-${VERSION}.json" schema/
git add schema

PAGE="docs/modules/ROOT/pages/schema.adoc"

AS=$(grep -hn "${ANCHOR_START}" ${PAGE} | awk -F: '{print $1}')
AE=$(grep -hn "${ANCHOR_END}" ${PAGE} | awk -F: '{print $1}')
AE=$((AE + 1))
PREVIOUS_RELEASE=$(grep -h -A1 "${ANCHOR_START}" ${PAGE} | tail -n 1)

HEAD=$(head -n "${AS}" "${PAGE}")
TAIL=$(tail -n +"${AE}" "${PAGE}")

cat << EOF > "${PAGE}"
${HEAD}
 - link:https://jreleaser.org/schema/jreleaser-schema-${VERSION}.json[jreleaser-schema-${VERSION}.json]
// ${ANCHOR_END}
${PREVIOUS_RELEASE}
${TAIL}
EOF

echo "⬆️  Updating website"
git add VERSION
git config --global user.email "${GH_BOT_EMAIL}"
git config --global user.name "GitHub Action"
git commit -a -m "Releasing version ${VERSION}"
git push origin main

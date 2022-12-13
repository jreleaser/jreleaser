#!/bin/sh

# env vars:
# GITHUB_TOKEN
# TAG: vx.y.z or early-access
# REPO_OWNER
# REPO_NAME
# PROVENANCE_FILE: xxx.intoto.jsonl

set -e

echo "⬇️ Fetching release id for ${TAG}"
curl -s \
  -H "Accept: application/vnd.github+json" \
  -H "Authorization: Bearer $GITHUB_TOKEN"\
  -H "X-GitHub-Api-Version: 2022-11-28" \
  https://api.github.com/repos/${REPO_OWNER}/${REPO_NAME}/releases/tags/${TAG} \
  --output release.json

RELEASE_ID=`jq ".id" release.json`
SIZE=`ls -l ${PROVENANCE_FILE} | awk '{print $5}'`

echo "⬆️ Uploading provenance file"
curl -s \
  -X POST \
  -H "Accept: application/vnd.github+json" \
  -H "Authorization: Bearer $GITHUB_TOKEN"\
  -H "X-GitHub-Api-Version: 2022-11-28" \
  -H "Content-Length: $SIZE" \
  -H "Content-Type: application/json" \
  https://uploads.github.com/repos/${REPO_OWNER}/${REPO_NAME}/releases/${RELEASE_ID}/assets?name=${PROVENANCE_FILE} \
  --data-binary "@${PROVENANCE_FILE}"

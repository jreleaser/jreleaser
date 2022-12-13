#!/bin/sh

# env vars:
# VERSION
# GITHUB_BOT_EMAIL

set -e

git merge origin/development
sed -i -e "s/^\version\:\ early-access.*/version: latest/g" docs/antora.yml
sed -i -e "s/jreleaser-version\:\ .*/jreleaser-version: ${VERSION}/g" docs/antora.yml
echo ${VERSION} > VERSION
git add VERSION
git config --global user.email "${GITHUB_BOT_EMAIL}"
git config --global user.name "GitHub Action"
git commit -a -m "Releasing version ${VERSION}"
git push origin main
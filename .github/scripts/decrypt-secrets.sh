#!/bin/sh

gpg --quiet --batch --yes --decrypt --passphrase="$GPG_PASSPHRASE" --output $HOME/.github-secrets.tar .github-secrets.tar.gpg
tar xvf $HOME/.github-secrets.tar

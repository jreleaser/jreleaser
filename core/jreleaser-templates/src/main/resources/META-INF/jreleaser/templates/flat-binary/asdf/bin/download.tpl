#!/usr/bin/env bash

set -euo pipefail

current_script_path=${BASH_SOURCE[0]}
plugin_dir=$(dirname "$(dirname "$current_script_path")")

# shellcheck source=../lib/utils.bash
source "${plugin_dir}/lib/utils.bash"

mkdir -p "$ASDF_DOWNLOAD_PATH"

release_file="$ASDF_DOWNLOAD_PATH/{{asdfDistributionArtifactFile}}"

# Download distribution file to the download directory
download_release "$ASDF_INSTALL_VERSION" "$release_file"

# Copy distribution file into the download directory
copy_release "$ASDF_INSTALL_VERSION" "$release_file" || fail "Could not copy $release_file"


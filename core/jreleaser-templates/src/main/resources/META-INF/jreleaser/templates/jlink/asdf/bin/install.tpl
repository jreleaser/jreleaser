#!/usr/bin/env bash

set -euo pipefail

current_script_path=${BASH_SOURCE[0]}
plugin_dir=$(dirname "$(dirname "$current_script_path")")

# shellcheck source=../lib/utils.bash
source "${plugin_dir}/lib/utils.bash"

install_version "$ASDF_INSTALL_TYPE" "$ASDF_INSTALL_VERSION" "$ASDF_INSTALL_PATH"

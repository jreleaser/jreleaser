#!/usr/bin/env bash

set -euo pipefail

TOOL_REPO="{{repoUrl}}"
TOOL_NAME="{{distributionExecutable}}"
TOOL_TEST="{{asdfPluginToolCheck}}"

fail() {
  echo -e "asdf-$TOOL_NAME: $*"
  exit 1
}

curl_opts=(-fsSL)

# NOTE: You might want to remove this if {{distributionExecutable}} is not hosted on GitHub releases.
if [ -n "${GITHUB_API_TOKEN:-}" ]; then
  curl_opts=("${curl_opts[@]}" -H "Authorization: token $GITHUB_API_TOKEN")
fi

sort_versions() {
  sed 'h; s/[+-]/./g; s/.p\([[:digit:]]\)/.z\1/; s/$/.z/; G; s/\n/ /' |
    LC_ALL=C sort -t. -k 1,1 -k 2,2n -k 3,3n -k 4,4n -k 5,5n | awk '{print $2}'
}

list_github_tags() {
  git ls-remote --tags --refs "$TOOL_REPO" |
    grep -o 'refs/tags/.*' | cut -d/ -f3- |
    sed 's/^v//' # NOTE: You might want to adapt this sed to remove non-version strings from tags
}

list_all_versions() {
  # TODO: Adapt this. By default we simply list the tag names from GitHub releases.
  # Change this function if{{distributionExecutable}} has other means of determining installable versions.
  list_github_tags
}

download_release() {
  local version="$1"
  local filename="$2"
  # TODO: Adapt the release URL convention for {{distributionExecutable}}
  local url="{{asdfDistributionUrl}}"

  echo "* Downloading $TOOL_NAME release $version..."
  curl "${curl_opts[@]}" -o "$filename" -C - "$url" || fail "Could not download $url"
}

extract_release() {
  local version="$1"
  local filename="$2"

  if [[ $filename == *.zip ]]
  then
    local tmp_download_dir
    tmp_download_dir=$(mktemp -d -t asdf_extract_XXXXXXX)

    (
      set -e

      cd "$tmp_download_dir"
      unzip -q "$filename" && mv "{{asdfDistributionArtifactFileName}}"/* "$ASDF_DOWNLOAD_PATH"
    )
  else
    tar -xvf $filename -C "$ASDF_DOWNLOAD_PATH" --strip-components=1
  fi
}

install_version() {
  local install_type="$1"
  local version="$2"
  local install_path="$3"

  if [ "$install_type" != "version" ]; then
    fail "asdf-$TOOL_NAME supports release installs only"
  fi

  (
    mkdir -p "$install_path"
    cp -r "$ASDF_DOWNLOAD_PATH"/* "$install_path"

    # TODO: Assert {{distributionExecutable}} executable exists.
    local tool_cmd
    tool_cmd="$(echo "$TOOL_TEST" | cut -d' ' -f1)"
    test -x "$install_path/bin/$tool_cmd" || fail "Expected $install_path/bin/$tool_cmd to be executable."

    echo "$TOOL_NAME $version installation was successful!"
  ) || (
    rm -rf "$install_path"
    fail "An error occurred while installing $TOOL_NAME $version."
  )
}

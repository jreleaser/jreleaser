#!/bin/sh

#
# SPDX-License-Identifier: Apache-2.0
#
# Copyright 2020-2025 The JReleaser authors.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     https://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

set -u
set -e

url_prefix="https://github.com/jreleaser/jreleaser/releases/download/"
temp_prefix="${TMPDIR:-/tmp}"
base_jreleaser_cache="$HOME/.jreleaser/caches/jreleaser/distributions"
current_jreleaser_distribution="$base_jreleaser_cache/latest/jreleaser"

has_curl() {
  command -v curl >/dev/null 2>&1
}

has_wget() {
  command -v wget >/dev/null 2>&1
}

isexec() {
  if [ -f "$1" ] && [ -x $(realpath "$1") ]; then
    RET_VAL=true
  else
    RET_VAL=false
  fi
}

check_cache() {
  local latest_version=$1

  local do_install=false
  if [ -f "$current_jreleaser_distribution" ]; then
    say "Previous JReleaser distribution found in cache. Checking compatibility"
    chmod +x "$current_jreleaser_distribution"

    if $(isexec "$current_jreleaser_distribution"); then
      local version_banner=$($current_jreleaser_distribution -V)
      local local_version=
      local re='.*jreleaser ([1-9]\.[0-9]+\.[0-9]+).*'
      if [[ $version_banner =~ $re ]]; then
        local_version="${BASH_REMATCH[1]}"
      else
        say_err "Could not determine JReleaser version"
        do_install=true
      fi

      if [ "$latest_version" != "$local_version" ]; then
        do_install=true
      fi
    else
      say_err "Could not determine JReleaser version"
      do_install=true
    fi
  else
    say "No previous JReleaser distribution found in cache"
    do_install=true
  fi

  RET_VAL=$do_install
}

install() {
  local latest_version=
  if has_curl; then
    latest_version=$(curl -sSfL https://jreleaser.org/releases/latest/download/VERSION)
  elif has_wget; then
    latest_version=$(wget -qO - https://jreleaser.org/releases/latest/download/VERSION)
  fi

  check_cache "$latest_version" || return 1
  local do_install=$RET_VAL

  if [ "$do_install" = "false" ]; then
    say "JReleaser $latest_version already installed"
    exit 0
  fi

  get_platform || return 1
  local platform=$RET_VAL
  local filename=
  local distroname="jreleaser"
  local url="$url_prefix"v"$latest_version/"
  local mode=""

  case $platform in
  *osx-x86_64* | *osx-aarch64* | *linux-x86_64* | *linux-aarch64* | *windows-x86_64*)
    # native executable
    distroname="$distroname-native-$latest_version-$platform"
    mode="native"
    ;;
  *windows-aarch64*)
    # bundled Java runtime
    distroname="$distroname-standalone-$latest_version-$platform"
    mode="standalone"
    ;;
  *)
    # requires Java
    distroname="$distroname-$latest_version"
    mode="java"
    ;;
  esac

  filename="${distroname}.zip"
  url="$url$filename"

  say "Downloading $filename"
  if has_curl; then
    curl -sSfL "$url" -o "$temp_prefix/$filename"
  elif has_wget; then
    wget -qO "$temp_prefix/$filename" "$url"
  fi

  mkdir -p $base_jreleaser_cache
  unzip -q -o -d $base_jreleaser_cache "$temp_prefix/$filename"
  mkdir -p $(dirname $current_jreleaser_distribution)
  rm -f $current_jreleaser_distribution
  ln -s "$base_jreleaser_cache/$distroname/bin/jreleaser" $current_jreleaser_distribution
  chmod +x $current_jreleaser_distribution
  local version_banner=$($current_jreleaser_distribution -V)
  echo "$version_banner"

  say "Add $(dirname $current_jreleaser_distribution) to your \$PATH"
  if [ "$mode" = "java" ]; then
    say "You need a Java version 8 or greater to run JReleaser"
  fi
}

get_platform() {
  # Get OS/CPU info and store in a `myos` and `mycpu` variable.
  local ucpu=$(uname -m)
  local uos=$(uname)
  local ucpu=$(echo $ucpu | tr "[:upper:]" "[:lower:]")
  local uos=$(echo $uos | tr "[:upper:]" "[:lower:]")

  case $uos in
  *linux*)
    local myos="linux"
    ;;
  *dragonfly*)
    local myos="freebsd"
    ;;
  *freebsd*)
    local myos="freebsd"
    ;;
  *openbsd*)
    local myos="openbsd"
    ;;
  *netbsd*)
    local myos="netbsd"
    ;;
  *darwin*)
    local myos="osx"
    if [ "$HOSTTYPE" = "x86_64" ]; then
      local ucpu="x86_64"
    fi
    if [ "$HOSTTYPE" = "arm64" ]; then
      local ucpu="aarch64"
    fi
    ;;
  *aix*)
    local myos="aix"
    ;;
  *solaris* | *sun*)
    local myos="solaris"
    ;;
  *haiku*)
    local myos="haiku"
    ;;
  *mingw* | *msys*)
    local myos="windows"
    ;;
  *)
    err "unknown operating system: $uos"
    ;;
  esac

  case $ucpu in
  *i386* | *i486* | *i586* | *i686* | *bepc* | *i86pc*)
    local mycpu="i386"
    ;;
  *amd*64* | *x86-64* | *x86_64*)
    local mycpu="x86_64"
    ;;
  *sparc* | *sun*)
    local mycpu="sparc"
    if [ "$(isainfo -b)" = "64" ]; then
      local mycpu="sparc64"
    fi
    ;;
  *ppc64*)
    local mycpu="powerpc64"
    ;;
  *power* | *ppc*)
    local mycpu="powerpc"
    ;;
  *mips*)
    local mycpu="mips"
    ;;
  *arm* | *armv6l*)
    local mycpu="arm"
    ;;
  *aarch64*)
    local mycpu="aarch64"
    ;;
  *)
    err "unknown processor: $ucpu"
    ;;
  esac

  RET_VAL="$myos"-"$mycpu"
}

say() {
  echo "setup-jreleaser: $1"
}

say_err() {
  say "Error: $1" >&2
}

err() {
  say_err "$1"
  exit 1
}

install

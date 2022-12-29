name: Release

on:
  push:
    tags:
      - '*'

jobs:
  release:
    name: Release
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
        with:
          fetch-depth: 0

      - name: Variables
        id: vars
        run: |
          echo "version=$(cat VERSION)" >> $GITHUB_OUTPUT
          echo "tag=${GITHUB_REF#refs/tags/}" >> $GITHUB_OUTPUT

      - name: Install libfuse
        run: |
          sudo apt-get update -y
          sudo apt-get install fuse

      - name: Create the AppImage
        run: sh create-appimage.sh
        env:
          DISTRIBUTION_VERSION: ${{=<% %>=}}{{ steps.vars.outputs.version }}<%={{ }}=%>
          DISTRIBUTION_TAG: ${{=<% %>=}}{{ steps.vars.outputs.tag }}<%={{ }}=%>

      - name: Release
        uses: jreleaser/release-action@v2
        with:
          version: latest
          arguments: full-release
        env:
          JRELEASER_PROJECT_VERSION: ${{=<% %>=}}{{ steps.vars.outputs.version }}<%={{ }}=%>
          JRELEASER_GITHUB_TOKEN: ${{=<% %>=}}{{ secrets.GITHUB_TOKEN }}<%={{ }}=%>

      - name: JReleaser output
        if: always()
        uses: actions/upload-artifact@v3
        with:
          name: jreleaser-logs
          path: |
            out/jreleaser/trace.log
            out/jreleaser/output.properties

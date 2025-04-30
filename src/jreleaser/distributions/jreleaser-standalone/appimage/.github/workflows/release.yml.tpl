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
      - uses: actions/checkout@v4
        with:
          fetch-depth: 0

      - name: Variables
        id: vars
        run: |
          echo "version=$(cat VERSION)" >>$GITHUB_OUTPUT
          echo "tag=${GITHUB_REF#refs/tags/}" >>$GITHUB_OUTPUT

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
          JRELEASER_GPG_PASSPHRASE: ${{=<% %>=}}{{ secrets.GPG_PASSPHRASE }}<%={{ }}=%>
          JRELEASER_GPG_PUBLIC_KEY: ${{=<% %>=}}{{ secrets.GPG_PUBLIC_KEY }}<%={{ }}=%>
          JRELEASER_GPG_SECRET_KEY: ${{=<% %>=}}{{ secrets.GPG_SECRET_KEY }}<%={{ }}=%>
          JRELEASER_MASTODON_ACCESS_TOKEN: ${{=<% %>=}}{{ secrets.MASTODON_ACCESS_TOKEN }}<%={{ }}=%>
          JRELEASER_BLUESKY_HOST: ${{=<% %>=}}{{ secrets.BLUESKY_HOST }}<%={{ }}=%>
          JRELEASER_BLUESKY_HANDLE: ${{=<% %>=}}{{ secrets.BLUESKY_HANDLE }}<%={{ }}=%>
          JRELEASER_BLUESKY_PASSWORD: ${{=<% %>=}}{{ secrets.BLUESKY_PASSWORD }}<%={{ }}=%>

      - name: JReleaser output
        if: always()
        uses: actions/upload-artifact@v4
        with:
          name: jreleaser-logs
          path: |
            out/jreleaser/trace.log
            out/jreleaser/output.properties

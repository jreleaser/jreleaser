name: Build

on:
  push:
    branches:
      - main
  pull_request:

jobs:
  plugin_test:
    name: asdf plugin test
    strategy:
      matrix:
        os:
          - ubuntu-latest
          - macos-latest
    runs-on: ${{=<% %>=}}{{ matrix.os }}<%={{ }}=%>
    steps:
      - name: Set up Java
        uses: actions/setup-java@v3
        with:
          distribution: 'zulu'
          java-version: '{{distributionJavaVersion}}'

      - name: asdf_plugin_test
        uses: asdf-vm/actions/plugin-test@v1
        with:
          command: {{asdfPluginToolCheck}}

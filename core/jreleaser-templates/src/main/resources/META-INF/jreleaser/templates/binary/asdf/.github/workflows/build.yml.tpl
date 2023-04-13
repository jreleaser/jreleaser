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
      - name: asdf_plugin_test
        uses: asdf-vm/actions/plugin-test@v2
        with:
          command: {{asdfPluginToolCheck}}

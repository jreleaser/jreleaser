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
        uses: asdf-vm/actions/plugin-test@b7bcd026f18772e44fe1026d729e1611cc435d47 #v4.0.1
        with:
          command: {{asdfPluginToolCheck}}

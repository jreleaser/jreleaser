name: build

on:
  push:
    branches: [ main ]
  pull_request:
    branches: [ main ]

jobs:
  build:
    runs-on: windows-latest
    name: build
    steps:
      - name: Checkout
        uses: actions/checkout@v2.3.4

      - name: Setup .Net
        uses: actions/setup-dotnet@v1.7.2
        with:
          dotnet-version: '2.2.204' # SDK Version to use.

      - name: Build
        run: |
          powershell
          choco pack {{distributionPackageDirectory}}/chocolatey/{{distributionExecutable}}.nuspec

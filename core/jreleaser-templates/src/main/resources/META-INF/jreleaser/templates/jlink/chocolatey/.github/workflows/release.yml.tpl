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

      - name: Nuget
        continue-on-error: true
        env:
          GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}
        run: |
          powershell
          dotnet source Add -Name "GitHub" -Source "https://nuget.pkg.github.com/{{chocolateyUsername}}/index.json"
          dotnet nuget setApiKey $GITHUB_TOKEN -Source "GitHub"
          dotnet nuget push $(ls *.nupkg | % {$_.FullName}) --source "GitHub"

      - name: Publish
        continue-on-error: true
        run: |
          powershell
          choco apikey -k ${{ secrets.CHOCOLATEY_API_KEY }} -source https://push.chocolatey.org/
          choco push $(ls *.nupkg | % {$_.FullName}) -s https://push.chocolatey.org/

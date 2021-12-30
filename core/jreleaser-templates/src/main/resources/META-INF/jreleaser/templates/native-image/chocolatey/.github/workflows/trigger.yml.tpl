name: Trigger

on:
  workflow_dispatch:

jobs:
  trigger:
    runs-on: windows-latest
    
    steps:
      - uses: actions/checkout@v2
      
      - uses: actions/setup-dotnet@v1
        with:
          dotnet-version: '2.2.204'
          
      - name: Pack
        run: |
          powershell
          choco pack {{distributionName}}/{{chocolateyPackageName}}.nuspec
          
      - name: Publish
        run: |
          powershell
          choco apikey -k ${{ secrets.CHOCOLATEY_API_KEY  }} -source https://push.chocolatey.org/
          choco push $(ls *.nupkg | % {$_.FullName}) -s https://push.chocolatey.org/

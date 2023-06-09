name: Push-{{distributionName}}

on:
  push:
    tags:
      - '*'
    branches-ignore:
      - '**'

jobs:
  push:
    runs-on: windows-latest
    
    steps:
      - uses: actions/checkout@v3
      
      - uses: actions/setup-dotnet@v3
        with:
          dotnet-version: '5.0.x'

      - name: Find package
        shell: bash
        run: |
          echo "PACKAGE_NAME=$(ls {{chocolateyPackageName}}/*.nuspec)" >> $GITHUB_ENV

      - name: Pack
        shell: powershell
        run: |
          choco pack ${{=<% %>=}}{{ env.PACKAGE_NAME }}<%={{ }}=%>

      - name: Publish
        shell: powershell
        run: |
          choco apikey -k ${{=<% %>=}}{{ secrets.CHOCOLATEY_API_KEY }}<%={{ }}=%> -s {{chocolateySource}}
          choco push $(ls *.nupkg | % {$_.FullName}) -s {{chocolateySource}}

name: X-choco-push

on:
  workflow_call:
    inputs:
      choco-package-name:
        required: true
        type: string
      choco-source:
        required: true
        type: string
    secrets:
      choco-api-key:
        required: true

permissions:
  contents: read

jobs:
  choco:
    runs-on: windows-latest
    steps:
      - uses: actions/checkout@de0fac2e4500dabe0009e67214ff5f5447ce83dd # v6.0.2
        with:
          persist-credentials: false

      - uses: actions/setup-dotnet@c2fa09f4bde5ebb9d1777cf28262a3eb3db3ced7 # v5.2.0
        with:
          dotnet-version: '8.0.x'

      - name: Find package
        shell: bash
        run: |
          echo "PACKAGE_NAME=$(ls ${CHOCO_PACKAGE_NAME}/*.nuspec)" >> $GITHUB_ENV
        env:
          CHOCO_PACKAGE_NAME: ${{=<% %>=}}{{ inputs.choco-package-name }}<%={{ }}=%>

      - name: Pack
        shell: powershell
        run: |
          choco pack ${env:PACKAGE_NAME}
        env:
          PACKAGE_NAME: ${{=<% %>=}}{{ env.PACKAGE_NAME }}<%={{ }}=%>

      - name: Publish
        shell: powershell
        run: |
          choco apikey -k ${{=<% %>=}}{{ secrets.choco-api-key }}<%={{ }}=%> -s ${env:CHOCO_SOURCE}
          choco push $(ls *.nupkg | % {$_.FullName}) -s ${env:CHOCO_SOURCE}
        env:
          CHOCO_SOURCE: ${{=<% %>=}}{{ inputs.choco-source }}<%={{ }}=%>

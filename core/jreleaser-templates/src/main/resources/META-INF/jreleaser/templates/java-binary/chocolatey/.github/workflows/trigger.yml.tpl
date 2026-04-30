name: Trigger-{{chocolateyPackageName}}

on:
  workflow_dispatch:

permissions:
  contents: read

jobs:
  trigger:
    uses: ./.github/workflows/choco.yml
    with:
      choco-package-name: {{chocolateyPackageName}}
      choco-source: {{chocolateySource}}
    secrets:
      choco-api-key: ${{=<% %>=}}{{ secrets.CHOCOLATEY_API_KEY }}<%={{ }}=%>

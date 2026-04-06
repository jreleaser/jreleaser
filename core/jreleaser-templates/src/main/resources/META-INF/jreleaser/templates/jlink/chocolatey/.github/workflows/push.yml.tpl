name: Push-{{chocolateyPackageName}}

on:
  push:
    tags:
      - '*'
    branches-ignore:
      - '**'

permissions:
  contents: read

jobs:
  push:
    uses: ./.github/workflows/choco.yml@main
    with:
      choco-package-name: {{chocolateyPackageName}}
      choco-source: {{chocolateySource}}
    secrets:
      choco-api-key: ${{=<% %>=}}{{ secrets.CHOCOLATEY_API_KEY }}<%={{ }}=%>

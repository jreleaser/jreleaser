# [JRELEASER_VERSION]
$tools = Split-Path $MyInvocation.MyCommand.Definition
$package = Split-Path $tools
$app_home = Join-Path $package '{{projectName}}-{{projectVersion}}'
$app_bat = Join-Path $app_home 'bin/{{distributionExecutable}}.{{distributionExecutableExtension}}'

Uninstall-BinFile -Name '{{distributionExecutable}}' -Path $app_bat

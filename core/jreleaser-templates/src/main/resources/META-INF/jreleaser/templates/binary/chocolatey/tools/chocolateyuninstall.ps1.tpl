# {{jreleaserCreationStamp}}
$tools = Split-Path $MyInvocation.MyCommand.Definition
$package = Split-Path $tools
$app_home = Join-Path $package '{{distributionArtifactFileName}}'
$app_exe = Join-Path $app_home 'bin/{{distributionExecutable}}.exe'

Uninstall-BinFile -Name '{{distributionExecutable}}' -Path $app_exe

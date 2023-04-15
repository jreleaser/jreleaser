# {{jreleaserCreationStamp}}
$tools = Split-Path $MyInvocation.MyCommand.Definition
$package = Split-Path $tools
$app_home = Join-Path $package '{{distributionArtifactRootEntryName}}'
$app_exe = Join-Path $app_home 'bin/{{distributionExecutableWindows}}'

Uninstall-BinFile -Name '{{distributionExecutableName}}' -Path $app_exe

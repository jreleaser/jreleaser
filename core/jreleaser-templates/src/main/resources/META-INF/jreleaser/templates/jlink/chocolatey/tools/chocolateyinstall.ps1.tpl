# {{jreleaserCreationStamp}}
$tools = Split-Path $MyInvocation.MyCommand.Definition
$package = Split-Path $tools
$app_home = Join-Path $package '{{distributionArtifactRootEntryName}}'
$app_exe = Join-Path $app_home 'bin/{{distributionExecutableWindows}}'

Install-ChocolateyZipPackage `
    -PackageName '{{chocolateyPackageName}}' `
    -Url '{{distributionUrl}}' `
    -Checksum '{{distributionChecksumSha256}}' `
    -ChecksumType 'sha256' `
    -UnzipLocation $package

Install-BinFile -Name '{{distributionExecutableName}}' -Path $app_exe
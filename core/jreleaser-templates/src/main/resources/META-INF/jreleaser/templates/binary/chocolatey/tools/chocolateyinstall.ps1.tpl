# [JRELEASER_VERSION]
$tools = Split-Path $MyInvocation.MyCommand.Definition
$package = Split-Path $tools
$app_home = Join-Path $package '{{distributionArtifactFileName}}'
$app_exe = Join-Path $app_home 'bin/{{distributionExecutable}}.exe'

Install-ChocolateyZipPackage `
    -PackageName '{{chocolateyPackageName}}' `
    -Url '{{distributionUrl}}' `
    -Checksum '{{distributionChecksumSha256}}' `
    -ChecksumType 'sha256' `
    -UnzipLocation $package

Install-BinFile -Name '{{distributionExecutable}}' -Path $app_exe
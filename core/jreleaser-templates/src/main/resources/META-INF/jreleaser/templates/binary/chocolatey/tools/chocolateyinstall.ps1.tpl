# {{jreleaserCreationStamp}}
$tools = Split-Path $MyInvocation.MyCommand.Definition
$package = Split-Path $tools

Install-ChocolateyZipPackage `
    -PackageName '{{chocolateyPackageName}}' `
    -Url '{{distributionUrl}}' `
    -Checksum '{{distributionChecksumSha256}}' `
    -ChecksumType 'sha256' `
    -UnzipLocation $package
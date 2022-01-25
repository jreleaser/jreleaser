# {{jreleaserCreationStamp}}
$toolsDir   = "$(Split-Path -Parent $MyInvocation.MyCommand.Definition)"

$packageArgs = @{
  packageName   = '{{chocolateyPackageName}}'
  fileType      = '{{distributionArtifactFileFormat}}'
  url           = '{{distributionUrl}}'
  silentArgs    = "/quiet"
  validExitCodes= @(0)
  softwareName  = '{{chocolateyPackageName}}*'
  checksum      = '{{distributionChecksumSha256}}'
  checksumType  = 'sha256'
}

Install-ChocolateyPackage @packageArgs
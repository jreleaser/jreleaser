# {{jreleaserCreationStamp}}
cask "{{brewCaskName}}" do
  desc "{{projectDescription}}"
  homepage "{{projectWebsite}}"
  url "{{distributionUrl}}",
      verified: "{{repoHost}}"
  version "{{projectVersion}}"
  sha256 "{{distributionChecksumSha256}}"
  name "{{brewCaskDisplayName}}"
  {{#brewCaskHasAppcast}}
  appcast {{brewCaskAppcast}}
  {{/brewCaskHasAppcast}}
  auto_updates true

  {{#brewHasLivecheck}}
  livecheck do
    {{#brewLivecheck}}
    {{.}}
    {{/brewLivecheck}}
  end
  {{/brewHasLivecheck}}
  {{#brewDependencies}}
  depends_on {{.}}
  {{/brewDependencies}}

  {{#brewCaskHasPkg}}
  pkg "{{brewCaskPkg}}"
  {{/brewCaskHasPkg}}
  {{#brewCaskHasApp}}
  app "{{brewCaskApp}}"
  {{/brewCaskHasApp}}
  {{#brewCaskHasBinary}}
  binary "{{distributionArtifactFileName}}/bin/{{distributionExecutableUnix}}"
  {{/brewCaskHasBinary}}
  {{#brewCaskHasUninstall}}
  {{#brewCaskUninstall}}
  uninstall {{name}}: [
    {{#items}}
    "{{.}}",
    {{/items}}
  ]
  {{/brewCaskUninstall}}
  {{/brewCaskHasUninstall}}
  {{#brewCaskHasZap}}
  {{#brewCaskZap}}
  zap {{name}}: [
    {{#items}}
    "{{.}}",
    {{/items}}
  ]
  {{/brewCaskZap}}
  {{/brewCaskHasZap}}
end

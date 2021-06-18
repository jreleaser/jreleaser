class {{brewFormulaName}} < Formula
  desc "{{projectDescription}}"
  homepage "{{projectWebsite}}"
  version "{{projectVersion}}"
  url "{{distributionUrl}}"
  sha256 "{{distributionChecksumSha256}}"
  license "{{projectLicense}}"

  bottle :unneeded

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

  def install
    libexec.install Dir["*"]
    bin.install_symlink "#{libexec}/{{distributionExecutable}}"
  end

  test do
    output = shell_output("#{libexec}/{{distributionExecutable}} --version")
    assert_match "{{projectVersion}}", output
  end
end

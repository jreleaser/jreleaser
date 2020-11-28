class {{projectNameCapitalized}} < Formula
  desc "{{projectDescription}}"
  homepage "{{projectWebsite}}"
  url "{{distributionUrl}}"
  sha256 "{{distributionSha256}}"
  license "{{projectLicense}}"

  bottle :unneeded

  {{#brewDependencies}}
  depends_on {{formattedDependency}}
  {{/brewDependencies}}

  def install
    libexec.install Dir["*"]
    bin.install_symlink "#{libexec}/bin/{{distributionExecutable}}"
  end

  test do
    output = shell_output("#{bin}/{{distributionExecutable}} --version")
    assert_match "{{projectVersion}}", output
  end
end

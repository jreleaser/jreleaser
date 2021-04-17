class {{brewFormulaName}} < Formula
  desc "{{projectDescription}}"
  homepage "{{projectWebsite}}"
  version "{{projectVersion}}"
  url "{{distributionUrl}}", :using => :nounzip
  sha256 "{{distributionSha256}}"
  license "{{projectLicense}}"

  bottle :unneeded

  {{#brewDependencies}}
  depends_on {{.}}
  {{/brewDependencies}}

  def install
    File.open("{{distributionExecutable}}", "w") do |f|
      f << "#!/bin/bash\n"
      f << "java -jar $JAVA_OPTS #{prefix}/{{artifactFileName}} \"$@\"\n"
    end
    prefix.install "{{artifactFileName}}"
    bin.install "{{distributionExecutable}}"
  end

  test do
    output = shell_output("#{bin}/{{distributionExecutable}} --version")
    assert_match "{{projectVersion}}", output
  end
end

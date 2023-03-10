# {{jreleaserCreationStamp}}
class {{brewFormulaName}} < Formula
  desc "{{projectDescription}}"
  homepage "{{projectLinkHomepage}}"
  url "{{distributionUrl}}", :using => :nounzip
  version "{{projectVersion}}"
  sha256 "{{distributionChecksumSha256}}"
  license "{{projectLicense}}"

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
    libexec.install "{{distributionArtifactFile}}"

    bin.mkpath
    File.open("#{bin}/{{distributionExecutableName}}", "w") do |f|
      f.write <<~EOS
        #!/bin/bash
        export JAVA_HOME="#{Language::Java.overridable_java_home_env(nil)[:JAVA_HOME]}"
        {{#distributionJavaMainModule}}
        exec "${JAVA_HOME}/bin/java" -p #{libexec}/{{distributionArtifactFile}} -m {{distributionJavaMainModule}}/{{distributionJavaMainClass}} "$@"
        {{/distributionJavaMainModule}}
        {{^distributionJavaMainModule}}
        exec "${JAVA_HOME}/bin/java" -jar #{libexec}/{{distributionArtifactFile}} "$@"
        {{/distributionJavaMainModule}}
      EOS
    end
  end

  test do
    output = shell_output("#{bin}/{{distributionExecutableName}} --version")
    assert_match "{{projectVersion}}", output
  end
end

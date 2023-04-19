#!/bin/sh
# {{jreleaserCreationStamp}}

{{#distributionJavaMainModule}}
$JAVA_HOME/bin/java $JAVA_OPTS \
    -p /{{jibWorkingDirectory}}/{{distributionArtifactFileName}}/lib/{{distributionArtifactFile}} \
    -m {{distributionJavaMainModule}}/{{distributionJavaMainClass}} \
    "$@"
{{/distributionJavaMainModule}}
{{^distributionJavaMainModule}}
$JAVA_HOME/bin/java $JAVA_OPTS \
    -jar /{{jibWorkingDirectory}}/{{distributionArtifactFileName}}/lib/{{distributionArtifactFile}} \
    "$@"
{{/distributionJavaMainModule}}
#!/bin/sh
# {{jreleaserCreationStamp}}

{{#distributionJavaMainModule}}
$JAVA_HOME/bin/java $JAVA_OPTS \
    -p /{{distributionName}}-{{projectVersion}}/lib/{{distributionArtifactFile}} \
    -m {{distributionJavaMainModule}}/{{distributionJavaMainClass}} \
    "$@"
{{/distributionJavaMainModule}}
{{^distributionJavaMainModule}}
$JAVA_HOME/bin/java $JAVA_OPTS \
    -jar /{{distributionName}}-{{projectVersion}}/lib/{{distributionArtifactFile}} \
    "$@"
{{/distributionJavaMainModule}}
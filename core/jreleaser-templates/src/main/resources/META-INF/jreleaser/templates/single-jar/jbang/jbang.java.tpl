//usr/bin/env jbang "$0" "$@" ; exit $?
//JAVA {{distributionJavaVersion}}+
//DEPS {{jbangDistributionGA}}:{{projectVersion}}
// {{jreleaserCreationStamp}}

public class {{jbangScriptName}} {
    public static void main(String... args) throws Exception {
        {{distributionJavaMainClass}}.main(args);
    }
}

//usr/bin/env jbang "$0" "$@" ; exit $?
//JAVA {{distributionJavaVersion}}
//DEPS {{jbangDistributionGA}}:{{projectVersion}}

public class {{jbangAliasClassName}} {
    public static void main(String... args) throws Exception {
        {{distributionJavaMainClass}}.main(args);
    }
}

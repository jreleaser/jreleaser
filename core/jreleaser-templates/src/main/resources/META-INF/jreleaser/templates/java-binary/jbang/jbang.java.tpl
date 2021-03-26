//usr/bin/env jbang "$0" "$@" ; exit $?
//JAVA {{distributionJavaVersion}}
//DEPS {{jbangDistributionGA}}:{{repoBranch}}

public class {{jbangAliasClassName}} {
    public static void main(String... args) throws Exception {
        {{distributionMainClass}}.main(args);
    }
}

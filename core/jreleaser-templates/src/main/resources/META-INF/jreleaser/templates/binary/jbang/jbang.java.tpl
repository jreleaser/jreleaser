//usr/bin/env jbang "$0" "$@" ; exit $?
//JAVA {{distributionJavaVersion}}
//DEPS {{reverseRepoHost}}.{{repoOwner}}.{{repoName}}:{{distributionArtifactId}}:{{repoBranch}}

public class {{jbangAliasClassName}} {
    public static void main(String... args) throws Exception {
        {{mainClass}}.main(args);
    }
}

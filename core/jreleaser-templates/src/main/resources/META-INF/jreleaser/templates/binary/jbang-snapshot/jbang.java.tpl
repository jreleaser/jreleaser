//usr/bin/env jbang "$0" "$@" ; exit $?
//JAVA {{distributionJavaVersion}}
//REPOS jitpack
//DEPS {{reverseRepoHost}}.{{repoOwner}}.{{repoName}}:{{distributionArtifactId}}:{{repoBranch}}-SNAPSHOT

public class {{jbangAliasClassName}} {
    public static void main(String... args) throws Exception {
        {{mainClass}}.main(args);
    }
}

/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2022 The JReleaser authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jreleaser.sdk.git;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.errors.RepositoryNotFoundException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.transport.RemoteConfig;
import org.eclipse.jgit.transport.URIish;
import org.jreleaser.bundle.RB;
import org.jreleaser.model.JReleaserContext;
import org.jreleaser.model.releaser.spi.Commit;
import org.jreleaser.model.releaser.spi.Repository;
import org.jreleaser.util.Env;
import org.jreleaser.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.jreleaser.util.StringUtils.isBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class GitSdk {
    public static final String REFS_TAGS = "refs/tags/";
    public static final String REFS_HEADS = "refs/heads/";
    public static final String REFS_REMOTES = "refs/remotes/";

    private final File basedir;
    private final boolean gitRootSearch;

    private GitSdk(File basedir, boolean gitRootSearch) {
        this.basedir = basedir;
        this.gitRootSearch = gitRootSearch;
    }

    public Git open() throws IOException {
        if (!gitRootSearch) {
            return Git.open(basedir);
        }

        File dir = basedir;

        while (dir != null) {
            try {
                return Git.open(dir);
            } catch (RepositoryNotFoundException e) {
                dir = dir.getParentFile();
            }
        }

        throw new RepositoryNotFoundException(basedir);
    }

    public Repository getRemote() throws IOException {
        Git git = open();

        String remote = resolveDefaultGitRemoteName();

        try {
            RemoteConfig remoteConfig = git.remoteList().call().stream()
                .filter(rc -> remote.equals(rc.getName()))
                .findFirst()
                .orElseThrow(() -> new IOException(RB.$("ERROR_git_repository_remote", remote)));

            List<URIish> uris = remoteConfig.getURIs();
            if (uris.isEmpty()) {
                // better be safe than sorry
                throw new IOException(RB.$("ERROR_git_repository_remote_missing_url", remote));
            }

            // grab the first one
            URIish uri = uris.get(0);

            Repository.Kind kind = Repository.Kind.OTHER;
            switch (uri.getHost()) {
                case "github.com":
                    kind = Repository.Kind.GITHUB;
                    break;
                case "gitlab.com":
                    kind = Repository.Kind.GITLAB;
                    break;
                case "codeberg.org":
                    kind = Repository.Kind.CODEBERG;
                    break;
            }

            String[] parts = uri.getPath().split("/");
            if (parts.length < 2) {
                throw new IOException(RB.$("ERROR_git_repository_remote_url_parse", uri.getPath()));
            }

            String owner = parts[parts.length - 2];
            String name = parts[parts.length - 1].replace(".git", "");

            return new Repository(
                kind,
                owner,
                name,
                null,
                uri.toString());
        } catch (GitAPIException e) {
            throw new IOException(RB.$("ERROR_git_repository_origin_remote"), e);
        }
    }

    public List<String> getLocalBranchNames() throws IOException {
        Git git = open();

        try {
            return git.branchList()
                .call().stream()
                .map(GitSdk::extractHeadName)
                .filter(StringUtils::isNotBlank)
                .collect(toList());
        } catch (GitAPIException e) {
            throw new IOException(RB.$("ERROR_git_repository_list_local_branch"), e);
        }
    }

    public List<String> getRemoteBranches() throws IOException {
        Git git = open();

        try {
            return git.branchList()
                .setListMode(ListBranchCommand.ListMode.REMOTE)
                .call().stream()
                .map(GitSdk::extractRemoteName)
                .filter(StringUtils::isNotBlank)
                .collect(toList());
        } catch (GitAPIException e) {
            throw new IOException(RB.$("ERROR_git_repository_list_local_branch"), e);
        }
    }

    public Commit head() throws IOException {
        Git git = open();

        RevWalk walk = new RevWalk(git.getRepository());
        ObjectId head = git.getRepository().resolve(Constants.HEAD);
        RevCommit commit = walk.parseCommit(head);
        Ref ref = git.getRepository().findRef(Constants.HEAD);

        return new Commit(
            commit.getId().abbreviate(7).name(),
            commit.getId().name(),
            extractHeadName(ref));
    }

    public void deleteTag(String tagName) throws IOException {
        Git git = open();

        try {
            git.tagDelete()
                .setTags(tagName)
                .call();
        } catch (GitAPIException e) {
            throw new IOException(RB.$("ERROR_git_delete_tag", tagName), e);
        }
    }

    public boolean findTag(String tagName) throws IOException {
        Git git = open();

        try {
            return git.tagList().call().stream()
                .map(GitSdk::extractTagName)
                .anyMatch(tagName::matches);
        } catch (GitAPIException e) {
            throw new IOException(RB.$("ERROR_git_find_tag", tagName), e);
        }
    }

    public void tag(String tagName, JReleaserContext context) throws IOException {
        tag(tagName, false, context);
    }

    public void tag(String tagName, boolean force, JReleaserContext context) throws IOException {
        Git git = open();

        try {
            boolean signEnabled = context.getModel().getRelease().getGitService().isSign();
            git.tag()
                .setSigned(signEnabled)
                .setSigningKey("**********")
                .setGpgSigner(new JReleaserGpgSigner(context, signEnabled))
                .setName(tagName)
                .setForceUpdate(force)
                .call();
        } catch (GitAPIException e) {
            throw new IOException(RB.$("ERROR_git_create_tag", tagName), e);
        }
    }

    public static String resolveDefaultGitRemoteName() {
        String remoteName = Env.resolve("DEFAULT_GIT_REMOTE", "");
        if (isBlank(remoteName)) remoteName = "origin";
        return remoteName;
    }

    public static GitSdk of(JReleaserContext context) {
        return of(context.getBasedir().toFile(), context.isGitRootSearch());
    }

    public static GitSdk of(Path basedir, boolean gitRootSearch) {
        return of(basedir.toFile(), gitRootSearch);
    }

    public static GitSdk of(File basedir, boolean gitRootSearch) {
        return new GitSdk(basedir, gitRootSearch);
    }

    public static String extractTagName(Ref tag) {
        if (tag.getName().startsWith(REFS_TAGS)) {
            return tag.getName().substring(REFS_TAGS.length());
        }
        return "";
    }

    public static String extractHeadName(Ref ref) {
        if (ref.getTarget().getName().startsWith(REFS_HEADS)) {
            return ref.getTarget().getName().substring(REFS_HEADS.length());
        }
        return "";
    }

    public static String extractRemoteName(Ref ref) {
        if (ref.getTarget().getName().startsWith(REFS_REMOTES)) {
            String remoteAndBranch = ref.getTarget().getName().substring(REFS_REMOTES.length());
            String remoteName = resolveDefaultGitRemoteName();
            if (remoteAndBranch.startsWith(remoteName)) {
                return remoteAndBranch.substring(remoteName.length() + 1);
            }
        }
        return "";
    }

    public static class TagComparator implements Comparator<Ref> {
        @Override
        public int compare(Ref tag1, Ref tag2) {
            String tagName1 = extractTagName(tag1);
            String tagName2 = extractTagName(tag2);
            return tagName1.compareTo(tagName2);
        }
    }
}

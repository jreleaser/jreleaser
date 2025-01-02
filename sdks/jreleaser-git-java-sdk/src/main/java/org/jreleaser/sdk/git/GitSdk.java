/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2025 The JReleaser authors.
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
import org.eclipse.jgit.api.TagCommand;
import org.eclipse.jgit.api.errors.EmptyCommitException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.errors.RepositoryNotFoundException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevObject;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.transport.RemoteConfig;
import org.eclipse.jgit.transport.URIish;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.jreleaser.bundle.RB;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.internal.release.BaseReleaser;
import org.jreleaser.model.spi.release.Commit;
import org.jreleaser.model.spi.release.Repository;
import org.jreleaser.util.Env;
import org.jreleaser.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;

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

        while (null != dir) {
            try {
                return Git.open(dir);
            } catch (RepositoryNotFoundException e) {
                dir = dir.getParentFile();
            }
        }

        throw new RepositoryNotFoundException(basedir);
    }

    public Repository getRemote() throws IOException {
        try (Git git = open()) {
            String remote = resolveDefaultGitRemoteName();
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
                default:
                    // noop
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
        try (Git git = open()) {
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
        try (Git git = open()) {
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
        return commit(Constants.HEAD);
    }

    public Commit commit(String input) throws IOException {
        try (Git git = open()) {
            RevWalk walk = new RevWalk(git.getRepository());
            ObjectId objectId = git.getRepository().resolve(input);
            RevCommit commit = null;

            try {
                commit = walk.parseCommit(objectId);
            } catch (NullPointerException e) {
                throw new IllegalStateException(RB.$("ERROR_commit_not_found", input));
            }

            Ref ref = git.getRepository().findRef(input);
            PersonIdent authorIdent = commit.getAuthorIdent();
            Date authorDate = authorIdent.getWhen();
            TimeZone authorTimeZone = authorIdent.getTimeZone();

            ZoneId zoneId = ZoneId.of(authorTimeZone.getID());
            LocalDateTime local = LocalDateTime.ofInstant(authorDate.toInstant(), zoneId);
            ZonedDateTime zoned = ZonedDateTime.of(local, zoneId);

            return new Commit(
                commit.getId().abbreviate(7).name(),
                commit.getId().name(),
                extractHeadName(ref),
                commit.getCommitTime(),
                zoned);
        }
    }

    public RevCommit resolveSingleCommit(Git git, Ref tag) throws GitAPIException {
        try {
            Iterable<RevCommit> commits = git.log().add(getObjectId(git, tag))
                .setMaxCount(1)
                .call();
            if (null == commits) {
                throw new EmptyCommitException(RB.$("ERROR_git_commit_not_found", tag.getName()));
            }
            Iterator<RevCommit> iterator = commits.iterator();
            if (iterator.hasNext()) {
                return iterator.next();
            }
            throw new EmptyCommitException(RB.$("ERROR_git_commit_not_found", tag.getName()));
        } catch (IOException e) {
            throw new EmptyCommitException(RB.$("ERROR_git_commit_not_found", tag.getName()), e);
        }
    }

    public ObjectId getObjectId(Git git, Ref ref) throws IOException {
        Ref peeled = git.getRepository().getRefDatabase().peel(ref);
        return null != peeled.getPeeledObjectId() ? peeled.getPeeledObjectId() : peeled.getObjectId();
    }

    public void deleteTag(String tagName) throws IOException {
        try (Git git = open()) {
            git.tagDelete()
                .setTags(tagName)
                .call();
        } catch (GitAPIException e) {
            throw new IOException(RB.$("ERROR_git_delete_tag", tagName), e);
        }
    }

    public boolean findTag(String tagName) throws IOException {
        try (Git git = open()) {
            return git.tagList().call().stream()
                .map(GitSdk::extractTagName)
                .anyMatch(tagName::matches);
        } catch (GitAPIException e) {
            throw new IOException(RB.$("ERROR_git_find_tag", tagName), e);
        }
    }

    public void checkoutBranch(String branch) throws IOException {
        checkoutBranch(null, branch, true, false);
    }

    public void checkoutBranch(BaseReleaser<?, ?> releaser, String branch, boolean checkout, boolean create) throws IOException {
        try (Git git = open()) {
            if (checkout) {
                git.checkout()
                    .setName(branch)
                    .setCreateBranch(create)
                    .call();
            }

            if (create) {
                UsernamePasswordCredentialsProvider credentialsProvider = new UsernamePasswordCredentialsProvider(
                    releaser.getUsername(),
                    releaser.getToken());

                git.push()
                    .setDryRun(false)
                    .setPushAll()
                    .setCredentialsProvider(credentialsProvider)
                    .call();
            }
        } catch (GitAPIException e) {
            throw new IOException(RB.$("ERROR_git_checkout_branch", branch), e);
        }
    }

    public boolean isShallow() {
        Path path = basedir.toPath();
        return Files.exists(path.resolve(".git/shallow"));
    }

    public void tag(String tagName, JReleaserContext context) throws IOException {
        tag(tagName, false, context);
    }

    public void tag(String tagName, boolean force, JReleaserContext context) throws IOException {
        try (Git git = open()) {
            ObjectId objectId = git.getRepository().resolve(context.getModel().getCommit().getFullHash());
            boolean signEnabled = context.getModel().getRelease().getReleaser().isSign();
            TagCommand tagCommand = git.tag()
                .setSigned(signEnabled)
                .setSigningKey("**********")
                .setGpgSigner(new JReleaserGpgSigner(context, signEnabled))
                .setName(tagName)
                .setForceUpdate(force);
            if (objectId instanceof RevObject) {
                tagCommand = tagCommand.setObjectId((RevObject) objectId);
            }
            tagCommand.call();
        } catch (GitAPIException e) {
            throw new IOException(RB.$("ERROR_git_create_tag", tagName), e);
        }
    }

    public static String resolveDefaultGitRemoteName() {
        String remoteName = Env.env(org.jreleaser.model.Constants.DEFAULT_GIT_REMOTE, "");
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

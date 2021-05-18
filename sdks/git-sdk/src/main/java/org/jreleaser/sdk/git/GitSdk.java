/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2021 Andres Almiray.
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
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.transport.RemoteConfig;
import org.eclipse.jgit.transport.URIish;
import org.jreleaser.model.JReleaserContext;
import org.jreleaser.model.releaser.spi.Commit;
import org.jreleaser.model.releaser.spi.Repository;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class GitSdk {
    public static final String REFS_TAGS = "refs/tags/";
    public static final String REFS_HEADS = "refs/heads/";

    private final File basedir;

    private GitSdk(File basedir) {
        this.basedir = basedir;
    }

    public Git open() throws IOException {
        return Git.open(basedir);
    }

    public Repository getRemote() throws IOException {
        Git git = open();

        try {
            RemoteConfig remoteConfig = git.remoteList().call().stream()
                .filter(rc -> "origin".equals(rc.getName()))
                .findFirst()
                .orElseThrow(() -> new IOException("repository doesn't have an 'origin' remote"));

            List<URIish> uris = remoteConfig.getURIs();
            if (uris.isEmpty()) {
                // better be safe than sorry
                throw new IOException("'origin' remote does not have a configured URL");
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
                throw new IOException("Unparseable remote URL " + uri.getPath());
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
            throw new IOException("Could not determine 'origin' remote", e);
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
            throw new IOException("Could not delete tag " + tagName, e);
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
            throw new IOException("Could not create tag " + tagName, e);
        }
    }

    public static GitSdk of(JReleaserContext context) {
        return new GitSdk(context.getBasedir().toFile());
    }

    public static GitSdk of(Path basedir) {
        return new GitSdk(basedir.toFile());
    }

    public static GitSdk of(File basedir) {
        return new GitSdk(basedir);
    }

    public static Commit head(Path basedir) throws IOException {
        Git git = Git.open(basedir.toFile());

        RevWalk walk = new RevWalk(git.getRepository());
        ObjectId head = git.getRepository().resolve(Constants.HEAD);
        RevCommit commit = walk.parseCommit(head);
        Ref ref = git.getRepository().findRef(Constants.HEAD);

        return new Commit(
            commit.getId().abbreviate(7).name(),
            commit.getId().name(),
            extractHeadName(ref));
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

    public static class TagComparator implements Comparator<Ref> {
        @Override
        public int compare(Ref tag1, Ref tag2) {
            String tagName1 = extractTagName(tag1);
            String tagName2 = extractTagName(tag2);
            return tagName1.compareTo(tagName2);
        }
    }
}

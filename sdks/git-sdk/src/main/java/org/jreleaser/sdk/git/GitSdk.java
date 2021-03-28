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
import org.jreleaser.model.JReleaserContext;
import org.jreleaser.model.releaser.spi.Commit;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Comparator;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class GitSdk {
    public static final String REFS_TAGS = "refs/tags/";

    private final JReleaserContext context;

    private GitSdk(JReleaserContext context) {
        this.context = context;
    }

    public Git open() throws IOException {
        return Git.open(context.getBasedir().toFile());
    }

    public Commit head() throws IOException {
        Git git = open();

        RevWalk walk = new RevWalk(git.getRepository());
        ObjectId head = git.getRepository().resolve(Constants.HEAD);
        RevCommit commit = walk.parseCommit(head);

        return new Commit(
            commit.getId().abbreviate(7).name(),
            commit.getId().name());
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

    public void tag(String tagName) throws IOException {
        tag(tagName, false);
    }

    public void tag(String tagName, boolean force) throws IOException {
        Git git = open();

        try {
            git.tag()
                .setName(tagName)
                .setForceUpdate(force)
                .call();
        } catch (GitAPIException e) {
            throw new IOException("Could not create tag " + tagName, e);
        }
    }

    public static GitSdk of(JReleaserContext context) {
        return new GitSdk(context);
    }

    public static Commit head(Path basedir) throws IOException {
        Git git = Git.open(basedir.toFile());

        RevWalk walk = new RevWalk(git.getRepository());
        ObjectId head = git.getRepository().resolve(Constants.HEAD);
        RevCommit commit = walk.parseCommit(head);

        return new Commit(
            commit.getId().abbreviate(7).name(),
            commit.getId().name());
    }

    public static String extractTagName(Ref tag) {
        if (tag.getName().startsWith(REFS_TAGS)) {
            return tag.getName().substring(REFS_TAGS.length());
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

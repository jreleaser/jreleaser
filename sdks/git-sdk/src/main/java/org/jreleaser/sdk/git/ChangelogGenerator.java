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
import org.jreleaser.model.Changelog;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class ChangelogGenerator {
    private static final String REFS_TAGS = "refs/tags/";

    public static String generate(Path basedir, Changelog changelog) throws IOException {
        if (!changelog.isEnabled()) {
            return "";
        }

        if (null != changelog.getExternal() && changelog.getExternal().exists()) {
            return readChangelogFile(changelog.getExternal());
        }

        return createChangelog(basedir);
    }

    private static String readChangelogFile(File file) throws IOException {
        return "## Changelog" +
            System.lineSeparator() +
            System.lineSeparator() +
            String.join(System.lineSeparator(), Files.readAllLines(file.toPath()));
    }

    private static String createChangelog(Path basedir) throws IOException {
        try {
            Git git = Git.open(basedir.toFile());
            Iterable<RevCommit> commits = resolveCommits(git);

            return "## Changelog" +
                System.lineSeparator() +
                System.lineSeparator() +
                StreamSupport.stream(commits.spliterator(), false)
                    .map(ChangelogGenerator::formatCommit)
                    .collect(Collectors.joining(System.lineSeparator()));
        } catch (GitAPIException e) {
            throw new IOException(e);
        }
    }

    private static String formatCommit(RevCommit commit) {
        String abbreviation = commit.getTree().getId().abbreviate(8).name();
        String[] input = commit.getFullMessage().trim().split(System.lineSeparator());

        List<String> lines = new ArrayList<>();
        for (int i = 0; i < input.length; i++) {
            if (i == 0) {
                lines.add(abbreviation + " " + input[i].trim());
            } else if (isNotBlank(input[i])) {
                lines.add("         " + input[i].trim());
            }
        }

        return String.join(System.lineSeparator(), lines);
    }

    private static Iterable<RevCommit> resolveCommits(Git git) throws GitAPIException, IOException {
        List<Ref> tags = git.tagList().call();
        tags.sort(new TagComparator().reversed());

        Optional<Ref> tag = tags.stream()
            .filter(ref -> extractTagName(ref).startsWith("v"))
            .findFirst();

        ObjectId head = git.getRepository().resolve(Constants.HEAD);
        if (tag.isPresent()) {
            Ref peeled = git.getRepository().getRefDatabase().peel(tag.get());
            ObjectId fromRef = peeled.getPeeledObjectId() != null ? peeled.getPeeledObjectId() : peeled.getObjectId();
            return git.log().addRange(fromRef, head).call();
        }

        return git.log().add(head).call();
    }

    private static String extractTagName(Ref tag) {
        if (tag.getName().startsWith(REFS_TAGS)) {
            return tag.getName().substring(REFS_TAGS.length());
        }
        return "";
    }

    private static class TagComparator implements Comparator<Ref> {
        @Override
        public int compare(Ref tag1, Ref tag2) {
            String tagName1 = extractTagName(tag1);
            String tagName2 = extractTagName(tag2);
            return tagName1.compareTo(tagName2);
        }
    }
}

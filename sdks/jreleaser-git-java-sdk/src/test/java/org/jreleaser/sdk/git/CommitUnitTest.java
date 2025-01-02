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

import org.eclipse.jgit.lib.AbbreviatedObjectId;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.revwalk.RevCommit;
import org.jreleaser.logging.JReleaserLogger;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.internal.JReleaserModel;
import org.jreleaser.model.internal.release.BaseReleaser;
import org.jreleaser.model.internal.release.Changelog;
import org.jreleaser.model.internal.release.Release;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.jreleaser.util.StringUtils.isNotBlank;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CommitUnitTest {
    private final JReleaserContext defaultContext = mockContext("", "repo", "");

    private JReleaserContext mockContext(String issueTracker, String repo, String owner) {
        JReleaserContext context = mock(JReleaserContext.class);
        JReleaserLogger logger = mock(JReleaserLogger.class);
        JReleaserModel model = mock(JReleaserModel.class);
        Release release = mock(Release.class);
        BaseReleaser releaser = mock(BaseReleaser.class);

        when(context.getLogger()).thenReturn(logger);
        when(context.getModel()).thenReturn(model);
        when(model.getRelease()).thenReturn(release);
        when(release.getReleaser()).thenReturn(releaser);
        when(releaser.getResolvedIssueTrackerUrl(any(), anyBoolean())).thenReturn(issueTracker);
        when(releaser.getCanonicalRepoName()).thenReturn(isNotBlank(owner) ? owner + "/" + repo : repo);
        when(releaser.getName()).thenReturn(repo);

        return context;
    }

    private ChangelogGenerator.Commit mockCommit(String commitBody) {
        RevCommit revCommit = mock(RevCommit.class);
        ObjectId objectId = mock(ObjectId.class);
        AbbreviatedObjectId abbreviatedObjectId = mock(AbbreviatedObjectId.class);
        PersonIdent committer = mock(PersonIdent.class);
        PersonIdent author = mock(PersonIdent.class);
        Changelog changelog = mock(Changelog.class);
        int time = 123456;

        when(revCommit.getId()).thenReturn(objectId);
        when(objectId.name()).thenReturn("full-hash");
        when(objectId.abbreviate(7)).thenReturn(abbreviatedObjectId);
        when(abbreviatedObjectId.name()).thenReturn("short-hash");
        when(revCommit.getFullMessage()).thenReturn(commitBody);
        when(revCommit.getCommitterIdent()).thenReturn(committer);
        when(committer.getName()).thenReturn("committer-name");
        when(committer.getEmailAddress()).thenReturn("committer@example.com");
        when(revCommit.getAuthorIdent()).thenReturn(author);
        when(author.getName()).thenReturn("author-name");
        when(author.getEmailAddress()).thenReturn("author@example.com");
        when(revCommit.getCommitTime()).thenReturn(time);
        when(changelog.getPreset()).thenReturn("conventional-commits");

        return ChangelogGenerator.Commit.of(revCommit);
    }

    @Test
    void closeFooterWithHash() {
        String commitBody = "feat(scope): add new feature\n" +
            "\n" +
            "BREAKING CHANGE: single line breaking change\n" +
            "Reviewed-by: Z\n" +
            "Closes #42";

        ChangelogGenerator.Commit c = mockCommit(commitBody).extractIssues(defaultContext);

        assertThat(c.getIssues())
            .hasSize(1)
            .containsExactlyInAnyOrder(42);
    }

    @Test
    void refsFooterWithDotsAndHash() {
        String commitBody = "fix: prevent racing of requests\n" +
            "\n" +
            "Introduce a request id and a reference to latest request. Dismiss\n" +
            "incoming responses other than from latest request.\n" +
            "\n" +
            "Remove timeouts which were used to mitigate the racing issue but are\n" +
            "obsolete now.\n" +
            "\n" +
            "Reviewed-by: Z\n" +
            "Refs: #123";

        ChangelogGenerator.Commit c = mockCommit(commitBody).extractIssues(defaultContext);

        assertThat(c.getIssues())
            .hasSize(1)
            .containsExactlyInAnyOrder(123);
    }

    @Test
    void multipleHashes() {
        String commitBody = "a classic commit that fixes #46 (#47)";

        ChangelogGenerator.Commit c = mockCommit(commitBody).extractIssues(defaultContext);

        assertThat(c.getIssues())
            .hasSize(2)
            .containsExactlyInAnyOrder(46, 47);
    }

    @Test
    void multipleHashes2() {
        String commitBody = "a classic commit\n" +
            "Closes: #1, #2, #3";

        ChangelogGenerator.Commit c = mockCommit(commitBody).extractIssues(defaultContext);

        assertThat(c.getIssues())
            .hasSize(3)
            .containsExactlyInAnyOrder(1, 2, 3);
    }

    @Test
    void issueTracker() {
        String commitBody = "a classic commit\n" +
            "Closes: https://github.com/owner/repo/issues/1, https://github.com/owner/repo/issues/2, #3";

        JReleaserContext context = mockContext("https://github.com/owner/repo/issues/", "repo", "");

        ChangelogGenerator.Commit c = mockCommit(commitBody).extractIssues(context);

        assertThat(c.getIssues())
            .hasSize(3)
            .containsExactlyInAnyOrder(1, 2, 3);
    }

    @Test
    void repoOnly() {
        String commitBody = "a classic commit\n" +
            "#1, repo#2, other#3";

        ChangelogGenerator.Commit c = mockCommit(commitBody).extractIssues(defaultContext);

        assertThat(c.getIssues())
            .hasSize(2)
            .containsExactlyInAnyOrder(1, 2);
    }

    @Test
    void repoWithOwner() {
        String commitBody = "a classic commit\n" +
            "Closes: someone/repo#1, owner/repo#2, repo#3, (#4)";

        JReleaserContext context = mockContext("", "repo", "owner");

        ChangelogGenerator.Commit c = mockCommit(commitBody).extractIssues(context);

        assertThat(c.getIssues())
            .hasSize(3)
            .containsExactlyInAnyOrder(2, 3, 4);
    }

    @Test
    void otherNumbers() {
        String commitBody = "upgrade SQLite to 3.40.0\n" +
            "previously log() computed the natural logarithm, now it computes a base-10 logarithm";

        JReleaserContext context = mockContext("https://github.com/owner/repo/issues/", "repo", "");

        ChangelogGenerator.Commit c = mockCommit(commitBody).extractIssues(context);

        assertThat(c.getIssues()).isEmpty();
    }
}

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
import org.jreleaser.model.internal.release.Changelog;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ConventionalCommitUnitTest {
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

        return ChangelogGenerator.ConventionalCommit.of(revCommit);
    }

    @Test
    void badlyFormattedCommitIsNotConventional() {
        String commitBody = "featadd new feature";

        ChangelogGenerator.Commit c = mockCommit(commitBody);

        assertThat(c)
            .isInstanceOf(ChangelogGenerator.Commit.class)
            .isNotInstanceOf(ChangelogGenerator.ConventionalCommit.class);
    }

    @Test
    void typeCanBeAnyWord() {
        String commitBody = "whatever: correct spelling of CHANGELOG";

        ChangelogGenerator.Commit c = mockCommit(commitBody);

        assertThat(c)
            .hasFieldOrPropertyWithValue("isConventional", true)
            .hasFieldOrPropertyWithValue("ccIsBreakingChange", false)
            .hasFieldOrPropertyWithValue("ccBreakingChangeContent", "")
            .hasFieldOrPropertyWithValue("ccType", "whatever")
            .hasFieldOrPropertyWithValue("ccScope", "")
            .hasFieldOrPropertyWithValue("ccDescription", "correct spelling of CHANGELOG")
            .hasFieldOrPropertyWithValue("ccBody", "");
        assertThat(((ChangelogGenerator.ConventionalCommit) c).getTrailers()).isEmpty();
    }

    @Test
    void typeCanBeHaveNumbers() {
        String commitBody = "i18n: correct spelling of CHANGELOG";

        ChangelogGenerator.Commit c = mockCommit(commitBody);

        assertThat(c)
            .hasFieldOrPropertyWithValue("isConventional", true)
            .hasFieldOrPropertyWithValue("ccIsBreakingChange", false)
            .hasFieldOrPropertyWithValue("ccBreakingChangeContent", "")
            .hasFieldOrPropertyWithValue("ccType", "i18n")
            .hasFieldOrPropertyWithValue("ccScope", "")
            .hasFieldOrPropertyWithValue("ccDescription", "correct spelling of CHANGELOG")
            .hasFieldOrPropertyWithValue("ccBody", "");
        assertThat(((ChangelogGenerator.ConventionalCommit) c).getTrailers()).isEmpty();
    }

    @Test
    void bangIsBreakingChange() {
        String commitBody = "feat(scope)!: add new feature";

        ChangelogGenerator.Commit c = mockCommit(commitBody);

        assertThat(c)
            .hasFieldOrPropertyWithValue("isConventional", true)
            .hasFieldOrPropertyWithValue("ccIsBreakingChange", true)
            .hasFieldOrPropertyWithValue("ccBreakingChangeContent", "")
            .hasFieldOrPropertyWithValue("ccType", "feat")
            .hasFieldOrPropertyWithValue("ccScope", "scope")
            .hasFieldOrPropertyWithValue("ccDescription", "add new feature")
            .hasFieldOrPropertyWithValue("ccBody", "");
        assertThat(((ChangelogGenerator.ConventionalCommit) c).getTrailers()).isEmpty();
    }

    @Test
    void singleLineBreakingChange() {
        String commitBody = "feat(scope): add new feature\n" +
            "\n" +
            "BREAKING CHANGE: single line breaking change";

        ChangelogGenerator.Commit c = mockCommit(commitBody);

        assertThat(c)
            .hasFieldOrPropertyWithValue("isConventional", true)
            .hasFieldOrPropertyWithValue("ccIsBreakingChange", true)
            .hasFieldOrPropertyWithValue("ccBreakingChangeContent", "single line breaking change")
            .hasFieldOrPropertyWithValue("ccType", "feat")
            .hasFieldOrPropertyWithValue("ccScope", "scope")
            .hasFieldOrPropertyWithValue("ccDescription", "add new feature")
            .hasFieldOrPropertyWithValue("ccBody", "");
        assertThat(((ChangelogGenerator.ConventionalCommit) c).getTrailers()).isEmpty();
    }

    @Test
    void singleLineBreakingChangeWithDash() {
        String commitBody = "feat(scope): add new feature\n" +
            "\n" +
            "BREAKING-CHANGE: single line breaking change";

        ChangelogGenerator.Commit c = mockCommit(commitBody);

        assertThat(c)
            .hasFieldOrPropertyWithValue("isConventional", true)
            .hasFieldOrPropertyWithValue("ccIsBreakingChange", true)
            .hasFieldOrPropertyWithValue("ccBreakingChangeContent", "single line breaking change")
            .hasFieldOrPropertyWithValue("ccType", "feat")
            .hasFieldOrPropertyWithValue("ccScope", "scope")
            .hasFieldOrPropertyWithValue("ccDescription", "add new feature")
            .hasFieldOrPropertyWithValue("ccBody", "");
        assertThat(((ChangelogGenerator.ConventionalCommit) c).getTrailers()).isEmpty();
    }

    @Test
    void singleLineBreakingChangeLowerCase() {
        String commitBody = "feat(scope): add new feature\n" +
            "\n" +
            "breaking CHANGE: single line breaking change";

        ChangelogGenerator.Commit c = mockCommit(commitBody);

        assertThat(c)
            .hasFieldOrPropertyWithValue("isConventional", true)
            .hasFieldOrPropertyWithValue("ccIsBreakingChange", false)
            .hasFieldOrPropertyWithValue("ccBreakingChangeContent", "")
            .hasFieldOrPropertyWithValue("ccType", "feat")
            .hasFieldOrPropertyWithValue("ccScope", "scope")
            .hasFieldOrPropertyWithValue("ccDescription", "add new feature")
            .hasFieldOrPropertyWithValue("ccBody", "breaking CHANGE: single line breaking change");
        assertThat(((ChangelogGenerator.ConventionalCommit) c).getTrailers()).isEmpty();
    }

    @Test
    void multiLineBreakingChange() {
        String commitBody = "feat(scope): add new feature\n" +
            "\n" +
            "BREAKING CHANGE: multi line\n" +
            "breaking change";

        ChangelogGenerator.Commit c = mockCommit(commitBody);

        assertThat(c)
            .hasFieldOrPropertyWithValue("isConventional", true)
            .hasFieldOrPropertyWithValue("ccIsBreakingChange", true)
            .hasFieldOrPropertyWithValue("ccBreakingChangeContent", "multi line\nbreaking change")
            .hasFieldOrPropertyWithValue("ccType", "feat")
            .hasFieldOrPropertyWithValue("ccScope", "scope")
            .hasFieldOrPropertyWithValue("ccDescription", "add new feature")
            .hasFieldOrPropertyWithValue("ccBody", "");
        assertThat(((ChangelogGenerator.ConventionalCommit) c).getTrailers()).isEmpty();
    }

    @Test
    void singleLineBreakingChangeFollowedByGitTrailer() {
        String commitBody = "feat(scope): add new feature\n" +
            "\n" +
            "BREAKING CHANGE: single line breaking change\n" +
            "Reviewed-by: Z\n" +
            "Closes #42";

        ChangelogGenerator.Commit c = mockCommit(commitBody);

        assertThat(c)
            .hasFieldOrPropertyWithValue("isConventional", true)
            .hasFieldOrPropertyWithValue("ccIsBreakingChange", true)
            .hasFieldOrPropertyWithValue("ccBreakingChangeContent", "single line breaking change");
        assertThat(((ChangelogGenerator.ConventionalCommit) c).getTrailers())
            .hasSize(2)
            .containsExactlyInAnyOrder(new ChangelogGenerator.ConventionalCommit.Trailer("Reviewed-by", "Z"), new ChangelogGenerator.ConventionalCommit.Trailer("Closes", "42"));
    }

    @Test
    void scopeCanHaveDash() {
        String commitBody = "build(deps-dev): bump mockito-core from 4.8.1 to 5.2.0";

        ChangelogGenerator.Commit c = mockCommit(commitBody);

        assertThat(c)
            .hasFieldOrPropertyWithValue("isConventional", true)
            .hasFieldOrPropertyWithValue("ccIsBreakingChange", false)
            .hasFieldOrPropertyWithValue("ccBreakingChangeContent", "")
            .hasFieldOrPropertyWithValue("ccType", "build")
            .hasFieldOrPropertyWithValue("ccScope", "deps-dev")
            .hasFieldOrPropertyWithValue("ccDescription", "bump mockito-core from 4.8.1 to 5.2.0")
            .hasFieldOrPropertyWithValue("ccBody", "");
        assertThat(((ChangelogGenerator.ConventionalCommit) c).getTrailers()).isEmpty();
    }

    @Test
    void scopeCanHaveOtherCharacters() {
        String commitBody = "build(@scope/pkg-name): javascript style scope";

        ChangelogGenerator.Commit c = mockCommit(commitBody);

        assertThat(c)
            .hasFieldOrPropertyWithValue("isConventional", true)
            .hasFieldOrPropertyWithValue("ccIsBreakingChange", false)
            .hasFieldOrPropertyWithValue("ccBreakingChangeContent", "")
            .hasFieldOrPropertyWithValue("ccType", "build")
            .hasFieldOrPropertyWithValue("ccScope", "@scope/pkg-name")
            .hasFieldOrPropertyWithValue("ccDescription", "javascript style scope")
            .hasFieldOrPropertyWithValue("ccBody", "");
        assertThat(((ChangelogGenerator.ConventionalCommit) c).getTrailers()).isEmpty();
    }

    @Test
    void ccExample1() {
        String commitBody = "feat: allow provided config object to extend other configs\n" +
            "\n" +
            "BREAKING CHANGE: `extends` key in config file is now used for extending other config files";

        ChangelogGenerator.Commit c = mockCommit(commitBody);

        assertThat(c)
            .hasFieldOrPropertyWithValue("isConventional", true)
            .hasFieldOrPropertyWithValue("ccIsBreakingChange", true)
            .hasFieldOrPropertyWithValue("ccBreakingChangeContent", "`extends` key in config file is now used for extending other config files")
            .hasFieldOrPropertyWithValue("ccType", "feat")
            .hasFieldOrPropertyWithValue("ccScope", "")
            .hasFieldOrPropertyWithValue("ccDescription", "allow provided config object to extend other configs")
            .hasFieldOrPropertyWithValue("ccBody", "");
        assertThat(((ChangelogGenerator.ConventionalCommit) c).getTrailers()).isEmpty();
    }

    @Test
    void ccExample2() {
        String commitBody = "feat!: send an email to the customer when a product is shipped";

        ChangelogGenerator.Commit c = mockCommit(commitBody);

        assertThat(c)
            .hasFieldOrPropertyWithValue("isConventional", true)
            .hasFieldOrPropertyWithValue("ccIsBreakingChange", true)
            .hasFieldOrPropertyWithValue("ccBreakingChangeContent", "")
            .hasFieldOrPropertyWithValue("ccType", "feat")
            .hasFieldOrPropertyWithValue("ccScope", "")
            .hasFieldOrPropertyWithValue("ccDescription", "send an email to the customer when a product is shipped")
            .hasFieldOrPropertyWithValue("ccBody", "");
        assertThat(((ChangelogGenerator.ConventionalCommit) c).getTrailers()).isEmpty();
    }

    @Test
    void ccExample3() {
        String commitBody = "feat(api)!: send an email to the customer when a product is shipped";

        ChangelogGenerator.Commit c = mockCommit(commitBody);

        assertThat(c)
            .hasFieldOrPropertyWithValue("isConventional", true)
            .hasFieldOrPropertyWithValue("ccIsBreakingChange", true)
            .hasFieldOrPropertyWithValue("ccBreakingChangeContent", "")
            .hasFieldOrPropertyWithValue("ccType", "feat")
            .hasFieldOrPropertyWithValue("ccScope", "api")
            .hasFieldOrPropertyWithValue("ccDescription", "send an email to the customer when a product is shipped")
            .hasFieldOrPropertyWithValue("ccBody", "");
        assertThat(((ChangelogGenerator.ConventionalCommit) c).getTrailers()).isEmpty();

        assertThat(c.asContext(false, "", "").asMap())
            .containsEntry("commitIsConventional", true)
            .containsEntry("conventionalCommitBreakingChangeContent", "")
            .containsEntry("conventionalCommitIsBreakingChange", true)
            .containsEntry("conventionalCommitType", "feat")
            .containsEntry("conventionalCommitScope", "api")
            .containsEntry("conventionalCommitDescription", "!!send an email to the customer when a product is shipped!!")
            .containsEntry("conventionalCommitBody", "");
    }

    @Test
    void ccExample4() {
        String commitBody = "chore!: drop support for Node 6\n" +
            "\n" +
            "BREAKING CHANGE: use JavaScript features not available in Node 6.";

        ChangelogGenerator.Commit c = mockCommit(commitBody);

        assertThat(c)
            .hasFieldOrPropertyWithValue("isConventional", true)
            .hasFieldOrPropertyWithValue("ccIsBreakingChange", true)
            .hasFieldOrPropertyWithValue("ccBreakingChangeContent", "use JavaScript features not available in Node 6.")
            .hasFieldOrPropertyWithValue("ccType", "chore")
            .hasFieldOrPropertyWithValue("ccScope", "")
            .hasFieldOrPropertyWithValue("ccDescription", "drop support for Node 6")
            .hasFieldOrPropertyWithValue("ccBody", "");
        assertThat(((ChangelogGenerator.ConventionalCommit) c).getTrailers()).isEmpty();

        assertThat(c.asContext(false, "", "").asMap())
            .containsEntry("commitIsConventional", true)
            .containsEntry("conventionalCommitBreakingChangeContent", "!!use JavaScript features not available in Node 6.!!")
            .containsEntry("conventionalCommitIsBreakingChange", true)
            .containsEntry("conventionalCommitType", "chore")
            .containsEntry("conventionalCommitScope", "")
            .containsEntry("conventionalCommitDescription", "!!drop support for Node 6!!")
            .containsEntry("conventionalCommitBody", "");
    }

    @Test
    void ccExample5() {
        String commitBody = "docs: correct spelling of CHANGELOG";

        ChangelogGenerator.Commit c = mockCommit(commitBody);

        assertThat(c)
            .hasFieldOrPropertyWithValue("isConventional", true)
            .hasFieldOrPropertyWithValue("ccIsBreakingChange", false)
            .hasFieldOrPropertyWithValue("ccBreakingChangeContent", "")
            .hasFieldOrPropertyWithValue("ccType", "docs")
            .hasFieldOrPropertyWithValue("ccScope", "")
            .hasFieldOrPropertyWithValue("ccDescription", "correct spelling of CHANGELOG")
            .hasFieldOrPropertyWithValue("ccBody", "");
        assertThat(((ChangelogGenerator.ConventionalCommit) c).getTrailers()).isEmpty();
    }

    @Test
    void ccExample6() {
        String commitBody = "feat(lang): add Polish language";

        ChangelogGenerator.Commit c = mockCommit(commitBody);

        assertThat(c)
            .hasFieldOrPropertyWithValue("isConventional", true)
            .hasFieldOrPropertyWithValue("ccIsBreakingChange", false)
            .hasFieldOrPropertyWithValue("ccBreakingChangeContent", "")
            .hasFieldOrPropertyWithValue("ccType", "feat")
            .hasFieldOrPropertyWithValue("ccScope", "lang")
            .hasFieldOrPropertyWithValue("ccDescription", "add Polish language")
            .hasFieldOrPropertyWithValue("ccBody", "");
        assertThat(((ChangelogGenerator.ConventionalCommit) c).getTrailers()).isEmpty();
    }

    @Test
    void ccExample7() {
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

        ChangelogGenerator.Commit c = mockCommit(commitBody);

        assertThat(c)
            .hasFieldOrPropertyWithValue("isConventional", true)
            .hasFieldOrPropertyWithValue("ccIsBreakingChange", false)
            .hasFieldOrPropertyWithValue("ccBreakingChangeContent", "")
            .hasFieldOrPropertyWithValue("ccType", "fix")
            .hasFieldOrPropertyWithValue("ccScope", "")
            .hasFieldOrPropertyWithValue("ccDescription", "prevent racing of requests")
            .hasFieldOrPropertyWithValue("ccBody", "Introduce a request id and a reference to latest request. Dismiss\n" +
                "incoming responses other than from latest request.\n" +
                "\n" +
                "Remove timeouts which were used to mitigate the racing issue but are\n" +
                "obsolete now.");
        assertThat(((ChangelogGenerator.ConventionalCommit) c).getTrailers())
            .hasSize(2)
            .containsExactlyInAnyOrder(new ChangelogGenerator.ConventionalCommit.Trailer("Reviewed-by", "Z"), new ChangelogGenerator.ConventionalCommit.Trailer("Refs", "#123"));
    }
}

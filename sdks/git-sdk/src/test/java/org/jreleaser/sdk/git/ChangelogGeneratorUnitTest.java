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
import org.eclipse.jgit.api.ListTagCommand;
import org.eclipse.jgit.api.LogCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectIdRef;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevCommit;
import org.jreleaser.model.Changelog;
import org.jreleaser.model.GitService;
import org.jreleaser.model.JReleaserContext;
import org.jreleaser.model.JReleaserModel;
import org.jreleaser.model.Project;
import org.jreleaser.model.Release;
import org.jreleaser.model.VersionPattern;
import org.jreleaser.util.SemVer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ChangelogGeneratorUnitTest {
    @Spy
    ChangelogGenerator changelogGenerator = new ChangelogGenerator();
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private Git git;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private JReleaserContext context;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    GitService gitService;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    Changelog changelog;

    private MockedStatic<GitSdk> gitSdkMockedStatic;

    private MockedStatic<ChangelogGenerator.Commit> commitMockedStatic;


    void cleanUpStaticMocks() {
        gitSdkMockedStatic.close();
        commitMockedStatic.close();
    }

    void setUpStaticMocks() {
        gitSdkMockedStatic = Mockito.mockStatic(GitSdk.class);
        commitMockedStatic = Mockito.mockStatic(ChangelogGenerator.Commit.class);
    }

    @Test
    @DisplayName("When configured tag has no prefix and no matches if found then all commits from head must be used")
    public void notParsable() throws GitAPIException, IOException {
        // given:
        String effectiveTagName = "2.2.0";
        String configuredTagName = "{{projectVersion}}";
        ObjectId headId = ObjectId.fromString("085bb3bcb608e1e8451d4b2432f8ecbe6306e7e7");
        boolean isSnapshot = false;
        List<Ref> tagRefs = buildMockedTagRefs(
            new String[]{"refs/tags/v1.0.0", "cac0cab538b970a37ea1e769cbbde608743bc96d"},
            new String[]{"refs/tags/v2.0.0", "a11bef06a3f659402fe7563abf99ad00de2209e6"});

        LogCommand logCommand = doNecessaryMock(effectiveTagName, configuredTagName, headId, isSnapshot, tagRefs);

        // when:
        changelogGenerator.resolveCommits(git, context);

        // then:
        verify(logCommand).add(headId);
    }

    @Test
    @DisplayName("When no tag is found that match current configured tag name then all commits from head must be used")
    public void tagThatNoMatches() throws GitAPIException, IOException {
        // given:
        String effectiveTagName = "2.2.0";
        String configuredTagName = "release-{{projectVersion}}";
        ObjectId headId = ObjectId.fromString("085bb3bcb608e1e8451d4b2432f8ecbe6306e7e7");
        boolean isSnapshot = false;
        List<Ref> tagRefs = buildMockedTagRefs(
            new String[]{"refs/tags/v1.0.0", "cac0cab538b970a37ea1e769cbbde608743bc96d"},
            new String[]{"refs/tags/v2.0.0", "a11bef06a3f659402fe7563abf99ad00de2209e6"});

        LogCommand logCommand = doNecessaryMock(effectiveTagName, configuredTagName, headId, isSnapshot, tagRefs);

        // when:
        changelogGenerator.resolveCommits(git, context);

        // then:
        verify(logCommand).add(headId);
    }

    @Test
    @DisplayName("When skipMergeCommits property is true and formatted enabled skip merge commits in the changelog")
    public void skipMergeCommitsFormatted() throws GitAPIException, IOException {
        // given:
        setUpStaticMocks();
        RevCommit mergeCommit = getMockRevCommit(true, true);

        // when:
        changelogGenerator.formatChangelog(context, changelog, Collections.singletonList(mergeCommit), Comparator.comparing(RevCommit::getCommitTime), "");

        // then:
        verify(changelogGenerator, times(0)).categorize(any(), any());

        cleanUpStaticMocks();
    }

    @Test
    @DisplayName("When skipMergeCommits property is false and formatted enabled keep merge commits in the changelog")
    public void keepMergeCommitsFormatted() throws GitAPIException, IOException {
        // given:
        setUpStaticMocks();
        RevCommit mergeCommit = getMockRevCommit(false, true);

        // when:
        changelogGenerator.formatChangelog(context, changelog, Collections.singletonList(mergeCommit), Comparator.comparing(RevCommit::getCommitTime), "");

        // then:
        verify(changelogGenerator, times(1)).categorize(any(), any());

        cleanUpStaticMocks();
    }

    @Test
    @DisplayName("When skipMergeCommits property is true and formatted disabled skip merge commits in the changelog")
    public void skipMergeCommits() throws GitAPIException, IOException {
        // given:
        setUpStaticMocks();
        RevCommit mergeCommit = getMockRevCommit(true, false);
        when(gitService.getChangelog()).thenReturn(changelog);
        Mockito.doReturn(Collections.singletonList(mergeCommit)).when(changelogGenerator).resolveCommits(git, context);

        // when:
        changelogGenerator.createChangelog(context);

        // then:
        verify(changelogGenerator, times(0)).formatCommit(any(), any(), any(), any());

        cleanUpStaticMocks();
    }

    @Test
    @DisplayName("When skipMergeCommits property is false and formatted disabled keep merge commits in the changelog")
    public void keepMergeCommits() throws GitAPIException, IOException {
        // given:
        setUpStaticMocks();
        RevCommit mergeCommit = getMockRevCommit(false, false);
        when(gitService.getChangelog()).thenReturn(changelog);
        Mockito.doReturn(Collections.singletonList(mergeCommit)).when(changelogGenerator).resolveCommits(git, context);

        // when:
        changelogGenerator.createChangelog(context);

        // then:
        verify(changelogGenerator, times(1)).formatCommit(any(), any(), any(), any());

        cleanUpStaticMocks();
    }

    private RevCommit getMockRevCommit(boolean skipMergeCommits, boolean formatted) throws GitAPIException, IOException {
        String effectiveTagName = "2.2.0";
        String configuredTagName = "{{projectVersion}}";
        ObjectId headId = ObjectId.fromString("085bb3bcb608e1e8451d4b2432f8ecbe6306e7e7");
        boolean isSnapshot = false;
        List<Ref> tagRefs = buildMockedTagRefs(
          new String[]{"refs/tags/v1.0.0", "cac0cab538b970a37ea1e769cbbde608743bc96d"},
          new String[]{"refs/tags/v2.0.0", "a11bef06a3f659402fe7563abf99ad00de2209e6"});

        doNecessaryMock(effectiveTagName, configuredTagName, headId, isSnapshot, tagRefs);

        RevCommit mergeCommit = mock(RevCommit.class);
        when(mergeCommit.getParentCount()).thenReturn(2);
        when(mergeCommit.getId()).thenReturn(headId);
        when(mergeCommit.getFullMessage()).thenReturn("");


        GitSdk mockGitSdk = mock(GitSdk.class);
        gitSdkMockedStatic.when(() -> GitSdk.of(context)).thenReturn(mockGitSdk);
        when(mockGitSdk.open()).thenReturn(git);

        ChangelogGenerator.Commit commit = mock(ChangelogGenerator.Commit.class);
        when(commit.asContext(anyBoolean(), any())).thenReturn(new HashMap<>());
        commitMockedStatic.when(() -> ChangelogGenerator.Commit.of(any())).thenReturn(commit);

        Mockito.doReturn(true).when(changelogGenerator).checkLabels(commit, changelog);

        Mockito.doReturn("").when(changelogGenerator).categorize(commit, changelog);

        when(changelog.resolveFormatted(any())).thenReturn(formatted);
        when(changelog.isSkipMergeCommits()).thenReturn(skipMergeCommits);
        when(changelog.getResolvedContentTemplate(context)).thenReturn(new StringReader("Changelog"));
        return mergeCommit;
    }

    private LogCommand doNecessaryMock(String effectiveTagName, String configuredTagName, ObjectId headId, boolean isSnapshot, List<Ref> tagRefs) throws GitAPIException, IOException {
        ListTagCommand listTagCommand = mock(ListTagCommand.class);
        JReleaserModel model = mock(JReleaserModel.class);
        when(context.getModel()).thenReturn(model);
        Project project = mock(Project.class);
        when(model.getProject()).thenReturn(project);
        Release release = mock(Release.class);
        when(model.getRelease()).thenReturn(release);
        VersionPattern versionPattern = mock(VersionPattern.class);
        when(versionPattern.getType()).thenReturn(VersionPattern.Type.SEMVER);
        when(project.versionPattern()).thenReturn(versionPattern);
        LogCommand logCommand = mock(LogCommand.class, RETURNS_DEEP_STUBS);
        when(git.log()).thenReturn(logCommand);
        when(listTagCommand.call()).thenReturn(tagRefs);
        when(git.tagList()).thenReturn(listTagCommand);
        when(release.getGitService()).thenReturn(gitService);

        when(gitService.getEffectiveTagName(any())).thenReturn(effectiveTagName);
        when(gitService.getConfiguredTagName()).thenReturn(configuredTagName);
        when(git.getRepository().resolve(Constants.HEAD)).thenReturn(headId);
        doReturn(SemVer.of(effectiveTagName)).when(project).version();
        when(context.getModel().getProject().isSnapshot()).thenReturn(isSnapshot);

        return logCommand;
    }

    private List<Ref> buildMockedTagRefs(String[]... refs) {
        return Arrays.stream(refs).map(pair -> new ObjectIdRef.PeeledTag(null, pair[0],
                ObjectId.fromString(pair[1]),
                null, 1))
            .collect(toList());
    }
}

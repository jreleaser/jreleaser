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
import org.jreleaser.model.*;
import org.jreleaser.util.SemVer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.io.IOException;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ChangelogGeneratorUnitTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private Git git;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private JReleaserContext context;

    ChangelogGenerator changelogGenerator = new ChangelogGenerator();

    @Test
    @DisplayName("When configured tag has no prefix and no matches if found then all commits from head must be used")
    void notParsable() throws GitAPIException, IOException {

        String effectiveTagName = "2.2.0";
        String configuredTagName = "{{projectVersion}}";
        ObjectId headId = ObjectId.fromString("085bb3bcb608e1e8451d4b2432f8ecbe6306e7e7");
        boolean isSnapshot = false;
        List<Ref> tagRefs = buildMockedTagRefs(
                new AbstractMap.SimpleEntry("refs/tags/v1.0.0", "cac0cab538b970a37ea1e769cbbde608743bc96d"),
                new AbstractMap.SimpleEntry("refs/tags/v2.0.0", "a11bef06a3f659402fe7563abf99ad00de2209e6"));

        LogCommand logCommand = doNecessaryMock(effectiveTagName, configuredTagName, headId, isSnapshot, tagRefs);


        changelogGenerator.resolveCommits(git, context);

        verify(logCommand).add(headId);
    }

    @Test
    @DisplayName("When no tag is found that match current configured tag name then all commits from head must be used")
    void tagThatNoMatches() throws GitAPIException, IOException {

        String effectiveTagName = "2.2.0";
        String configuredTagName = "release-{{projectVersion}}";
        ObjectId headId = ObjectId.fromString("085bb3bcb608e1e8451d4b2432f8ecbe6306e7e7");
        boolean isSnapshot = false;
        List<Ref> tagRefs = buildMockedTagRefs(
                new AbstractMap.SimpleEntry("refs/tags/v1.0.0", "cac0cab538b970a37ea1e769cbbde608743bc96d"),
                new AbstractMap.SimpleEntry("refs/tags/v2.0.0", "a11bef06a3f659402fe7563abf99ad00de2209e6"));

        LogCommand logCommand = doNecessaryMock(effectiveTagName, configuredTagName, headId, isSnapshot, tagRefs);


        changelogGenerator.resolveCommits(git, context);

        verify(logCommand).add(headId);
    }

    private LogCommand doNecessaryMock(String effectiveTagName, String configuredTagName, ObjectId headId, boolean isSnapshot, List<Ref> tagRefs) throws GitAPIException, IOException {
        ListTagCommand listTagCommand = mock(ListTagCommand.class);
        JReleaserModel jReleaserModel = mock(JReleaserModel.class);
        when(context.getModel()).thenReturn(jReleaserModel);
        Project project = mock(Project.class);
        when(jReleaserModel.getProject()).thenReturn(project);
        Release release = mock(Release.class);
        when(jReleaserModel.getRelease()).thenReturn(release);
        VersionPattern versionPattern = mock(VersionPattern.class);
        when(versionPattern.getType()).thenReturn(VersionPattern.Type.SEMVER);
        when(project.versionPattern()).thenReturn(versionPattern);
        LogCommand logCommand = mock(LogCommand.class, RETURNS_DEEP_STUBS);
        when(git.log()).thenReturn(logCommand);
        when(listTagCommand.call()).thenReturn(tagRefs);
        when(git.tagList()).thenReturn(listTagCommand);
        GitService gitService = mock(GitService.class, RETURNS_DEEP_STUBS);
        when(release.getGitService()).thenReturn(gitService);

        when(gitService.getEffectiveTagName(any())).thenReturn(effectiveTagName);
        when(gitService.getConfiguredTagName()).thenReturn(configuredTagName);
        when(git.getRepository().resolve(Constants.HEAD)).thenReturn(headId);
        doReturn(SemVer.of(effectiveTagName)).when(project).version();
        when(context.getModel().getProject().isSnapshot()).thenReturn(isSnapshot);
        return logCommand;
    }

    private List<Ref> buildMockedTagRefs(AbstractMap.SimpleEntry<String, String>... refs) {

        return Arrays.stream(refs).map(pair ->  new ObjectIdRef.PeeledTag(null, pair.getKey(),
                ObjectId.fromString(pair.getValue()),
                null, 1)).collect(Collectors.toList());

    }
}
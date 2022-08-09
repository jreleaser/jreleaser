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
package org.jreleaser.gradle.plugin.internal.dsl

import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.internal.provider.Providers
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.jreleaser.gradle.plugin.dsl.Github
import org.kordamp.gradle.util.ConfigureUtil

import javax.inject.Inject

/**
 *
 * @author Andres Almiray
 * @since 0.1.0
 */
@CompileStatic
class GithubImpl extends AbstractGitService implements Github {
    final Property<Boolean> draft
    final Property<String> discussionCategoryName
    final ChangelogImpl changelog
    final MilestoneImpl milestone
    final CommitAuthorImpl commitAuthor
    final PrereleaseImpl prerelease
    final ReleaseNotesImpl releaseNotes

    @Inject
    GithubImpl(ObjectFactory objects) {
        super(objects)
        draft = objects.property(Boolean).convention(Providers.notDefined())
        discussionCategoryName = objects.property(String).convention(Providers.notDefined())

        changelog = objects.newInstance(ChangelogImpl, objects)
        milestone = objects.newInstance(MilestoneImpl, objects)
        commitAuthor = objects.newInstance(CommitAuthorImpl, objects)
        prerelease = objects.newInstance(PrereleaseImpl, objects)
        releaseNotes = objects.newInstance(ReleaseNotesImpl, objects)
    }

    @Override
    @Internal
    boolean isSet() {
        super.isSet() ||
            draft.present ||
            prerelease.isSet() ||
            releaseNotes.isSet() ||
            discussionCategoryName.present ||
            changelog.isSet() ||
            milestone.isSet() ||
            commitAuthor.isSet()
    }

    @Override
    void prerelease(Action<? super Prerelease> action) {
        action.execute(prerelease)
    }

    @Override
    void releaseNotes(Action<? super ReleaseNotes> action) {
        action.execute(releaseNotes)
    }

    @Override
    void prerelease(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Prerelease) Closure<Void> action) {
        ConfigureUtil.configure(action, prerelease)
    }

    @Override
    void releaseNotes(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = ReleaseNotes) Closure<Void> action) {
        ConfigureUtil.configure(action, releaseNotes)
    }

    org.jreleaser.model.Github toModel() {
        org.jreleaser.model.Github service = new org.jreleaser.model.Github()
        toModel(service)
        if (draft.present) service.draft = draft.get()
        service.prerelease = prerelease.toModel()
        service.releaseNotes = releaseNotes.toModel()
        if (discussionCategoryName.present) service.discussionCategoryName = discussionCategoryName.get()
        service.changelog = changelog.toModel()
        if (milestone.isSet()) service.milestone = milestone.toModel()
        if (commitAuthor.isSet()) service.commitAuthor = commitAuthor.toModel()
        service
    }

    @CompileStatic
    static class ReleaseNotesImpl implements ReleaseNotes {
        final Property<Boolean> enabled
        final Property<String> configurationFile

        @Inject
        ReleaseNotesImpl(ObjectFactory objects) {
            enabled = objects.property(Boolean).convention(Providers.notDefined())
            configurationFile = objects.property(String).convention(Providers.notDefined())
        }

        @Internal
        boolean isSet() {
            enabled.present ||
                configurationFile.present
        }

        org.jreleaser.model.Github.ReleaseNotes toModel() {
            org.jreleaser.model.Github.ReleaseNotes releaseNotes = new org.jreleaser.model.Github.ReleaseNotes()
            if (enabled.present) releaseNotes.enabled = enabled.get()
            if (configurationFile.present) releaseNotes.configurationFile = configurationFile.get()
            releaseNotes
        }
    }
}

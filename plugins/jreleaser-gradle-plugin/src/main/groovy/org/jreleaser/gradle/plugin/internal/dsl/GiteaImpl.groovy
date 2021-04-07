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
package org.jreleaser.gradle.plugin.internal.dsl

import groovy.transform.CompileStatic
import org.gradle.api.internal.provider.Providers
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.jreleaser.gradle.plugin.dsl.Gitea

import javax.inject.Inject

/**
 *
 * @author Andres Almiray
 * @since 0.1.0
 */
@CompileStatic
class GiteaImpl extends AbstractGitService implements Gitea {
    final Property<String> targetCommitish
    final Property<Boolean> draft
    final Property<Boolean> prerelease
    final ChangelogImpl changelog
    final MilestoneImpl milestone
    final CommitAuthorImpl commitAuthor

    @Inject
    GiteaImpl(ObjectFactory objects) {
        super(objects)
        targetCommitish = objects.property(String).convention(Providers.notDefined())
        draft = objects.property(Boolean).convention(Providers.notDefined())
        prerelease = objects.property(Boolean).convention(Providers.notDefined())

        changelog = objects.newInstance(ChangelogImpl, objects)
        milestone = objects.newInstance(MilestoneImpl, objects)
        commitAuthor = objects.newInstance(CommitAuthorImpl, objects)
    }

    @Internal
    boolean isSet() {
        super.isSet() ||
            targetCommitish.present ||
            draft.present ||
            prerelease.present ||
            changelog.isSet() ||
            milestone.isSet() ||
            commitAuthor.isSet()
    }

    org.jreleaser.model.Gitea toModel() {
        org.jreleaser.model.Gitea service = new org.jreleaser.model.Gitea()
        toModel(service)
        if (targetCommitish.present) service.targetCommitish = targetCommitish.get()
        service.draft = draft.getOrElse(false)
        service.prerelease = prerelease.getOrElse(false)
        if (changelog.isSet()) service.changelog = changelog.toModel()
        if (milestone.isSet()) service.milestone = milestone.toModel()
        if (commitAuthor.isSet()) service.commitAuthor = commitAuthor.toModel()
        service
    }
}

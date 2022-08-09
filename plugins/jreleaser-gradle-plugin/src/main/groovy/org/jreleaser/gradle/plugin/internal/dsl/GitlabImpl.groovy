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
import org.gradle.api.internal.provider.Providers
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.jreleaser.gradle.plugin.dsl.Gitlab

import javax.inject.Inject

/**
 *
 * @author Andres Almiray
 * @since 0.1.0
 */
@CompileStatic
class GitlabImpl extends AbstractGitService implements Gitlab {
    final ChangelogImpl changelog
    final MilestoneImpl milestone
    final CommitAuthorImpl commitAuthor
    final Property<String> projectIdentifier
    final MapProperty<String, String> uploadLinks

    @Inject
    GitlabImpl(ObjectFactory objects) {
        super(objects)

        changelog = objects.newInstance(ChangelogImpl, objects)
        milestone = objects.newInstance(MilestoneImpl, objects)
        commitAuthor = objects.newInstance(CommitAuthorImpl, objects)

        projectIdentifier = objects.property(String).convention(Providers.notDefined())
        uploadLinks = objects.mapProperty(String, String).convention(Providers.notDefined())
    }

    Property<String> getIdentifier() {
        projectIdentifier
    }

    @Override
    @Internal
    boolean isSet() {
        super.isSet() ||
            changelog.isSet() ||
            milestone.isSet() ||
            commitAuthor.isSet() ||
            projectIdentifier.present ||
            uploadLinks.present
    }

    org.jreleaser.model.Gitlab toModel() {
        org.jreleaser.model.Gitlab service = new org.jreleaser.model.Gitlab()
        toModel(service)
        service.changelog = changelog.toModel()
        if (milestone.isSet()) service.milestone = milestone.toModel()
        if (commitAuthor.isSet()) service.commitAuthor = commitAuthor.toModel()
        if (projectIdentifier.present) service.projectIdentifier = projectIdentifier.get()
        if (uploadLinks.present) service.uploadLinks.putAll(uploadLinks.get())
        service
    }
}

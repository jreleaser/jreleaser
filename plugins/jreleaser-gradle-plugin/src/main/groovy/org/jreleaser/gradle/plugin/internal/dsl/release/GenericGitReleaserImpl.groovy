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
package org.jreleaser.gradle.plugin.internal.dsl.release

import groovy.transform.CompileStatic
import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.Internal
import org.jreleaser.gradle.plugin.dsl.release.GenericGitReleaser
import org.jreleaser.gradle.plugin.internal.dsl.common.CommitAuthorImpl

import javax.inject.Inject

/**
 *
 * @author Andres Almiray
 * @since 0.4.0
 */
@CompileStatic
class GenericGitReleaserImpl extends BaseReleaserImpl implements GenericGitReleaser {
    final ChangelogImpl changelog
    final MilestoneImpl milestone
    final CommitAuthorImpl commitAuthor

    @Inject
    GenericGitReleaserImpl(ObjectFactory objects) {
        super(objects)
        changelog = objects.newInstance(ChangelogImpl, objects)
        milestone = objects.newInstance(MilestoneImpl, objects)
        commitAuthor = objects.newInstance(CommitAuthorImpl, objects)
    }

    @Override
    @Internal
    boolean isSet() {
        super.isSet() ||
            changelog.isSet() ||
            milestone.isSet() ||
            commitAuthor.isSet()
    }

    org.jreleaser.model.internal.release.GenericGitReleaser toModel() {
        org.jreleaser.model.internal.release.GenericGitReleaser service = new org.jreleaser.model.internal.release.GenericGitReleaser()
        toModel(service)
        service.changelog = changelog.toModel()
        if (milestone.isSet()) service.milestone = milestone.toModel()
        if (commitAuthor.isSet()) service.commitAuthor = commitAuthor.toModel()
        service
    }
}

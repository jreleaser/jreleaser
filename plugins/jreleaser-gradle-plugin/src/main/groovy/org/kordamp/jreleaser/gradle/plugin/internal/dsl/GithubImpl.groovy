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
package org.kordamp.jreleaser.gradle.plugin.internal.dsl

import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.internal.provider.Providers
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.kordamp.jreleaser.gradle.plugin.dsl.Changelog
import org.kordamp.jreleaser.gradle.plugin.dsl.Github

import javax.inject.Inject

/**
 *
 * @author Andres Almiray
 * @since 0.1.0
 */
@CompileStatic
class GithubImpl extends AbstractGitService implements Github {
    final Property<String> targetCommitish
    final Property<Boolean> draft
    final Property<Boolean> prerelease
    final ChangelogImpl changelog

    @Inject
    GithubImpl(ObjectFactory objects) {
        super(objects)
        targetCommitish = objects.property(String).convention(Providers.notDefined())
        draft = objects.property(Boolean).convention(false)
        prerelease = objects.property(Boolean).convention(false)
        changelog = objects.newInstance(ChangelogImpl, objects)
    }

    @Override
    void changelog(Action<? super Changelog> action) {
        action.execute(changelog)
    }

    @Internal
    boolean isSet() {
        super.set ||
            targetCommitish.present ||
            draft.present ||
            prerelease.present ||
            changelog.set
    }

    org.kordamp.jreleaser.model.Github toModel() {
        org.kordamp.jreleaser.model.Github service = new org.kordamp.jreleaser.model.Github()
        toModel(service)
        if (targetCommitish.present) service.targetCommitish = targetCommitish.get()
        if (draft.present) service.draft = draft.get()
        if (prerelease.present) service.prerelease = prerelease.get()
        if (changelog.set) service.changelog = changelog.toModel()
        service
    }
}

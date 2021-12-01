/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2021 The JReleaser authors.
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
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.jreleaser.gradle.plugin.dsl.CommitAuthor
import org.jreleaser.gradle.plugin.dsl.Spec
import org.jreleaser.gradle.plugin.dsl.Tap
import org.kordamp.gradle.util.ConfigureUtil

import javax.inject.Inject

/**
 *
 * @author Andres Almiray
 * @since 0.9.1
 */
@CompileStatic
class SpecImpl extends AbstractRepositoryTool implements Spec {
    final Property<String> release
    final CommitAuthorImpl commitAuthor
    final TapImpl repository
    final ListProperty<String> requires

    @Inject
    SpecImpl(ObjectFactory objects) {
        super(objects)
        release = objects.property(String).convention(Providers.notDefined())
        repository = objects.newInstance(TapImpl, objects)
        commitAuthor = objects.newInstance(CommitAuthorImpl, objects)
        requires = objects.listProperty(String).convention(Providers.notDefined())
    }

    @Override
    @Internal
    boolean isSet() {
        super.isSet() ||
            commitAuthor.isSet() ||
            release.present ||
            repository.isSet() ||
            requires.present
    }

    @Override
    void repository(Action<? super Tap> action) {
        action.execute(repository)
    }

    @Override
    void commitAuthor(Action<? super CommitAuthor> action) {
        action.execute(commitAuthor)
    }

    @Override
    void repository(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Tap) Closure<Void> action) {
        ConfigureUtil.configure(action, repository)
    }

    @Override
    void commitAuthor(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = CommitAuthor) Closure<Void> action) {
        ConfigureUtil.configure(action, commitAuthor)
    }

    org.jreleaser.model.Spec toModel() {
        org.jreleaser.model.Spec tool = new org.jreleaser.model.Spec()
        fillToolProperties(tool)
        fillTemplateToolProperties(tool)
        if (release.present) tool.release = release.get()
        if (repository.isSet()) tool.repository = repository.toSpecRepository()
        if (commitAuthor.isSet()) tool.commitAuthor = commitAuthor.toModel()
        if (requires.present) tool.requires = (requires.get() as List<String>)
        tool
    }
}

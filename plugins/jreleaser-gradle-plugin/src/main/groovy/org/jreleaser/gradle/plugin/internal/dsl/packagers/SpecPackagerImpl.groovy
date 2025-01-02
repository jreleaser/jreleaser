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
package org.jreleaser.gradle.plugin.internal.dsl.packagers

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.internal.provider.Providers
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.jreleaser.gradle.plugin.dsl.common.CommitAuthor
import org.jreleaser.gradle.plugin.dsl.packagers.SpecPackager
import org.jreleaser.gradle.plugin.dsl.packagers.Tap
import org.jreleaser.gradle.plugin.internal.dsl.common.CommitAuthorImpl
import org.kordamp.gradle.util.ConfigureUtil

import javax.inject.Inject

/**
 *
 * @author Andres Almiray
 * @since 0.9.1
 */
@CompileStatic
class SpecPackagerImpl extends AbstractRepositoryPackager implements SpecPackager {
    final Property<String> packageName
    final Property<String> release
    final CommitAuthorImpl commitAuthor
    final TapImpl repository
    final ListProperty<String> requires

    @Inject
    SpecPackagerImpl(ObjectFactory objects) {
        super(objects)
        packageName = objects.property(String).convention(Providers.<String> notDefined())
        release = objects.property(String).convention(Providers.<String> notDefined())
        repository = objects.newInstance(TapImpl, objects)
        commitAuthor = objects.newInstance(CommitAuthorImpl, objects)
        requires = objects.listProperty(String).convention(Providers.<List<String>> notDefined())
    }

    @Override
    @Internal
    boolean isSet() {
        super.isSet() ||
            packageName.present ||
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
    @CompileDynamic
    void repository(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Tap) Closure<Void> action) {
        ConfigureUtil.configure(action, repository)
    }

    @Override
    @CompileDynamic
    void commitAuthor(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = CommitAuthor) Closure<Void> action) {
        ConfigureUtil.configure(action, commitAuthor)
    }

    org.jreleaser.model.internal.packagers.SpecPackager toModel() {
        org.jreleaser.model.internal.packagers.SpecPackager packager = new org.jreleaser.model.internal.packagers.SpecPackager()
        fillPackagerProperties(packager)
        fillTemplatePackagerProperties(packager)
        if (packageName.present) packager.packageName = packageName.get()
        if (release.present) packager.release = release.get()
        if (repository.isSet()) packager.repository = repository.toSpecRepository()
        if (commitAuthor.isSet()) packager.commitAuthor = commitAuthor.toModel()
        if (requires.present) packager.requires = (requires.get() as List<String>)
        packager
    }
}

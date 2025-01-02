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
import org.jreleaser.gradle.plugin.dsl.packagers.MacportsPackager
import org.jreleaser.gradle.plugin.dsl.packagers.Tap
import org.jreleaser.gradle.plugin.internal.dsl.common.CommitAuthorImpl
import org.kordamp.gradle.util.ConfigureUtil

import javax.inject.Inject

import static org.jreleaser.util.StringUtils.isNotBlank

/**
 *
 * @author Andres Almiray
 * @since 0.9.0
 */
@CompileStatic
class MacportsPackagerImpl extends AbstractRepositoryPackager implements MacportsPackager {
    final Property<String> packageName
    final Property<Integer> revision
    final CommitAuthorImpl commitAuthor
    final TapImpl repository
    final ListProperty<String> categories
    final ListProperty<String> maintainers

    @Inject
    MacportsPackagerImpl(ObjectFactory objects) {
        super(objects)
        packageName = objects.property(String).convention(Providers.<String> notDefined())
        revision = objects.property(Integer).convention(Providers.<Integer> notDefined())
        repository = objects.newInstance(TapImpl, objects)
        commitAuthor = objects.newInstance(CommitAuthorImpl, objects)
        categories = objects.listProperty(String).convention(Providers.<List<String>> notDefined())
        maintainers = objects.listProperty(String).convention(Providers.<List<String>> notDefined())
    }

    @Override
    @Internal
    boolean isSet() {
        super.isSet() ||
            packageName.present ||
            commitAuthor.isSet() ||
            revision.present ||
            repository.isSet() ||
            categories.present ||
            maintainers.present
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

    @Override
    void category(String category) {
        if (isNotBlank(category)) {
            categories.add(category)
        }
    }

    @Override
    void maintainer(String maintainer) {
        if (isNotBlank(maintainer)) {
            maintainers.add(maintainer)
        }
    }

    org.jreleaser.model.internal.packagers.MacportsPackager toModel() {
        org.jreleaser.model.internal.packagers.MacportsPackager packager = new org.jreleaser.model.internal.packagers.MacportsPackager()
        fillPackagerProperties(packager)
        fillTemplatePackagerProperties(packager)
        if (packageName.present) packager.packageName = packageName.get()
        if (revision.present) packager.revision = revision.get()
        if (repository.isSet()) packager.repository = repository.toMacportsRepository()
        if (commitAuthor.isSet()) packager.commitAuthor = commitAuthor.toModel()
        if (categories.present) packager.categories = (categories.get() as List<String>)
        if (maintainers.present) packager.maintainers = (maintainers.get() as List<String>)
        packager
    }
}

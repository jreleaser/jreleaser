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
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.jreleaser.gradle.plugin.dsl.common.CommitAuthor
import org.jreleaser.gradle.plugin.dsl.packagers.ChocolateyPackager
import org.jreleaser.gradle.plugin.dsl.packagers.Tap
import org.jreleaser.gradle.plugin.internal.dsl.common.CommitAuthorImpl
import org.kordamp.gradle.util.ConfigureUtil

import javax.inject.Inject

/**
 *
 * @author Andres Almiray
 * @since 0.1.0
 */
@CompileStatic
class ChocolateyPackagerImpl extends AbstractRepositoryPackager implements ChocolateyPackager {
    final Property<String> packageName
    final Property<String> packageVersion
    final Property<String> username
    final Property<String> apiKey
    final Property<String> title
    final Property<String> iconUrl
    final Property<String> source
    final Property<Boolean> remoteBuild
    final CommitAuthorImpl commitAuthor
    final TapImpl repository

    @Inject
    ChocolateyPackagerImpl(ObjectFactory objects) {
        super(objects)
        packageName = objects.property(String).convention(Providers.<String> notDefined())
        packageVersion = objects.property(String).convention(Providers.<String> notDefined())
        username = objects.property(String).convention(Providers.<String> notDefined())
        apiKey = objects.property(String).convention(Providers.<String> notDefined())
        title = objects.property(String).convention(Providers.<String> notDefined())
        iconUrl = objects.property(String).convention(Providers.<String> notDefined())
        source = objects.property(String).convention(Providers.<String> notDefined())
        remoteBuild = objects.property(Boolean).convention(Providers.<Boolean> notDefined())
        repository = objects.newInstance(TapImpl, objects)
        commitAuthor = objects.newInstance(CommitAuthorImpl, objects)
    }

    @Override
    @Internal
    boolean isSet() {
        super.isSet() ||
            packageName.present ||
            packageVersion.present ||
            username.present ||
            apiKey.present ||
            title.present ||
            iconUrl.present ||
            source.present ||
            remoteBuild.present ||
            repository.isSet() ||
            commitAuthor.isSet()
    }

    @Override
    Tap getBucket() {
        getRepository()
    }

    @Override
    void bucket(Action<? super Tap> action) {
        repository(action)
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
    void bucket(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Tap) Closure<Void> action) {
        repository(action)
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

    org.jreleaser.model.internal.packagers.ChocolateyPackager toModel() {
        org.jreleaser.model.internal.packagers.ChocolateyPackager packager = new org.jreleaser.model.internal.packagers.ChocolateyPackager()
        fillPackagerProperties(packager)
        fillTemplatePackagerProperties(packager)
        if (repository.isSet()) packager.repository = repository.toChocolateyRepository()
        if (commitAuthor.isSet()) packager.commitAuthor = commitAuthor.toModel()
        if (packageName.present) packager.packageName = packageName.get()
        if (packageVersion.present) packager.packageVersion = packageVersion.get()
        if (username.present) packager.username = username.get()
        if (apiKey.present) packager.apiKey = apiKey.get()
        if (title.present) packager.title = title.get()
        if (iconUrl.present) packager.iconUrl = iconUrl.get()
        if (source.present) packager.source = source.get()
        packager.remoteBuild = remoteBuild.getOrElse(false)
        packager
    }
}

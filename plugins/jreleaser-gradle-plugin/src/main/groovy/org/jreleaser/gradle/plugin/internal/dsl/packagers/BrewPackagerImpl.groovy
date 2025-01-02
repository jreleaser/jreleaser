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
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.Internal
import org.jreleaser.gradle.plugin.dsl.common.CommitAuthor
import org.jreleaser.gradle.plugin.dsl.packagers.BrewPackager
import org.jreleaser.gradle.plugin.dsl.packagers.Tap
import org.jreleaser.gradle.plugin.internal.dsl.common.CommitAuthorImpl
import org.kordamp.gradle.util.ConfigureUtil

import javax.inject.Inject

import static org.jreleaser.util.StringUtils.isNotBlank

/**
 *
 * @author Andres Almiray
 * @since 0.1.0
 */
@CompileStatic
class BrewPackagerImpl extends AbstractRepositoryPackager implements BrewPackager {
    final Property<String> formulaName
    final Property<String> downloadStrategy
    final Property<Boolean> multiPlatform
    final CommitAuthorImpl commitAuthor
    final TapImpl repository
    final CaskImpl cask
    final MapProperty<String, String> dependencies
    final ListProperty<String> livecheck
    final SetProperty<String> requireRelative

    @Inject
    BrewPackagerImpl(ObjectFactory objects) {
        super(objects)
        formulaName = objects.property(String).convention(Providers.<String> notDefined())
        downloadStrategy = objects.property(String).convention(Providers.<String> notDefined())
        multiPlatform = objects.property(Boolean).convention(Providers.<Boolean> notDefined())
        repository = objects.newInstance(TapImpl, objects)
        cask = objects.newInstance(CaskImpl, objects)
        commitAuthor = objects.newInstance(CommitAuthorImpl, objects)
        dependencies = objects.mapProperty(String, String).convention(Providers.notDefined())
        livecheck = objects.listProperty(String).convention(Providers.<List<String>> notDefined())
        requireRelative = objects.setProperty(String).convention(Providers.<Set<String>> notDefined())
    }

    @Override
    void dependency(String key, String value) {
        if (isNotBlank(key) && isNotBlank(value)) {
            dependencies.put(key.trim(), value.trim())
        }
    }

    @Override
    void dependency(String key) {
        if (isNotBlank(key)) {
            dependencies.put(key.trim(), 'null')
        }
    }

    @Override
    @Internal
    boolean isSet() {
        super.isSet() ||
            formulaName.present ||
            downloadStrategy.present ||
            multiPlatform.present ||
            dependencies.present ||
            repository.isSet() ||
            commitAuthor.isSet() ||
            livecheck.present ||
            requireRelative.present ||
            cask.isSet()
    }

    @Override
    Tap getRepoTap() {
        getRepository()
    }

    @Override
    void repoTap(Action<? super Tap> action) {
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
    void cask(Action<? super Cask> action) {
        action.execute(cask)
    }

    @Override
    @CompileDynamic
    void repoTap(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Tap) Closure<Void> action) {
        ConfigureUtil.configure(action, repository)
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
    @CompileDynamic
    void cask(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Cask) Closure<Void> action) {
        ConfigureUtil.configure(action, cask)
    }

    org.jreleaser.model.internal.packagers.BrewPackager toModel() {
        org.jreleaser.model.internal.packagers.BrewPackager packager = new org.jreleaser.model.internal.packagers.BrewPackager()
        fillPackagerProperties(packager)
        fillTemplatePackagerProperties(packager)
        if (formulaName.present) packager.formulaName = formulaName.get()
        if (downloadStrategy.present) packager.downloadStrategy = downloadStrategy.get()
        if (multiPlatform.present) packager.multiPlatform = multiPlatform.get()
        if (repository.isSet()) packager.repository = repository.toHomebrewRepository()
        if (commitAuthor.isSet()) packager.commitAuthor = commitAuthor.toModel()
        if (dependencies.present) packager.dependencies = dependencies.get()
        if (livecheck.present) packager.livecheck = (livecheck.get() as List<String>)
        if (requireRelative.present) packager.requireRelative = (requireRelative.get() as Set<String>)
        if (cask.isSet()) packager.cask = cask.toModel()
        packager
    }

    @CompileStatic
    static class CaskImpl implements Cask {
        final Property<String> name
        final Property<String> displayName
        final Property<String> pkgName
        final Property<String> appName
        final Property<String> appcast
        final Property<Boolean> enabled
        final MapProperty<String, List<String>> uninstall
        final MapProperty<String, List<String>> zap

        @Inject
        CaskImpl(ObjectFactory objects) {
            displayName = objects.property(String).convention(Providers.<String> notDefined())
            name = objects.property(String).convention(Providers.<String> notDefined())
            pkgName = objects.property(String).convention(Providers.<String> notDefined())
            appName = objects.property(String).convention(Providers.<String> notDefined())
            appcast = objects.property(String).convention(Providers.<String> notDefined())
            enabled = objects.property(Boolean).convention(Providers.<Boolean> notDefined())
            uninstall = (objects.mapProperty(String, List).convention(Providers.notDefined()) as MapProperty<String, List<String>>)
            zap = (objects.mapProperty(String, List).convention(Providers.notDefined()) as MapProperty<String, List<String>>)
        }

        @Internal
        boolean isSet() {
            displayName.present ||
                name.present ||
                pkgName.present ||
                appName.present ||
                appcast.present ||
                enabled.present ||
                uninstall.present ||
                zap.present
        }

        org.jreleaser.model.internal.packagers.BrewPackager.Cask toModel() {
            org.jreleaser.model.internal.packagers.BrewPackager.Cask cask = new org.jreleaser.model.internal.packagers.BrewPackager.Cask()
            if (displayName.present) cask.displayName = displayName.get()
            if (name.present) cask.name = name.get()
            if (pkgName.present) cask.pkgName = pkgName.get()
            if (appName.present) cask.appName = appName.get()
            if (appcast.present) cask.appcast = appcast.get()
            if (enabled.present) cask.enabled = enabled.get()
            if (uninstall.present) cask.uninstall = uninstall.get()
            if (zap.present) cask.zap = zap.get()
            cask
        }
    }
}

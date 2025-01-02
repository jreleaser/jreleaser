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
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.NamedDomainObjectFactory
import org.gradle.api.internal.provider.Providers
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.Internal
import org.jreleaser.gradle.plugin.dsl.common.CommitAuthor
import org.jreleaser.gradle.plugin.dsl.common.Icon
import org.jreleaser.gradle.plugin.dsl.common.Screenshot
import org.jreleaser.gradle.plugin.dsl.packagers.FlatpakPackager
import org.jreleaser.gradle.plugin.dsl.packagers.Tap
import org.jreleaser.gradle.plugin.internal.dsl.common.CommitAuthorImpl
import org.jreleaser.gradle.plugin.internal.dsl.common.IconImpl
import org.jreleaser.gradle.plugin.internal.dsl.common.ScreenshotImpl
import org.jreleaser.model.Flatpak
import org.kordamp.gradle.util.ConfigureUtil

import javax.inject.Inject

import static org.jreleaser.util.StringUtils.isNotBlank

/**
 *
 * @author Andres Almiray
 * @since 1.2.0
 */
@CompileStatic
class FlatpakPackagerImpl extends AbstractRepositoryPackager implements FlatpakPackager {
    final CommitAuthorImpl commitAuthor
    final TapImpl repository
    final Property<String> componentId
    final ListProperty<String> categories
    final Property<String> developerName
    final Property<Flatpak.Runtime> runtime
    final Property<String> runtimeVersion
    final SetProperty<String> sdkExtensions
    final SetProperty<String> finishArgs
    final SetProperty<String> skipReleases

    private final NamedDomainObjectContainer<ScreenshotImpl> screenshots
    private final NamedDomainObjectContainer<IconImpl> icons

    @Inject
    FlatpakPackagerImpl(ObjectFactory objects) {
        super(objects)
        repository = objects.newInstance(TapImpl, objects)
        commitAuthor = objects.newInstance(CommitAuthorImpl, objects)
        componentId = objects.property(String).convention(Providers.<String> notDefined())
        categories = objects.listProperty(String).convention(Providers.<List<String>> notDefined())
        developerName = objects.property(String).convention(Providers.<String> notDefined())
        runtime = objects.property(Flatpak.Runtime).convention(Providers.<Flatpak.Runtime> notDefined())
        runtimeVersion = objects.property(String).convention(Providers.<String> notDefined())
        sdkExtensions = objects.setProperty(String).convention(Providers.<Set<String>> notDefined())
        finishArgs = objects.setProperty(String).convention(Providers.<Set<String>> notDefined())
        skipReleases = objects.setProperty(String).convention(Providers.<Set<String>> notDefined())

        screenshots = objects.domainObjectContainer(ScreenshotImpl, new NamedDomainObjectFactory<ScreenshotImpl>() {
            @Override
            ScreenshotImpl create(String name) {
                ScreenshotImpl screenshot = objects.newInstance(ScreenshotImpl, objects)
                screenshot.name = name
                screenshot
            }
        })

        icons = objects.domainObjectContainer(IconImpl, new NamedDomainObjectFactory<IconImpl>() {
            @Override
            IconImpl create(String name) {
                IconImpl icon = objects.newInstance(IconImpl, objects)
                icon.name = name
                icon
            }
        })
    }

    @Override
    void setRuntime(String str) {
        if (isNotBlank(str)) {
            this.runtime.set(Flatpak.Runtime.of(str.trim()))
        }
    }

    @Override
    @Internal
    boolean isSet() {
        super.isSet() ||
            commitAuthor.isSet() ||
            repository.isSet() ||
            componentId.present ||
            categories.present ||
            developerName.present ||
            runtime.present ||
            runtimeVersion.present ||
            sdkExtensions.present ||
            finishArgs.present ||
            skipReleases.present ||
            !screenshots.empty ||
            !icons.empty
    }

    @Override
    void category(String str) {
        if (isNotBlank(str)) {
            categories.add(str.trim())
        }
    }

    @Override
    void sdkExtension(String str) {
        if (isNotBlank(str)) {
            sdkExtensions.add(str.trim())
        }
    }

    @Override
    void finishArg(String str) {
        if (isNotBlank(str)) {
            finishArgs.add(str.trim())
        }
    }

    @Override
    void skipRelease(String str) {
        if (isNotBlank(str)) {
            skipReleases.add(str.trim())
        }
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
    void screenshot(Action<? super Screenshot> action) {
        action.execute(screenshots.maybeCreate("screenshot-${screenshots.size()}".toString()))
    }

    @Override
    @CompileDynamic
    void screenshot(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Screenshot) Closure<Void> action) {
        ConfigureUtil.configure(action, screenshots.maybeCreate("screenshot-${screenshots.size()}".toString()))
    }

    @Override
    void icon(Action<? super Icon> action) {
        action.execute(icons.maybeCreate("icons-${icons.size()}".toString()))
    }

    @Override
    @CompileDynamic
    void icon(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Icon) Closure<Void> action) {
        ConfigureUtil.configure(action, icons.maybeCreate("icons-${icons.size()}".toString()))
    }

    org.jreleaser.model.internal.packagers.FlatpakPackager toModel() {
        org.jreleaser.model.internal.packagers.FlatpakPackager packager = new org.jreleaser.model.internal.packagers.FlatpakPackager()
        fillPackagerProperties(packager)
        fillTemplatePackagerProperties(packager)
        if (repository.isSet()) packager.repository = repository.toFlatpakRepository()
        if (commitAuthor.isSet()) packager.commitAuthor = commitAuthor.toModel()
        if (componentId.present) packager.componentId = componentId.get()
        packager.categories = (List<String>) categories.getOrElse([])
        if (developerName.present) packager.developerName = developerName.get()
        if (runtime.present) packager.runtime = runtime.get()
        if (runtimeVersion.present) packager.runtimeVersion = runtimeVersion.get()
        packager.sdkExtensions = (Set<String>) sdkExtensions.getOrElse(new LinkedHashSet<String>())
        packager.finishArgs = (Set<String>) finishArgs.getOrElse(new LinkedHashSet<String>())
        packager.skipReleases = (Set<String>) skipReleases.getOrElse(new LinkedHashSet<String>())
        for (ScreenshotImpl screenshot : screenshots) {
            packager.addScreenshot(screenshot.toModel())
        }
        for (IconImpl icon : icons) {
            packager.addIcon(icon.toModel())
        }
        packager
    }
}

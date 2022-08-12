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
import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.NamedDomainObjectFactory
import org.gradle.api.internal.provider.Providers
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.jreleaser.gradle.plugin.dsl.AppImage
import org.jreleaser.gradle.plugin.dsl.CommitAuthor
import org.jreleaser.gradle.plugin.dsl.Screenshot
import org.jreleaser.gradle.plugin.dsl.Tap
import org.kordamp.gradle.util.ConfigureUtil

import javax.inject.Inject

import static org.jreleaser.util.StringUtils.isNotBlank

/**
 *
 * @author Andres Almiray
 * @since 1.2.0
 */
@CompileStatic
class AppImageImpl extends AbstractRepositoryPackager implements AppImage {
    final CommitAuthorImpl commitAuthor
    final TapImpl repository
    final Property<String> componentId
    final ListProperty<String> categories
    final Property<String> developerName
    final Property<Boolean> requiresTerminal

    private final NamedDomainObjectContainer<ScreenshotImpl> screenshots

    @Inject
    AppImageImpl(ObjectFactory objects) {
        super(objects)
        repository = objects.newInstance(TapImpl, objects)
        commitAuthor = objects.newInstance(CommitAuthorImpl, objects)
        componentId = objects.property(String).convention(Providers.notDefined())
        categories = objects.listProperty(String).convention(Providers.notDefined())
        developerName = objects.property(String).convention(Providers.notDefined())
        requiresTerminal = objects.property(Boolean).convention(Providers.notDefined())

        screenshots = objects.domainObjectContainer(ScreenshotImpl, new NamedDomainObjectFactory<ScreenshotImpl>() {
            @Override
            ScreenshotImpl create(String name) {
                ScreenshotImpl screenshot = objects.newInstance(ScreenshotImpl, objects)
                screenshot.name = name
                screenshot
            }
        })
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
            requiresTerminal.present ||
            !screenshots.empty
    }

    @Override
    void category(String category) {
        if (isNotBlank(category)) {
            categories.add(category.trim())
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
    void repository(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Tap) Closure<Void> action) {
        ConfigureUtil.configure(action, repository)
    }

    @Override
    void commitAuthor(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = CommitAuthor) Closure<Void> action) {
        ConfigureUtil.configure(action, commitAuthor)
    }

    @Override
    void screenshot(Action<? super Screenshot> action) {
        action.execute(screenshots.maybeCreate("screenshot-${screenshots.size()}".toString()))
    }

    @Override
    void screenshot(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Screenshot) Closure<Void> action) {
        ConfigureUtil.configure(action, screenshots.maybeCreate("screenshot-${screenshots.size()}".toString()))
    }

    org.jreleaser.model.AppImage toModel() {
        org.jreleaser.model.AppImage packager = new org.jreleaser.model.AppImage()
        fillPackagerProperties(packager)
        fillTemplatePackagerProperties(packager)
        if (repository.isSet()) packager.repository = repository.toAppImageRepository()
        if (commitAuthor.isSet()) packager.commitAuthor = commitAuthor.toModel()
        if (componentId.present) packager.componentId = componentId.get()
        packager.categories = (List<String>) categories.getOrElse([])
        if (developerName.present) packager.developerName = developerName.get()
        if (requiresTerminal.present) packager.requiresTerminal = requiresTerminal.get()
        for (ScreenshotImpl screenshot : screenshots) {
            packager.addScreenshot(screenshot.toModel())
        }
        packager
    }
}

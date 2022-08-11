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
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Internal
import org.jreleaser.gradle.plugin.dsl.Java
import org.jreleaser.gradle.plugin.dsl.Project
import org.jreleaser.gradle.plugin.dsl.Screenshot
import org.jreleaser.model.Stereotype
import org.kordamp.gradle.util.ConfigureUtil

import javax.inject.Inject

import static org.jreleaser.util.StringUtils.isNotBlank

/**
 *
 * @author Andres Almiray
 * @since 0.1.0
 */
@CompileStatic
class ProjectImpl implements Project {
    final Property<String> name
    final Property<String> version
    final Property<String> versionPattern
    final Property<String> description
    final Property<String> longDescription
    final Property<String> website
    final Property<String> inceptionYear
    final Property<String> license
    final Property<String> licenseUrl
    final Property<String> copyright
    final Property<String> vendor
    final Property<String> docsUrl
    final Property<Stereotype> stereotype
    final ListProperty<String> authors
    final ListProperty<String> tags
    final ListProperty<String> maintainers
    final MapProperty<String, Object> extraProperties
    final JavaImpl java
    final SnapshotImpl snapshot
    final LinksImpl links

    private final NamedDomainObjectContainer<ScreenshotImpl> screenshots

    @Inject
    ProjectImpl(ObjectFactory objects,
                Provider<String> nameProvider,
                Provider<String> descriptionProvider,
                Provider<String> versionProvider) {
        name = objects.property(String).convention(nameProvider)
        version = objects.property(String).convention(versionProvider)
        versionPattern = objects.property(String).convention(Providers.notDefined())
        description = objects.property(String).convention(descriptionProvider)
        longDescription = objects.property(String).convention(descriptionProvider)
        website = objects.property(String).convention(Providers.notDefined())
        license = objects.property(String).convention(Providers.notDefined())
        inceptionYear = objects.property(String).convention(Providers.notDefined())
        licenseUrl = objects.property(String).convention(Providers.notDefined())
        copyright = objects.property(String).convention(Providers.notDefined())
        vendor = objects.property(String).convention(Providers.notDefined())
        docsUrl = objects.property(String).convention(Providers.notDefined())
        stereotype = objects.property(Stereotype).convention(Providers.notDefined())
        authors = objects.listProperty(String).convention(Providers.notDefined())
        tags = objects.listProperty(String).convention(Providers.notDefined())
        maintainers = objects.listProperty(String).convention(Providers.notDefined())
        extraProperties = objects.mapProperty(String, Object).convention(Providers.notDefined())

        java = objects.newInstance(JavaImpl, objects)
        snapshot = objects.newInstance(SnapshotImpl, objects)
        links = objects.newInstance(LinksImpl, objects)

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
    void setStereotype(String str) {
        if (isNotBlank(str)) {
            stereotype.set(Stereotype.of(str.trim()))
        }
    }

    @Override
    void author(String name) {
        if (isNotBlank(name)) {
            authors.add(name.trim())
        }
    }

    @Override
    void tag(String tag) {
        if (isNotBlank(tag)) {
            tags.add(tag.trim())
        }
    }

    @Override
    void maintainer(String maintainer) {
        if (isNotBlank(maintainer)) {
            maintainers.add(maintainer)
        }
    }

    @Override
    void links(Action<? super Links> action) {
        action.execute(links)
    }

    @Override
    void links(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Links) Closure<Void> action) {
        ConfigureUtil.configure(action, links)
    }

    @Override
    void java(Action<? super Java> action) {
        action.execute(java)
    }

    @Override
    void java(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Java) Closure<Void> action) {
        ConfigureUtil.configure(action, java)
    }

    @Override
    void snapshot(Action<? super Snapshot> action) {
        action.execute(snapshot)
    }

    @Override
    void snapshot(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Snapshot) Closure<Void> action) {
        ConfigureUtil.configure(action, snapshot)
    }

    @Override
    void screenshot(Action<? super Screenshot> action) {
        action.execute(screenshots.maybeCreate("screenshot-${screenshots.size()}".toString()))
    }

    @Override
    void screenshot(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Screenshot) Closure<Void> action) {
        ConfigureUtil.configure(action, screenshots.maybeCreate("screenshot-${screenshots.size()}".toString()))
    }

    org.jreleaser.model.Project toModel() {
        org.jreleaser.model.Project project = new org.jreleaser.model.Project()
        project.name = name.get()
        project.version = version.get()
        if (versionPattern.present) project.versionPattern = versionPattern.get()
        if (description.present) project.description = description.get()
        if (longDescription.present) project.longDescription = longDescription.get()
        if (website.present) project.links.homepage = website.get()
        if (license.present) project.license = license.get()
        if (inceptionYear.present) project.inceptionYear = inceptionYear.get()
        if (licenseUrl.present) project.links.license = licenseUrl.get()
        if (copyright.present) project.copyright = copyright.get()
        if (vendor.present) project.vendor = vendor.get()
        if (docsUrl.present) project.links.documentation = docsUrl.get()
        if (stereotype.present) project.stereotype = stereotype.get()
        project.authors = (List<String>) authors.getOrElse([])
        project.tags = (List<String>) tags.getOrElse([])
        project.maintainers = (List<String>) maintainers.getOrElse([])
        if (extraProperties.present) project.extraProperties.putAll(extraProperties.get())
        project.java = java.toModel()
        project.snapshot = snapshot.toModel()
        project.links = links.toModel()
        for (ScreenshotImpl screenshot : screenshots) {
            project.addScreenshot(screenshot.toModel())
        }
        project
    }

    @CompileStatic
    static class SnapshotImpl implements Snapshot {
        final Property<String> pattern
        final Property<String> label
        final Property<Boolean> fullChangelog

        @Inject
        SnapshotImpl(ObjectFactory objects) {
            pattern = objects.property(String).convention(Providers.notDefined())
            label = objects.property(String).convention(Providers.notDefined())
            fullChangelog = objects.property(Boolean).convention(Providers.notDefined())
        }

        @Internal
        boolean isSet() {
            pattern.present ||
                label.present ||
                fullChangelog.present
        }

        org.jreleaser.model.Project.Snapshot toModel() {
            org.jreleaser.model.Project.Snapshot snapshot = new org.jreleaser.model.Project.Snapshot()
            if (pattern.present) snapshot.pattern = pattern.get()
            if (label.present) snapshot.label = label.get()
            if (fullChangelog.present) snapshot.fullChangelog = fullChangelog.get()
            snapshot
        }
    }


    @CompileStatic
    static class LinksImpl implements Links {
        final Property<String> homepage
        final Property<String> documentation
        final Property<String> license
        final Property<String> bugTracker
        final Property<String> faq
        final Property<String> help
        final Property<String> donation
        final Property<String> translate
        final Property<String> contact
        final Property<String> vcsBrowser
        final Property<String> contribute

        @Inject
        LinksImpl(ObjectFactory objects) {
            homepage = objects.property(String).convention(Providers.notDefined())
            documentation = objects.property(String).convention(Providers.notDefined())
            license = objects.property(String).convention(Providers.notDefined())
            bugTracker = objects.property(String).convention(Providers.notDefined())
            faq = objects.property(String).convention(Providers.notDefined())
            help = objects.property(String).convention(Providers.notDefined())
            donation = objects.property(String).convention(Providers.notDefined())
            translate = objects.property(String).convention(Providers.notDefined())
            contact = objects.property(String).convention(Providers.notDefined())
            vcsBrowser = objects.property(String).convention(Providers.notDefined())
            contribute = objects.property(String).convention(Providers.notDefined())
        }

        @Internal
        boolean isSet() {
            homepage.present ||
                documentation.present ||
                license.present ||
                bugTracker.present ||
                faq.present ||
                help.present ||
                donation.present ||
                translate.present ||
                contact.present ||
                vcsBrowser.present ||
                contribute.present
        }

        org.jreleaser.model.Project.Links toModel() {
            org.jreleaser.model.Project.Links links = new org.jreleaser.model.Project.Links()
            if (homepage.present) links.homepage = homepage.get()
            if (documentation.present) links.documentation = documentation.get()
            if (license.present) links.license = license.get()
            if (bugTracker.present) links.bugTracker = bugTracker.get()
            if (faq.present) links.faq = faq.get()
            if (help.present) links.help = help.get()
            if (donation.present) links.donation = donation.get()
            if (translate.present) links.translate = translate.get()
            if (contact.present) links.contact = contact.get()
            if (vcsBrowser.present) links.vcsBrowser = vcsBrowser.get()
            if (contribute.present) links.contribute = contribute.get()
            links
        }
    }
}

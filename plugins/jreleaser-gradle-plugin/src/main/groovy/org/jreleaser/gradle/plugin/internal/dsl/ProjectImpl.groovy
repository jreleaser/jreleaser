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
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.jreleaser.gradle.plugin.dsl.Java
import org.jreleaser.gradle.plugin.dsl.Project
import org.jreleaser.model.VersionPattern
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
    final Property<VersionPattern> versionPattern
    final Property<String> snapshotPattern
    final Property<String> description
    final Property<String> longDescription
    final Property<String> website
    final Property<String> license
    final Property<String> copyright
    final Property<String> vendor
    final Property<String> docsUrl
    final ListProperty<String> authors
    final ListProperty<String> tags
    final MapProperty<String, Object> extraProperties
    final JavaImpl java

    @Inject
    ProjectImpl(ObjectFactory objects,
                Provider<String> nameProvider,
                Provider<String> descriptionProvider,
                Provider<String> versionProvider) {
        name = objects.property(String).convention(nameProvider)
        version = objects.property(String).convention(versionProvider)
        versionPattern = objects.property(VersionPattern).convention(Providers.notDefined())
        snapshotPattern = objects.property(String).convention(Providers.notDefined())
        description = objects.property(String).convention(descriptionProvider)
        longDescription = objects.property(String).convention(descriptionProvider)
        website = objects.property(String).convention(Providers.notDefined())
        license = objects.property(String).convention(Providers.notDefined())
        copyright = objects.property(String).convention(Providers.notDefined())
        vendor = objects.property(String).convention(Providers.notDefined())
        docsUrl = objects.property(String).convention(Providers.notDefined())
        authors = objects.listProperty(String).convention(Providers.notDefined())
        tags = objects.listProperty(String).convention(Providers.notDefined())
        extraProperties = objects.mapProperty(String, Object).convention(Providers.notDefined())

        java = objects.newInstance(JavaImpl, objects)
    }

    @Override
    void setVersionPattern(String str) {
        if (isNotBlank(str)) {
            versionPattern.set(VersionPattern.of(str.trim()))
        }
    }

    @Override
    void addAuthor(String name) {
        if (isNotBlank(name)) {
            authors.add(name.trim())
        }
    }

    @Override
    void addTag(String tag) {
        if (isNotBlank(tag)) {
            tags.add(tag.trim())
        }
    }

    @Override
    void java(Action<? super Java> action) {
        action.execute(java)
    }

    @Override
    void java(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Java) Closure<Void> action) {
        ConfigureUtil.configure(action, java)
    }

    org.jreleaser.model.Project toModel() {
        org.jreleaser.model.Project project = new org.jreleaser.model.Project()
        project.name = name.get()
        project.version = version.get()
        if (versionPattern.present) project.versionPattern = versionPattern.get()
        if (snapshotPattern.present) project.snapshotPattern = snapshotPattern.get()
        if (description.present) project.description = description.get()
        if (longDescription.present) project.longDescription = longDescription.get()
        if (website.present) project.website = website.get()
        if (license.present) project.license = license.get()
        if (copyright.present) project.copyright = copyright.get()
        if (vendor.present) project.vendor = vendor.get()
        if (docsUrl.present) project.docsUrl = docsUrl.get()
        project.authors = (List<String>) authors.getOrElse([])
        project.tags = (List<String>) tags.getOrElse([])
        if (extraProperties.present) project.extraProperties.putAll(extraProperties.get())
        project.java = java.toModel()
        project
    }
}

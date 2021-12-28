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
import org.gradle.api.internal.provider.Providers
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.jreleaser.gradle.plugin.dsl.Java

import javax.inject.Inject

/**
 *
 * @author Andres Almiray
 * @since 0.1.0
 */
@CompileStatic
class JavaImpl implements Java {
    final Property<String> version
    final Property<String> groupId
    final Property<String> artifactId
    final Property<String> mainClass
    final Property<Boolean> multiProject
    final MapProperty<String, Object> extraProperties

    @Inject
    JavaImpl(ObjectFactory objects) {
        version = objects.property(String).convention(Providers.notDefined())
        groupId = objects.property(String).convention(Providers.notDefined())
        artifactId = objects.property(String).convention(Providers.notDefined())
        mainClass = objects.property(String).convention(Providers.notDefined())
        multiProject = objects.property(Boolean).convention(Providers.notDefined())
        extraProperties = objects.mapProperty(String, Object).convention(Providers.notDefined())
    }

    @Internal
    boolean isSet() {
        version.present ||
            groupId.present ||
            artifactId.present ||
            mainClass.present ||
            multiProject.present ||
            extraProperties.present
    }

    org.jreleaser.model.Java toModel() {
        org.jreleaser.model.Java java = new org.jreleaser.model.Java()
        java.enabled = true
        if (version.present) java.version = version.get()
        if (groupId.present) java.groupId = groupId.get()
        if (artifactId.present) java.artifactId = artifactId.get()
        if (mainClass.present) java.mainClass = mainClass.get()
        if (multiProject.present) java.multiProject = multiProject.get()
        if (extraProperties.present) java.extraProperties.putAll(extraProperties.get())
        java
    }
}

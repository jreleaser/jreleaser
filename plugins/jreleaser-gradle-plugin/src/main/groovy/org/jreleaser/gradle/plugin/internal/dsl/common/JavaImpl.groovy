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
package org.jreleaser.gradle.plugin.internal.dsl.common

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.internal.provider.Providers
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.Internal
import org.jreleaser.gradle.plugin.dsl.common.EnvironmentVariables
import org.jreleaser.gradle.plugin.dsl.common.Java
import org.jreleaser.gradle.plugin.dsl.common.JvmOptions
import org.kordamp.gradle.util.ConfigureUtil

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
    final Property<String> mainModule
    final Property<String> mainClass
    final Property<Boolean> multiProject
    final SetProperty<String> options
    final MapProperty<String, Object> extraProperties
    final JvmOptionsImpl jvmOptions
    final EnvironmentVariablesImpl environmentVariables

    @Inject
    JavaImpl(ObjectFactory objects) {
        version = objects.property(String).convention(Providers.<String> notDefined())
        groupId = objects.property(String).convention(Providers.<String> notDefined())
        artifactId = objects.property(String).convention(Providers.<String> notDefined())
        mainModule = objects.property(String).convention(Providers.<String> notDefined())
        mainClass = objects.property(String).convention(Providers.<String> notDefined())
        multiProject = objects.property(Boolean).convention(Providers.<Boolean> notDefined())
        options = objects.setProperty(String).convention(Providers.<Set<String>> notDefined())
        extraProperties = objects.mapProperty(String, Object).convention(Providers.notDefined())
        jvmOptions = objects.newInstance(JvmOptionsImpl, objects)
        environmentVariables = objects.newInstance(EnvironmentVariablesImpl, objects)
    }

    @Internal
    boolean isSet() {
        version.present ||
            groupId.present ||
            artifactId.present ||
            mainModule.present ||
            mainClass.present ||
            multiProject.present ||
            options.present ||
            extraProperties.present ||
            jvmOptions.isSet() ||
            environmentVariables.isSet()
    }

    @Override
    void jvmOptions(Action<? super JvmOptions> action) {
        action.execute(jvmOptions)
    }

    @Override
    void environmentVariables(Action<? super EnvironmentVariables> action) {
        action.execute(environmentVariables)
    }

    @Override
    @CompileDynamic
    void jvmOptions(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = JvmOptions) Closure<Void> action) {
        ConfigureUtil.configure(action, jvmOptions)
    }

    @Override
    @CompileDynamic
    void environmentVariables(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = EnvironmentVariables) Closure<Void> action) {
        ConfigureUtil.configure(action, environmentVariables)
    }

    org.jreleaser.model.internal.common.Java toModel() {
        org.jreleaser.model.internal.common.Java java = new org.jreleaser.model.internal.common.Java()
        java.enabled = true
        if (version.present) java.version = version.get()
        if (groupId.present) java.groupId = groupId.get()
        if (artifactId.present) java.artifactId = artifactId.get()
        if (mainModule.present) java.mainModule = mainModule.get()
        if (mainClass.present) java.mainClass = mainClass.get()
        if (multiProject.present) java.multiProject = multiProject.get()
        if (extraProperties.present) java.extraProperties.putAll(extraProperties.get())
        options.getOrElse([] as Set<String>).forEach { option -> jvmOptions.universal(option) }
        java.jvmOptions = jvmOptions.toModel()
        java.environmentVariables = environmentVariables.toModel()
        java
    }
}

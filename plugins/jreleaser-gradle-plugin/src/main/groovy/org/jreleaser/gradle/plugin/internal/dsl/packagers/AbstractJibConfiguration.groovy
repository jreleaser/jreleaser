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
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.internal.provider.Providers
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.Internal
import org.jreleaser.gradle.plugin.dsl.packagers.JibConfiguration
import org.jreleaser.gradle.plugin.internal.dsl.common.CommitAuthorImpl
import org.jreleaser.model.Active
import org.kordamp.gradle.util.ConfigureUtil

import javax.inject.Inject

import static org.jreleaser.util.StringUtils.isNotBlank

/**
 *
 * @author Andres Almiray
 * @since 1.6.0
 */
@CompileStatic
abstract class AbstractJibConfiguration implements JibConfiguration {
    final Property<Active> active
    final DirectoryProperty templateDirectory
    final ListProperty<String> skipTemplates
    final MapProperty<String, Object> extraProperties
    final Property<String> baseImage
    final Property<String> creationTime
    final Property<String> user
    final Property<String> workingDirectory
    final Property<org.jreleaser.model.api.packagers.JibConfiguration.Format> format
    final SetProperty<String> imageNames
    final SetProperty<String> volumes
    final SetProperty<String> exposedPorts
    final MapProperty<String, String> environment
    final MapProperty<String, String> labels

    final NamedDomainObjectContainer<RegistryImpl> registries

    @Inject
    AbstractJibConfiguration(ObjectFactory objects) {
        active = objects.property(Active).convention(Providers.<Active> notDefined())
        templateDirectory = objects.directoryProperty().convention(Providers.notDefined())
        skipTemplates = objects.listProperty(String).convention(Providers.<List<String>> notDefined())
        extraProperties = objects.mapProperty(String, Object).convention(Providers.notDefined())
        baseImage = objects.property(String).convention(Providers.<String> notDefined())
        creationTime = objects.property(String).convention(Providers.<String> notDefined())
        user = objects.property(String).convention(Providers.<String> notDefined())
        workingDirectory = objects.property(String).convention(Providers.<String> notDefined())
        format = objects.property(org.jreleaser.model.api.packagers.JibConfiguration.Format).convention(Providers.<org.jreleaser.model.api.packagers.JibConfiguration.Format> notDefined())
        imageNames = objects.setProperty(String).convention(Providers.<Set<String>> notDefined())
        volumes = objects.setProperty(String).convention(Providers.<Set<String>> notDefined())
        exposedPorts = objects.setProperty(String).convention(Providers.<Set<String>> notDefined())
        environment = objects.mapProperty(String, String).convention(Providers.notDefined())
        labels = objects.mapProperty(String, String).convention(Providers.notDefined())

        registries = objects.domainObjectContainer(RegistryImpl)
    }

    @Override
    void setTemplateDirectory(String templateDirectory) {
        this.templateDirectory.set(new File(templateDirectory))
    }

    @Override
    void setActive(String str) {
        if (isNotBlank(str)) {
            active.set(Active.of(str.trim()))
        }
    }

    @Override
    void setFormat(String str) {
        if (isNotBlank(str)) {
            this.format.set(org.jreleaser.model.api.packagers.JibConfiguration.Format.of(str.trim()))
        }
    }

    @Override
    void environment(String key, String value) {
        if (isNotBlank(key) && isNotBlank(value)) {
            environment.put(key.trim(), value.trim())
        }
    }

    @Override
    void label(String key, String value) {
        if (isNotBlank(key) && isNotBlank(value)) {
            labels.put(key.trim(), value.trim())
        }
    }

    @Override
    void imageName(String imageName) {
        if (isNotBlank(imageName)) {
            imageNames.add(imageName)
        }
    }

    @Override
    void volume(String volume) {
        if (isNotBlank(volume)) {
            volumes.add(volume)
        }
    }

    @Override
    void exposedPort(String exposedPort) {
        if (isNotBlank(exposedPort)) {
            exposedPorts.add(exposedPort)
        }
    }

    @Override
    void registries(Action<? super NamedDomainObjectContainer<? extends Registry>> action) {
        action.execute(registries)
    }

    @Override
    @CompileDynamic
    void registries(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = NamedDomainObjectContainer) Closure<Void> action) {
        ConfigureUtil.configure(action, registries)
    }

    @Internal
    boolean isSet() {
        active.present ||
            templateDirectory.present ||
            skipTemplates.present ||
            extraProperties.present ||
            baseImage.present ||
            creationTime.present ||
            user.present ||
            workingDirectory.present ||
            format.present ||
            imageNames.present ||
            volumes.present ||
            exposedPorts.present ||
            environment.present ||
            labels.present ||
            registries.size()
    }

    void skipTemplate(String template) {
        if (isNotBlank(template)) {
            skipTemplates.add(template.trim())
        }
    }

    void toModel(org.jreleaser.model.internal.packagers.JibConfiguration jib) {
        if (active.present) jib.active = active.get()
        if (templateDirectory.present) {
            jib.templateDirectory = templateDirectory.get().asFile.toPath().toAbsolutePath().toString()
        }
        jib.skipTemplates = (List<String>) skipTemplates.getOrElse([])
        if (extraProperties.present) jib.extraProperties.putAll(extraProperties.get())
        if (baseImage.present) jib.baseImage = baseImage.get()
        if (creationTime.present) jib.creationTime = creationTime.get()
        if (user.present) jib.user = user.get()
        if (workingDirectory.present) jib.workingDirectory = workingDirectory.get()
        if (format.present) jib.format = format.get()
        if (environment.present) jib.environment.putAll(environment.get())
        if (labels.present) jib.labels.putAll(labels.get())
        if (imageNames.present) jib.imageNames = (imageNames.get() as Set<String>)
        if (volumes.present) jib.volumes = (volumes.get() as Set<String>)
        if (exposedPorts.present) jib.exposedPorts = (exposedPorts.get() as Set<String>)
        for (RegistryImpl registry : registries) {
            jib.addRegistry(registry.toModel())
        }
    }

    @CompileStatic
    static class RegistryImpl implements Registry {
        final String name
        final Property<String> server
        final Property<String> username
        final Property<String> toUsername
        final Property<String> fromUsername
        final Property<String> password
        final Property<String> toPassword
        final Property<String> fromPassword

        @Inject
        RegistryImpl(String name, ObjectFactory objects) {
            this.name = name
            server = objects.property(String).convention(Providers.<String> notDefined())
            username = objects.property(String).convention(Providers.<String> notDefined())
            toUsername = objects.property(String).convention(Providers.<String> notDefined())
            fromUsername = objects.property(String).convention(Providers.<String> notDefined())
            password = objects.property(String).convention(Providers.<String> notDefined())
            toPassword = objects.property(String).convention(Providers.<String> notDefined())
            fromPassword = objects.property(String).convention(Providers.<String> notDefined())
        }

        org.jreleaser.model.internal.packagers.JibConfiguration.Registry toModel() {
            org.jreleaser.model.internal.packagers.JibConfiguration.Registry registry = new org.jreleaser.model.internal.packagers.JibConfiguration.Registry()
            registry.name = name
            if (server.present) registry.server = server.get()
            if (username.present) registry.username = username.get()
            if (toUsername.present) registry.toUsername = toUsername.get()
            if (fromUsername.present) registry.fromUsername = fromUsername.get()
            if (password.present) registry.password = password.get()
            if (toPassword.present) registry.toPassword = toPassword.get()
            if (fromPassword.present) registry.fromPassword = fromPassword.get()
            registry
        }
    }
}

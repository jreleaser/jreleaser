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
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.internal.provider.Providers
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.Internal
import org.jreleaser.gradle.plugin.dsl.DockerConfiguration
import org.jreleaser.gradle.plugin.dsl.Registry
import org.jreleaser.model.Active
import org.kordamp.gradle.util.ConfigureUtil

import javax.inject.Inject

import static org.jreleaser.util.StringUtils.isNotBlank

/**
 *
 * @author Andres Almiray
 * @since 0.4.0
 */
@CompileStatic
abstract class AbstractDockerConfiguration implements DockerConfiguration {
    final Property<Active> active
    final DirectoryProperty templateDirectory
    final ListProperty<String> skipTemplates
    final MapProperty<String, Object> extraProperties
    final Property<String> baseImage
    final Property<Boolean> useLocalArtifact
    final SetProperty<String> imageNames
    final ListProperty<String> buildArgs
    final ListProperty<String> preCommands
    final ListProperty<String> postCommands
    final MapProperty<String, String> labels

    final NamedDomainObjectContainer<RegistryImpl> registries

    @Inject
    AbstractDockerConfiguration(ObjectFactory objects) {
        active = objects.property(Active).convention(Providers.notDefined())
        templateDirectory = objects.directoryProperty().convention(Providers.notDefined())
        skipTemplates = objects.listProperty(String).convention(Providers.notDefined())
        extraProperties = objects.mapProperty(String, Object).convention(Providers.notDefined())
        baseImage = objects.property(String).convention(Providers.notDefined())
        useLocalArtifact = objects.property(Boolean).convention(Providers.notDefined())
        imageNames = objects.setProperty(String).convention(Providers.notDefined())
        buildArgs = objects.listProperty(String).convention(Providers.notDefined())
        preCommands = objects.listProperty(String).convention(Providers.notDefined())
        postCommands = objects.listProperty(String).convention(Providers.notDefined())
        labels = objects.mapProperty(String, String).convention(Providers.notDefined())

        registries = objects.domainObjectContainer(RegistryImpl)
    }

    @Override
    void setActive(String str) {
        if (isNotBlank(str)) {
            active.set(Active.of(str.trim()))
        }
    }

    @Override
    void addLabel(String key, String value) {
        if (isNotBlank(key) && isNotBlank(value)) {
            labels.put(key.trim(), value.trim())
        }
    }

    @Override
    void addImageName(String imageName) {
        if (isNotBlank(imageName)) {
            imageNames.add(imageName.trim())
        }
    }

    @Override
    void addBuildArg(String buildArg) {
        if (isNotBlank(buildArg)) {
            buildArgs.add(buildArg.trim())
        }
    }

    @Override
    void addPreCommand(String command) {
        if (isNotBlank(command)) {
            preCommands.add(command.trim())
        }
    }

    @Override
    void addPostCommand(String command) {
        if (isNotBlank(command)) {
            postCommands.add(command.trim())
        }
    }

    @Override
    void registries(Action<? super NamedDomainObjectContainer<? extends Registry>> action) {
        action.execute(registries)
    }

    @Override
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
            useLocalArtifact.present ||
            imageNames.present ||
            buildArgs.present ||
            preCommands.present ||
            postCommands.present ||
            labels.present ||
            registries.size()
    }


    void skipTemplate(String template) {
        if (isNotBlank(template)) {
            skipTemplates.add(template.trim())
        }
    }

    void toModel(org.jreleaser.model.DockerConfiguration docker) {
        if (active.present) docker.active = active.get()
        if (templateDirectory.present) {
            docker.templateDirectory = templateDirectory.get().asFile.toPath().toAbsolutePath().toString()
        }
        docker.skipTemplates = (List<String>) skipTemplates.getOrElse([])
        if (extraProperties.present) docker.extraProperties.putAll(extraProperties.get())
        if (baseImage.present) docker.baseImage = baseImage.get()
        if (useLocalArtifact.present) docker.useLocalArtifact = useLocalArtifact.get()
        if (imageNames.present) docker.imageNames.addAll(imageNames.get())
        if (buildArgs.present) docker.buildArgs.addAll(buildArgs.get())
        if (preCommands.present) docker.preCommands.addAll(preCommands.get())
        if (postCommands.present) docker.postCommands.addAll(postCommands.get())
        if (labels.present) docker.labels.putAll(labels.get())
        for (RegistryImpl registry : registries) {
            docker.addRegistry(registry.toModel())
        }
    }
}

/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2021 Andres Almiray.
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
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.file.Directory
import org.gradle.api.internal.provider.Providers
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.Internal
import org.jreleaser.gradle.plugin.dsl.Docker

import javax.inject.Inject

import static org.jreleaser.util.StringUtils.isNotBlank

/**
 *
 * @author Andres Almiray
 * @since 0.1.0
 */
@CompileStatic
class DockerImpl extends AbstractTool implements Docker {
    final Property<String> baseImage
    final SetProperty<String> imageNames
    final ListProperty<String> buildArgs
    final ListProperty<String> preCommands
    final ListProperty<String> postCommands
    final MapProperty<String, String> labels

    final NamedDomainObjectContainer<RegistryImpl> registries

    @Inject
    DockerImpl(ObjectFactory objects, Provider<Directory> distributionsDirProvider) {
        super(objects, distributionsDirProvider)
        baseImage = objects.property(String).convention(Providers.notDefined())
        imageNames = objects.setProperty(String).convention(Providers.notDefined())
        buildArgs = objects.listProperty(String).convention(Providers.notDefined())
        preCommands = objects.listProperty(String).convention(Providers.notDefined())
        postCommands = objects.listProperty(String).convention(Providers.notDefined())
        labels = objects.mapProperty(String, String).convention(Providers.notDefined())

        registries = objects.domainObjectContainer(RegistryImpl)
    }

    @Override
    protected String toolName() { 'docker' }

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
    @Internal
    boolean isSet() {
        super.isSet() ||
            baseImage.present ||
            imageNames.present ||
            buildArgs.present ||
            preCommands.present ||
            postCommands.present ||
            labels.present ||
            registries.size()
    }

    org.jreleaser.model.Docker toModel() {
        org.jreleaser.model.Docker tool = new org.jreleaser.model.Docker()
        fillToolProperties(tool)
        if (baseImage.present) tool.baseImage = baseImage.get()
        if (imageNames.present) tool.imageNames.addAll(imageNames.get())
        if (buildArgs.present) tool.buildArgs.addAll(buildArgs.get())
        if (preCommands.present) tool.preCommands.addAll(preCommands.get())
        if (postCommands.present) tool.postCommands.addAll(postCommands.get())
        if (labels.present) tool.labels.putAll(labels.get())
        for (RegistryImpl registry : registries) {
            tool.addRegistry(registry.toModel())
        }
        tool
    }
}

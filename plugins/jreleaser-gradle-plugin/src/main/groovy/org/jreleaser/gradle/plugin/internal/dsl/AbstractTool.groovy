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
import org.gradle.api.Transformer
import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.internal.provider.Providers
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Internal
import org.jreleaser.gradle.plugin.dsl.Tool

import javax.inject.Inject

/**
 *
 * @author Andres Almiray
 * @since 0.1.0
 */
@CompileStatic
abstract class AbstractTool implements Tool {
    final Property<Boolean> enabled
    final DirectoryProperty templateDirectory
    final MapProperty<String, Object> extraProperties
    final Property<String> distributionName

    private final Provider<Directory> distributionsDirProvider
    private final DirectoryProperty localTemplate

    @Inject
    AbstractTool(ObjectFactory objects, Provider<Directory> distributionsDirProvider) {
        this.distributionsDirProvider = distributionsDirProvider
        enabled = objects.property(Boolean).convention(Providers.notDefined())
        templateDirectory = objects.directoryProperty().convention(Providers.notDefined())
        localTemplate = objects.directoryProperty()
        extraProperties = objects.mapProperty(String, Object).convention(Providers.notDefined())
        distributionName = objects.property(String).convention(Providers.notDefined())

        // FIXME: there's probable a better way to do this
        Provider<Directory> dd = distributionsDirProvider.flatMap(new Transformer<Provider<? extends Directory>, Directory>() {
            @Override
            Provider<? extends Directory> transform(Directory tr) {
                return tr.dir(distributionName)
            }
        })
        Provider<Directory> td = dd.flatMap(new Transformer<Provider<? extends Directory>, Directory>() {
            @Override
            Provider<? extends Directory> transform(Directory tr) {
                return tr.dir(objects.property(String).convention(toolName()))
            }
        })

        localTemplate.set(td)
    }

    protected abstract String toolName()

    protected <T extends org.jreleaser.model.Tool> void fillToolProperties(T tool) {
        tool.enabled = enabled.getOrElse(isSet())
        if (templateDirectory.present) {
            tool.templateDirectory = templateDirectory.get().asFile.toPath()
        } else if (localTemplate.asFile.get().exists()) {
            tool.templateDirectory = localTemplate.asFile.get().toPath()
        }
        if (extraProperties.present) tool.extraProperties.putAll(extraProperties.get())
    }

    @Internal
    boolean isSet() {
        enabled.present ||
                templateDirectory.present ||
                extraProperties.present
    }
}

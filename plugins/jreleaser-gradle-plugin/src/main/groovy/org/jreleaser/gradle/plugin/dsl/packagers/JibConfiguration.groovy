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
package org.jreleaser.gradle.plugin.dsl.packagers

import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.jreleaser.gradle.plugin.dsl.common.Activatable
import org.jreleaser.gradle.plugin.dsl.common.ExtraProperties

/**
 *
 * @author Andres Almiray
 * @since 1.6.0
 */
@CompileStatic
interface JibConfiguration extends Activatable, ExtraProperties {
    DirectoryProperty getTemplateDirectory()

    void setTemplateDirectory(String templateDirectory)

    ListProperty<String> getSkipTemplates()

    void skipTemplate(String template)

    Property<String> getBaseImage()

    Property<String> getCreationTime()

    Property<String> getUser()

    Property<String> getWorkingDirectory()

    Property<org.jreleaser.model.api.packagers.JibConfiguration.Format> getFormat()

    MapProperty<String, String> getEnvironment()

    MapProperty<String, String> getLabels()

    SetProperty<String> getImageNames()

    SetProperty<String> getVolumes()

    SetProperty<String> getExposedPorts()

    NamedDomainObjectContainer<Registry> getRegistries()

    void registries(Action<? super NamedDomainObjectContainer<? extends Registry>> action)

    void environment(String key, String value)

    void label(String key, String value)

    void setFormat(String str)

    void imageName(String imageName)

    void volume(String volume)

    void exposedPort(String exposedPort)

    @CompileStatic
    interface Registry {
        Property<String> getServer()

        Property<String> getUsername()

        Property<String> getToUsername()

        Property<String> getFromUsername()

        Property<String> getPassword()

        Property<String> getToPassword()

        Property<String> getFromPassword()
    }
}
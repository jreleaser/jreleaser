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
 * @since 0.4.0
 */
@CompileStatic
interface DockerConfiguration extends Activatable, ExtraProperties {
    DirectoryProperty getTemplateDirectory()

    void setTemplateDirectory(String templateDirectory)

    ListProperty<String> getSkipTemplates()

    void skipTemplate(String template)

    Property<String> getBaseImage()

    Property<Boolean> getUseLocalArtifact()

    SetProperty<String> getImageNames()

    ListProperty<String> getBuildArgs()

    ListProperty<String> getPreCommands()

    ListProperty<String> getPostCommands()

    MapProperty<String, String> getLabels()

    void imageName(String imageName)

    void buildArg(String buildArg)

    void preCommand(String command)

    void postCommand(String command)

    void label(String key, String value)

    NamedDomainObjectContainer<Registry> getRegistries()

    void registries(Action<? super NamedDomainObjectContainer<? extends Registry>> action)

    void buildx(Action<? super Buildx> action)

    void registries(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = NamedDomainObjectContainer) Closure<Void> action)

    void buildx(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Buildx) Closure<Void> action)

    @CompileStatic
    interface Registry {
        Property<String> getServer()

        Property<String> getRepositoryName()

        Property<String> getUsername()

        Property<String> getPassword()

        Property<Boolean> getExternalLogin()
    }

    @CompileStatic
    interface Buildx {
        Property<Boolean> getEnabled()

        Property<Boolean> getCreateBuilder()

        ListProperty<String> getCreateBuilderFlags()

        ListProperty<String> getPlatforms()

        void createBuilderFlag(String createBuilderFlag)

        void platform(String platform)
    }
}
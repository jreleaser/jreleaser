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
package org.jreleaser.gradle.plugin.dsl

import groovy.transform.CompileStatic
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty

/**
 *
 * @author Andres Almiray
 * @since 0.4.0
 */
@CompileStatic
interface DockerConfiguration extends Activatable, ExtraProperties {
    DirectoryProperty getTemplateDirectory()

    Property<String> getBaseImage()

    Property<Boolean> getUseLocalArtifact()

    SetProperty<String> getImageNames()

    ListProperty<String> getBuildArgs()

    ListProperty<String> getPreCommands()

    ListProperty<String> getPostCommands()

    MapProperty<String, String> getLabels()

    void addImageName(String imageName)

    void addBuildArg(String buildArg)

    void addPreCommand(String command)

    void addPostCommand(String command)

    void addLabel(String key, String value)

    NamedDomainObjectContainer<Registry> getRegistries()
}
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
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.jreleaser.gradle.plugin.dsl.ArtifactoryRepository
import org.jreleaser.model.Active
import org.jreleaser.util.FileType

import javax.inject.Inject

import static org.jreleaser.util.StringUtils.isNotBlank

/**
 *
 * @author Andres Almiray
 * @since 0.10.0
 */
@CompileStatic
class ArtifactoryRepositoryImpl implements ArtifactoryRepository {
    String name
    final Property<Active> active
    final Property<String> path
    final SetProperty<FileType> fileTypes

    @Inject
    ArtifactoryRepositoryImpl(ObjectFactory objects) {
        active = objects.property(Active).convention(Providers.notDefined())
        path = objects.property(String).convention(Providers.notDefined())
        fileTypes = objects.setProperty(FileType).convention(Providers.notDefined())
    }

    @Override
    void setActive(String str) {
        if (isNotBlank(str)) {
            active.set(Active.of(str.trim()))
        }
    }

    @Override
    void setFileType(String str) {
        if (isNotBlank(str)) {
            fileTypes.add(FileType.of(str.trim()))
        }
    }

    org.jreleaser.model.ArtifactoryRepository toModel() {
        org.jreleaser.model.ArtifactoryRepository repository = new org.jreleaser.model.ArtifactoryRepository()
        if (active.present) repository.active = active.get()
        if (path.present) repository.path = path.get()
        repository.fileTypes = (Set<FileType>) fileTypes.getOrElse([] as Set<FileType>)
        repository
    }
}

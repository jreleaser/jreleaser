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
package org.jreleaser.gradle.plugin.dsl.upload

import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.jreleaser.gradle.plugin.dsl.common.Activatable
import org.jreleaser.model.Http
import org.jreleaser.util.FileType

/**
 *
 * @author Andres Almiray
 * @since 0.3.0
 */
@CompileStatic
interface ArtifactoryUploader extends WebUploader {
    Property<String> getHost()

    Property<String> getUsername()

    Property<String> getPassword()

    Property<Http.Authorization> getAuthorization()

    void setAuthorization(String authorization)

    void repository(Action<? super ArtifactoryRepository> action)

    void repository(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = ArtifactoryRepository) Closure<Void> action)

    @CompileStatic
    interface ArtifactoryRepository extends Activatable {
        Property<String> getPath()

        SetProperty<FileType> getFileTypes()

        void setFileType(String str)
    }
}
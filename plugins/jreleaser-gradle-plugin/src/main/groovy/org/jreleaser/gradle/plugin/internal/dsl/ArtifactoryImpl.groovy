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
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.internal.provider.Providers
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.jreleaser.gradle.plugin.dsl.Artifactory

import javax.inject.Inject

/**
 *
 * @author Andres Almiray
 * @since 0.3.0
 */
@CompileStatic
class ArtifactoryImpl extends AbstractUploader implements Artifactory {
    String name
    final Property<String> target
    final Property<String> username
    final Property<String> password
    final Property<String> token

    @Inject
    ArtifactoryImpl(ObjectFactory objects) {
        super(objects)
        target = objects.property(String).convention(Providers.notDefined())
        username = objects.property(String).convention(Providers.notDefined())
        password = objects.property(String).convention(Providers.notDefined())
        token = objects.property(String).convention(Providers.notDefined())
    }

    @Override
    @Internal
    boolean isSet() {
        super.isSet() ||
            target.present ||
            username.present ||
            password.present ||
            token.present
    }

    org.jreleaser.model.Artifactory toModel() {
        org.jreleaser.model.Artifactory artifactory = new org.jreleaser.model.Artifactory()
        artifactory.name = name
        fillProperties(artifactory)
        if (target.present) artifactory.target = target.get()
        if (username.present) artifactory.username = username.get()
        if (password.present) artifactory.password = password.get()
        if (token.present) artifactory.token = token.get()
        artifactory
    }
}

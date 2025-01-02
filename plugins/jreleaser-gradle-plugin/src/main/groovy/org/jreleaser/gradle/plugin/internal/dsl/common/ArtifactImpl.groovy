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
package org.jreleaser.gradle.plugin.internal.dsl.common

import groovy.transform.CompileStatic
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.internal.provider.Providers
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.jreleaser.gradle.plugin.dsl.common.Artifact
import org.jreleaser.model.Active

import javax.inject.Inject

import static org.jreleaser.util.StringUtils.isNotBlank

/**
 *
 * @author Andres Almiray
 * @since 0.1.0
 */
@CompileStatic
class ArtifactImpl implements Artifact {
    String name
    final Property<Active> active
    final RegularFileProperty path
    final Property<String> transform
    final Property<String> platform
    final MapProperty<String, Object> extraProperties

    @Inject
    ArtifactImpl(ObjectFactory objects) {
        active = objects.property(Active).convention(Providers.<Active> notDefined())
        path = objects.fileProperty().convention(Providers.notDefined())
        platform = objects.property(String).convention(Providers.<String> notDefined())
        transform = objects.property(String).convention(Providers.<String> notDefined())
        extraProperties = objects.mapProperty(String, Object).convention(Providers.notDefined())
    }

    @Override
    void setActive(String str) {
        if (isNotBlank(str)) {
            active.set(Active.of(str.trim()))
        }
    }

    @Override
    void setPath(String path) {
        this.path.set(new File(path))
    }

    boolean isSet() {
        path.present
    }

    org.jreleaser.model.internal.common.Artifact toModel() {
        org.jreleaser.model.internal.common.Artifact artifact = new org.jreleaser.model.internal.common.Artifact()
        if (active.present) artifact.active = active.get()
        if (path.present) {
            artifact.path = path.asFile.get().absolutePath
        } else {
            throw new IllegalArgumentException("Artifact ${name} requires a value for 'path'")
        }
        if (transform.present) artifact.transform = transform.get()
        if (platform.present) artifact.platform = platform.get()
        if (extraProperties.present) artifact.extraProperties.putAll(extraProperties.get())
        artifact
    }
}

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
package org.kordamp.jreleaser.gradle.plugin.internal.dsl

import groovy.transform.CompileStatic
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.internal.provider.Providers
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.kordamp.jreleaser.gradle.plugin.dsl.Changelog

import javax.inject.Inject

/**
 *
 * @author Andres Almiray
 * @since 0.1.0
 */
@CompileStatic
class ChangelogImpl implements Changelog {
    final Property<Boolean> enabled
    final Property<org.kordamp.jreleaser.model.Changelog.Sort> sort
    final RegularFileProperty external

    @Inject
    ChangelogImpl(ObjectFactory objects) {
        enabled = objects.property(Boolean).convention(Providers.notDefined())
        sort = objects.property(org.kordamp.jreleaser.model.Changelog.Sort)
            .convention(org.kordamp.jreleaser.model.Changelog.Sort.ASC)
        external = objects.fileProperty().convention(Providers.notDefined())
    }

    @Internal
    boolean isSet() {
        enabled.present
    }

    @Override
    void setSort(String sort) {
        this.sort.set(org.kordamp.jreleaser.model.Changelog.Sort.valueOf(sort.toUpperCase()))
    }

    org.kordamp.jreleaser.model.Changelog toModel() {
        org.kordamp.jreleaser.model.Changelog changelog = new org.kordamp.jreleaser.model.Changelog()
        if (enabled.present) changelog.enabled = enabled.get()
        if (sort.present) changelog.sort = sort.get()
        if (external.present) changelog.external = external.getAsFile().get()
        changelog
    }
}

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
package org.jreleaser.gradle.plugin.internal.dsl.catalog.swid

import groovy.transform.CompileStatic
import org.gradle.api.internal.provider.Providers
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.Internal
import org.jreleaser.gradle.plugin.dsl.catalog.swid.Entity

import javax.inject.Inject

import static org.jreleaser.util.StringUtils.isNotBlank

/**
 *
 * @author Andres Almiray
 * @since 1.11.0
 */
@CompileStatic
class EntityImpl implements Entity {
    final Property<String> name
    final Property<String> regid
    final SetProperty<String> roles

    @Inject
    EntityImpl(ObjectFactory objects) {
        name = objects.property(String).convention(Providers.<String> notDefined())
        regid = objects.property(String).convention(Providers.<String> notDefined())
        roles = objects.setProperty(String).convention(Providers.<Set<String>> notDefined())
    }

    @Internal
    boolean isSet() {
        name.present ||
            regid.present ||
            roles.present
    }

    @Override
    void role(String role) {
        if (isNotBlank(role)) {
            roles.add(role.trim())
        }
    }

    org.jreleaser.model.internal.catalog.swid.Entity toModel() {
        org.jreleaser.model.internal.catalog.swid.Entity entity = new org.jreleaser.model.internal.catalog.swid.Entity()
        if (name.present) entity.name = name.get()
        if (regid.present) entity.regid = regid.get()
        entity.roles = (Set<String>) roles.getOrElse([] as Set<String>)
        entity
    }
}

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
import org.gradle.api.tasks.Internal
import org.jreleaser.gradle.plugin.dsl.CommitAuthor

import javax.inject.Inject

/**
 *
 * @author Andres Almiray
 * @since 0.1.0
 */
@CompileStatic
class CommitAuthorImpl implements CommitAuthor {
    final Property<String> name
    final Property<String> email

    @Inject
    CommitAuthorImpl(ObjectFactory objects) {
        name = objects.property(String).convention(Providers.notDefined())
        email = objects.property(String).convention(Providers.notDefined())
    }

    @Internal
    boolean isSet() {
        name.present ||
            email.present
    }

    org.jreleaser.model.CommitAuthor toModel() {
        org.jreleaser.model.CommitAuthor ca = new org.jreleaser.model.CommitAuthor()
        if (name.present) ca.name = name.get()
        if (email.present) ca.email = email.get()
        ca
    }
}

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
import org.jreleaser.gradle.plugin.dsl.DockerRepository
import org.jreleaser.model.Active

import javax.inject.Inject

import static org.jreleaser.util.StringUtils.isNotBlank

/**
 *
 * @author Andres Almiray
 * @since 0.9.0
 */
@CompileStatic
class DockerRepositoryImpl implements DockerRepository {
    final Property<Active> active
    final Property<String> owner
    final Property<String> name
    final Property<String> branch
    final Property<String> username
    final Property<String> token
    final Property<String> commitMessage
    final Property<Boolean> versionedSubfolders

    @Inject
    DockerRepositoryImpl(ObjectFactory objects) {
        active = objects.property(Active).convention(Providers.notDefined())
        owner = objects.property(String).convention(Providers.notDefined())
        name = objects.property(String).convention(Providers.notDefined())
        branch = objects.property(String).convention(Providers.notDefined())
        username = objects.property(String).convention(Providers.notDefined())
        token = objects.property(String).convention(Providers.notDefined())
        commitMessage = objects.property(String).convention(Providers.notDefined())
        versionedSubfolders = objects.property(Boolean).convention(Providers.notDefined())
    }

    @Override
    void setActive(String str) {
        if (isNotBlank(str)) {
            active.set(Active.of(str.trim()))
        }
    }

    @Internal
    boolean isSet() {
        active.present ||
            owner.present ||
            name.present ||
            branch.present ||
            username.present ||
            versionedSubfolders.present ||
            token.present ||
            commitMessage.present
    }

    org.jreleaser.model.DockerRepository toModel() {
        org.jreleaser.model.DockerRepository tap = new org.jreleaser.model.DockerRepository()
        if (active.present) tap.active = active.get()
        if (owner.present) tap.owner = owner.get()
        if (name.present) tap.name = name.get()
        if (branch.present) tap.branch = branch.get()
        if (username.present) tap.name = username.get()
        if (token.present) tap.token = token.get()
        if (commitMessage.present) tap.commitMessage = commitMessage.get()
        if (versionedSubfolders.present) tap.versionedSubfolders = versionedSubfolders.get()
        tap
    }
}

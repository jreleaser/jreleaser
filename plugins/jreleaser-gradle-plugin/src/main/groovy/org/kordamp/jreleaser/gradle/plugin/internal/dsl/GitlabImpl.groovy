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
import org.gradle.api.Action
import org.gradle.api.internal.provider.Providers
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.kordamp.jreleaser.gradle.plugin.dsl.Changelog
import org.kordamp.jreleaser.gradle.plugin.dsl.Gitlab

import javax.inject.Inject

/**
 *
 * @author Andres Almiray
 * @since 0.1.0
 */
@CompileStatic
class GitlabImpl extends AbstractGitService implements Gitlab {
    final Property<String> ref
    final ChangelogImpl changelog

    @Inject
    GitlabImpl(ObjectFactory objects) {
        super(objects)
        ref = objects.property(String).convention(Providers.notDefined())
        changelog = objects.newInstance(ChangelogImpl, objects)
    }

    @Override
    void changelog(Action<? super Changelog> action) {
        action.execute(changelog)
    }

    @Internal
    boolean isSet() {
        super.set ||
            ref.present ||
            changelog.set
    }

    org.kordamp.jreleaser.model.Gitlab toModel() {
        org.kordamp.jreleaser.model.Gitlab service = new org.kordamp.jreleaser.model.Gitlab()
        toModel(service)
        if (ref.present) service.ref = ref.get()
        if (changelog.set) service.changelog = changelog.toModel()
        service
    }
}

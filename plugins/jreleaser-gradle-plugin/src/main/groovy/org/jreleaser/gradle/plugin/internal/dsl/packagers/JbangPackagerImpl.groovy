/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2026 The JReleaser authors.
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
package org.jreleaser.gradle.plugin.internal.dsl.packagers


import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.internal.provider.Providers
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.jreleaser.gradle.plugin.dsl.common.CommitAuthor
import org.jreleaser.gradle.plugin.dsl.packagers.JbangPackager
import org.jreleaser.gradle.plugin.dsl.packagers.Tap
import org.jreleaser.gradle.plugin.internal.dsl.common.CommitAuthorImpl

import javax.inject.Inject

/**
 *
 * @author Andres Almiray
 * @since 0.1.0
 */
@CompileStatic
class JbangPackagerImpl extends AbstractRepositoryPackager implements JbangPackager {
    final Property<String> alias
    final CommitAuthorImpl commitAuthor
    final TapImpl repository

    @Inject
    JbangPackagerImpl(ObjectFactory objects) {
        super(objects)
        alias = objects.property(String).convention(Providers.<String> notDefined())
        repository = objects.newInstance(TapImpl, objects)
        commitAuthor = objects.newInstance(CommitAuthorImpl, objects)
    }

    @Override
    @Internal
    boolean isSet() {
        super.isSet() ||
            repository.isSet() ||
            commitAuthor.isSet()
    }

    @Override
    Tap getCatalog() {
        getRepository()
    }

    @Override
    void repository(Action<? super Tap> action) {
        action.execute(repository)
    }

    @Override
    void catalog(Action<? super Tap> action) {
        repository(action)
    }

    @Override
    void commitAuthor(Action<? super CommitAuthor> action) {
        action.execute(commitAuthor)
    }

    org.jreleaser.model.internal.packagers.JbangPackager toModel() {
        org.jreleaser.model.internal.packagers.JbangPackager packager = new org.jreleaser.model.internal.packagers.JbangPackager()
        fillPackagerProperties(packager)
        fillTemplatePackagerProperties(packager)
        if (alias.present) packager.alias = alias.get()
        if (repository.isSet()) packager.repository = repository.toJbangRepository()
        if (commitAuthor.isSet()) packager.commitAuthor = commitAuthor.toModel()
        packager
    }
}

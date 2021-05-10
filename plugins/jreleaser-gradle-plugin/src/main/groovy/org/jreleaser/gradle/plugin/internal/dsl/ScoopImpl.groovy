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
import org.gradle.api.Action
import org.gradle.api.internal.provider.Providers
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.jreleaser.gradle.plugin.dsl.CommitAuthor
import org.jreleaser.gradle.plugin.dsl.Scoop
import org.jreleaser.gradle.plugin.dsl.Tap

import javax.inject.Inject

/**
 *
 * @author Andres Almiray
 * @since 0.1.0
 */
@CompileStatic
class ScoopImpl extends AbstractRepositoryTool implements Scoop {
    final Property<String> checkverUrl
    final Property<String> autoupdateUrl
    final CommitAuthorImpl commitAuthor
    final TapImpl bucket

    @Inject
    ScoopImpl(ObjectFactory objects) {
        super(objects)
        checkverUrl = objects.property(String).convention(Providers.notDefined())
        autoupdateUrl = objects.property(String).convention(Providers.notDefined())
        bucket = objects.newInstance(TapImpl, objects)
        commitAuthor = objects.newInstance(CommitAuthorImpl, objects)
    }

    @Override
    @Internal
    boolean isSet() {
        super.isSet() ||
            checkverUrl.present ||
            autoupdateUrl.present ||
            bucket.isSet() ||
            commitAuthor.isSet()
    }

    @Override
    void bucket(Action<? super Tap> action) {
        action.execute(bucket)
    }

    @Override
    void commitAuthor(Action<? super CommitAuthor> action) {
        action.execute(commitAuthor)
    }

    org.jreleaser.model.Scoop toModel() {
        org.jreleaser.model.Scoop tool = new org.jreleaser.model.Scoop()
        fillToolProperties(tool)
        if (bucket.isSet()) tool.bucket = bucket.toScoopBucket()
        if (commitAuthor.isSet()) tool.commitAuthor = commitAuthor.toModel()
        if (checkverUrl.present) tool.checkverUrl = checkverUrl.get()
        if (autoupdateUrl.present) tool.autoupdateUrl = autoupdateUrl.get()
        tool
    }
}

/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2023 The JReleaser authors.
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

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.internal.provider.Providers
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.jreleaser.gradle.plugin.dsl.common.CommitAuthor
import org.jreleaser.gradle.plugin.dsl.packagers.ScoopPackager
import org.jreleaser.gradle.plugin.dsl.packagers.Tap
import org.jreleaser.gradle.plugin.internal.dsl.common.CommitAuthorImpl
import org.kordamp.gradle.util.ConfigureUtil

import javax.inject.Inject

/**
 *
 * @author Andres Almiray
 * @since 0.1.0
 */
@CompileStatic
class ScoopPackagerImpl extends AbstractRepositoryPackager implements ScoopPackager {
    final Property<String> packageName
    final Property<String> checkverUrl
    final Property<String> autoupdateUrl
    final CommitAuthorImpl commitAuthor
    final TapImpl bucket

    @Inject
    ScoopPackagerImpl(ObjectFactory objects) {
        super(objects)
        packageName = objects.property(String).convention(Providers.<String> notDefined())
        checkverUrl = objects.property(String).convention(Providers.<String> notDefined())
        autoupdateUrl = objects.property(String).convention(Providers.<String> notDefined())
        bucket = objects.newInstance(TapImpl, objects)
        commitAuthor = objects.newInstance(CommitAuthorImpl, objects)
    }

    @Override
    @Internal
    boolean isSet() {
        super.isSet() ||
            packageName.present ||
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

    @Override
    @CompileDynamic
    void bucket(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Tap) Closure<Void> action) {
        ConfigureUtil.configure(action, bucket)
    }

    @Override
    @CompileDynamic
    void commitAuthor(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = CommitAuthor) Closure<Void> action) {
        ConfigureUtil.configure(action, commitAuthor)
    }

    org.jreleaser.model.internal.packagers.ScoopPackager toModel() {
        org.jreleaser.model.internal.packagers.ScoopPackager packager = new org.jreleaser.model.internal.packagers.ScoopPackager()
        fillPackagerProperties(packager)
        fillTemplatePackagerProperties(packager)
        if (packageName.present) packager.packageName = packageName.get()
        if (bucket.isSet()) packager.bucket = bucket.toScoopBucket()
        if (commitAuthor.isSet()) packager.commitAuthor = commitAuthor.toModel()
        if (checkverUrl.present) packager.checkverUrl = checkverUrl.get()
        if (autoupdateUrl.present) packager.autoupdateUrl = autoupdateUrl.get()
        packager
    }
}

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
import org.gradle.api.file.Directory
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Internal
import org.jreleaser.gradle.plugin.dsl.CommitAuthor
import org.jreleaser.gradle.plugin.dsl.Jbang
import org.jreleaser.gradle.plugin.dsl.Tap

import javax.inject.Inject

/**
 *
 * @author Andres Almiray
 * @since 0.1.0
 */
@CompileStatic
class JbangImpl extends AbstractTool implements Jbang {
    final CommitAuthorImpl commitAuthor
    final TapImpl catalog

    @Inject
    JbangImpl(ObjectFactory objects, Provider<Directory> distributionsDirProvider) {
        super(objects, distributionsDirProvider)
        catalog = objects.newInstance(TapImpl, objects)
        commitAuthor = objects.newInstance(CommitAuthorImpl, objects)
    }

    @Override
    protected String toolName() { 'jbang' }

    @Override
    @Internal
    boolean isSet() {
        super.isSet() ||
            catalog.isSet() ||
            commitAuthor.isSet()
    }

    @Override
    void catalog(Action<? super Tap> action) {
        action.execute(catalog)
    }


    @Override
    void commitAuthor(Action<? super CommitAuthor> action) {
        action.execute(commitAuthor)
    }

    org.jreleaser.model.Jbang toModel() {
        org.jreleaser.model.Jbang tool = new org.jreleaser.model.Jbang()
        fillToolProperties(tool)
        if (catalog.isSet()) tool.catalog = catalog.toJbangCatalog()
        if (commitAuthor.isSet()) tool.commitAuthor = commitAuthor.toModel()
        tool
    }
}

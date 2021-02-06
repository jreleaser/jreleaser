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
import org.gradle.api.file.Directory
import org.gradle.api.internal.provider.Providers
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Internal

import javax.inject.Inject

/**
 *
 * @author Andres Almiray
 * @since 0.1.0
 */
@CompileStatic
class ScoopImpl extends AbstractTool implements org.jreleaser.gradle.plugin.dsl.Scoop {
    final Property<String> checkverUrl
    final Property<String> autoupdateUrl

    @Inject
    ScoopImpl(ObjectFactory objects, Provider<Directory> distributionsDirProvider) {
        super(objects, distributionsDirProvider)
        checkverUrl = objects.property(String).convention(Providers.notDefined())
        autoupdateUrl = objects.property(String).convention(Providers.notDefined())
    }

    @Override
    protected String toolName() { 'scoop' }

    @Override
    @Internal
    boolean isSet() {
        return super.isSet() ||
                checkverUrl.present ||
                autoupdateUrl.present
    }

    org.jreleaser.model.Scoop toModel() {
        org.jreleaser.model.Scoop tool = new org.jreleaser.model.Scoop()
        fillToolProperties(tool)
        tool.checkverUrl = checkverUrl.orNull
        tool.autoupdateUrl = autoupdateUrl.orNull
        tool
    }
}

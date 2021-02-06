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
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Provider

import javax.inject.Inject

/**
 *
 * @author Andres Almiray
 * @since 0.1.0
 */
@CompileStatic
class ChocolateyImpl extends AbstractTool implements org.jreleaser.gradle.plugin.dsl.Chocolatey {
    @Inject
    ChocolateyImpl(ObjectFactory objects, Provider<Directory> distributionsDirProvider) {
        super(objects, distributionsDirProvider)
    }

    @Override
    protected String toolName() { 'chocolatey' }

    org.jreleaser.model.Chocolatey toModel() {
        org.jreleaser.model.Chocolatey tool = new org.jreleaser.model.Chocolatey()
        fillToolProperties(tool)
        tool
    }
}

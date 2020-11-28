/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020 Andres Almiray.
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
import org.gradle.api.internal.provider.Providers
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.kordamp.jreleaser.gradle.plugin.dsl.ScoopPackager
import org.kordamp.jreleaser.model.Scoop

import javax.inject.Inject

/**
 *
 * @author Andres Almiray
 * @since 0.1.0
 */
@CompileStatic
class ScoopPackagerImpl extends AbstractPackagerTool implements ScoopPackager {
    final Property<String> checkverUrl
    final Property<String> autoupdateUrl

    @Inject
    ScoopPackagerImpl(ObjectFactory objects) {
        super(objects)
        checkverUrl = objects.property(String).convention(Providers.notDefined())
        autoupdateUrl = objects.property(String).convention(Providers.notDefined())
    }

    @Override
    protected String toolName() { 'scoop' }

    @Override
    boolean isSet() {
        return super.isSet() ||
                checkverUrl.present ||
                autoupdateUrl.present
    }

    Scoop toModel() {
        Scoop tool = new Scoop()
        fillToolProperties(tool)
        tool.checkverUrl = checkverUrl.orNull
        tool.autoupdateUrl = autoupdateUrl.orNull
        tool
    }
}

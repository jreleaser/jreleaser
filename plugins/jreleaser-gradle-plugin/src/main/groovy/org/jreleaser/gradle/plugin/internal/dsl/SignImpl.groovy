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
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.internal.provider.Providers
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.jreleaser.gradle.plugin.dsl.Sign

import javax.inject.Inject

/**
 *
 * @author Andres Almiray
 * @since 0.1.0
 */
@CompileStatic
class SignImpl implements Sign {
    final Property<Boolean> enabled
    final Property<Boolean> armored
    final Property<String> passphrase
    final RegularFileProperty keyRingFile

    @Inject
    SignImpl(ObjectFactory objects) {
        enabled = objects.property(Boolean).convention(Providers.notDefined())
        armored = objects.property(Boolean).convention(Providers.notDefined())
        passphrase = objects.property(String).convention(Providers.notDefined())
        keyRingFile = objects.fileProperty().convention(Providers.notDefined())
    }

    @Internal
    boolean isSet() {
        return enabled.present ||
            armored.present ||
            passphrase.present ||
            keyRingFile.present
    }

    org.jreleaser.model.Sign toModel() {
        org.jreleaser.model.Sign sign = new org.jreleaser.model.Sign()
        sign.enabled = enabled.orElse(isSet())
        sign.armored = armored.getOrElse(false)
        if (passphrase.present) sign.passphrase = passphrase.get()
        sign.keyRingFile = keyRingFile.present ? keyRingFile.getAsFile().get().absolutePath : null
        sign
    }
}

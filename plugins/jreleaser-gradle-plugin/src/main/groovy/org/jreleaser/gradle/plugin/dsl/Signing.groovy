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
package org.jreleaser.gradle.plugin.dsl

import groovy.transform.CompileStatic
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.jreleaser.model.Active

/**
 *
 * @author Andres Almiray
 * @since 0.1.0
 */
@CompileStatic
interface Signing {
    Property<Active> getActive()

    void setActive(String str)

    Property<Boolean> getArmored()

    Property<String> getPassphrase()

    Property<String> getPublicKey()

    Property<String> getSecretKey()

    Property<String> getExecutable()

    Property<String> getKeyName()

    Property<String> getHomeDir()

    Property<String> getPublicKeyring()

    Property<org.jreleaser.model.Signing.Mode> getMode()

    void setMode(String mode)

    Property<Boolean> getArtifacts()

    Property<Boolean> getFiles()

    Property<Boolean> getChecksums()

    Property<Boolean> getDefaultKeyring()

    ListProperty<String> getArgs()

    void addArg(String arg)
}
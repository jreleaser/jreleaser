/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2025 The JReleaser authors.
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
package org.jreleaser.gradle.plugin.dsl.signing

import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.jreleaser.gradle.plugin.dsl.common.Activatable

/**
 *
 * @author Andres Almiray
 * @since 0.1.0
 */
@CompileStatic
interface Signing extends Activatable {
    Property<Boolean> getArmored()

    Property<Boolean> getVerify()

    Property<String> getPassphrase()

    Property<String> getPublicKey()

    Property<String> getSecretKey()

    Property<org.jreleaser.model.Signing.Mode> getMode()

    void setMode(String mode)

    Property<Boolean> getArtifacts()

    Property<Boolean> getFiles()

    Property<Boolean> getChecksums()

    Property<Boolean> getCatalogs()

    Command getCommand()

    Cosign getCosign()

    void command(Action<? super Command> action)

    void cosign(Action<? super Cosign> action)

    void command(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Command) Closure<Void> action)

    void cosign(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Cosign) Closure<Void> action)

    interface Command {
        Property<String> getExecutable()

        Property<String> getKeyName()

        Property<String> getHomeDir()

        Property<String> getPublicKeyring()

        Property<Boolean> getDefaultKeyring()

        ListProperty<String> getArgs()

        void arg(String arg)
    }

    interface Cosign {
        Property<String> getVersion()

        RegularFileProperty getPrivateKeyFile()

        RegularFileProperty getPublicKeyFile()

        void setPrivateKeyFile(String privateKeyFile)

        void setPublicKeyFile(String publicKeyFile)
    }
}
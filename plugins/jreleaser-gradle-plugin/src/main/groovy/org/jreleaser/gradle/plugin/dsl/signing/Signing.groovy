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
    @Deprecated
    Property<Boolean> getArmored()

    @Deprecated
    Property<Boolean> getVerify()

    @Deprecated
    Property<String> getPassphrase()

    @Deprecated
    Property<String> getPublicKey()

    @Deprecated
    Property<String> getSecretKey()

    @Deprecated
    Property<org.jreleaser.model.Signing.Mode> getMode()

    @Deprecated
    void setMode(String mode)

    @Deprecated
    Property<Boolean> getArtifacts()

    @Deprecated
    Property<Boolean> getFiles()

    @Deprecated
    Property<Boolean> getChecksums()

    @Deprecated
    Property<Boolean> getCatalogs()

    Pgp getPgp()

    @Deprecated
    Command getCommand()

    Cosign getCosign()

    Minisign getMinisign()

    void pgp(Action<? super Pgp> action)

    @Deprecated
    void command(Action<? super Command> action)

    void cosign(Action<? super Cosign> action)

    void minisign(Action<? super Minisign> action)

    interface Command {
        Property<String> getExecutable()

        Property<String> getKeyName()

        Property<String> getHomeDir()

        Property<String> getPublicKeyring()

        Property<Boolean> getDefaultKeyring()

        ListProperty<String> getArgs()

        void arg(String arg)
    }

    interface SigningTool extends Activatable {
        Property<Boolean> getVerify()

        Property<String> getPassphrase()

        Property<Boolean> getArtifacts()

        Property<Boolean> getFiles()

        Property<Boolean> getChecksums()

        Property<Boolean> getCatalogs()
    }

    interface Pgp extends SigningTool {
        Property<Boolean> getArmored()

        Property<String> getPublicKey()

        Property<String> getSecretKey()

        Property<org.jreleaser.model.Signing.Mode> getMode()

        void setMode(String mode)

        Command getCommand()

        void command(Action<? super Command> action)
    }

    interface Cosign extends SigningTool {
        Property<String> getVersion()

        @Deprecated
        RegularFileProperty getPrivateKeyFile()

        RegularFileProperty getSecretKeyFile()

        RegularFileProperty getPublicKeyFile()

        @Deprecated
        void setPrivateKeyFile(String privateKeyFile)

        void setSecretKeyFile(String secretKeyFile)

        void setPublicKeyFile(String publicKeyFile)
    }

    interface Minisign extends SigningTool {
        Property<String> getVersion()

        RegularFileProperty getSecretKeyFile()

        RegularFileProperty getPublicKeyFile()

        void setSecretKeyFile(String secretKeyFile)

        void setPublicKeyFile(String publicKeyFile)
    }
}
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
package org.jreleaser.gradle.plugin.internal.dsl.signing

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.internal.provider.Providers
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.jreleaser.gradle.plugin.dsl.signing.Signing
import org.jreleaser.model.Active
import org.kordamp.gradle.util.ConfigureUtil

import javax.inject.Inject

import static org.jreleaser.util.StringUtils.isNotBlank

/**
 *
 * @author Andres Almiray
 * @since 0.1.0
 */
@CompileStatic
class SigningImpl implements Signing {
    final Property<Active> active
    final Property<Boolean> armored
    final Property<Boolean> verify
    final Property<String> passphrase
    final Property<String> publicKey
    final Property<String> secretKey
    final Property<org.jreleaser.model.Signing.Mode> mode
    final Property<Boolean> artifacts
    final Property<Boolean> files
    final Property<Boolean> checksums
    final Property<Boolean> catalogs
    final Command command
    final Cosign cosign

    @Inject
    SigningImpl(ObjectFactory objects) {
        active = objects.property(Active).convention(Providers.<Active> notDefined())
        armored = objects.property(Boolean).convention(Providers.<Boolean> notDefined())
        verify = objects.property(Boolean).convention(Providers.<Boolean> notDefined())
        passphrase = objects.property(String).convention(Providers.<String> notDefined())
        publicKey = objects.property(String).convention(Providers.<String> notDefined())
        secretKey = objects.property(String).convention(Providers.<String> notDefined())
        mode = objects.property(org.jreleaser.model.Signing.Mode).convention(org.jreleaser.model.Signing.Mode.MEMORY)
        artifacts = objects.property(Boolean).convention(Providers.<Boolean> notDefined())
        files = objects.property(Boolean).convention(Providers.<Boolean> notDefined())
        checksums = objects.property(Boolean).convention(Providers.<Boolean> notDefined())
        catalogs = objects.property(Boolean).convention(Providers.<Boolean> notDefined())
        command = objects.newInstance(CommandImpl, objects)
        cosign = objects.newInstance(CosignImpl, objects)
    }

    @Internal
    boolean isSet() {
        return active.present ||
            armored.present ||
            verify.present ||
            passphrase.present ||
            publicKey.present ||
            artifacts.present ||
            files.present ||
            checksums.present ||
            catalogs.present ||
            secretKey.present ||
            ((CommandImpl) command).isSet() ||
            ((CosignImpl) cosign).isSet()
    }

    @Override
    void setActive(String str) {
        if (isNotBlank(str)) {
            active.set(Active.of(str.trim()))
        }
    }

    @Override
    void setMode(String str) {
        if (isNotBlank(str)) {
            mode.set(org.jreleaser.model.Signing.Mode.of(str.trim()))
        }
    }

    @Override
    void command(Action<? super Command> action) {
        action.execute(command)
    }

    @Override
    void cosign(Action<? super Cosign> action) {
        action.execute(cosign)
    }

    @Override
    @CompileDynamic
    void command(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Command) Closure<Void> action) {
        ConfigureUtil.configure(action, command)
    }

    @Override
    @CompileDynamic
    void cosign(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = Cosign) Closure<Void> action) {
        ConfigureUtil.configure(action, cosign)
    }

    org.jreleaser.model.internal.signing.Signing toModel() {
        org.jreleaser.model.internal.signing.Signing signing = new org.jreleaser.model.internal.signing.Signing()
        if (active.present) signing.active = active.get()
        if (armored.present) signing.armored = armored.get()
        if (verify.present) signing.verify = verify.get()
        if (passphrase.present) signing.passphrase = passphrase.get()
        if (publicKey.present) signing.publicKey = publicKey.get()
        if (secretKey.present) signing.secretKey = secretKey.get()
        if (mode.present) signing.mode = mode.get()
        if (artifacts.present) signing.artifacts = artifacts.get()
        if (files.present) signing.files = files.get()
        if (checksums.present) signing.checksums = checksums.get()
        if (catalogs.present) signing.catalogs = catalogs.get()
        signing.command = ((CommandImpl) command).toModel()
        signing.cosign = ((CosignImpl) cosign).toModel()
        signing
    }

    static class CommandImpl implements Command {
        final Property<String> executable
        final Property<String> keyName
        final Property<String> homeDir
        final Property<String> publicKeyring
        final Property<Boolean> defaultKeyring
        final ListProperty<String> args

        @Inject
        CommandImpl(ObjectFactory objects) {
            executable = objects.property(String).convention(Providers.<String> notDefined())
            keyName = objects.property(String).convention(Providers.<String> notDefined())
            homeDir = objects.property(String).convention(Providers.<String> notDefined())
            publicKeyring = objects.property(String).convention(Providers.<String> notDefined())
            defaultKeyring = objects.property(Boolean).convention(Providers.<Boolean> notDefined())
            args = objects.listProperty(String).convention(Providers.<List<String>> notDefined())
        }

        @Internal
        boolean isSet() {
            return executable.present ||
                keyName.present ||
                homeDir.present ||
                publicKeyring.present ||
                defaultKeyring.present ||
                args.present
        }

        @Override
        void arg(String arg) {
            if (isNotBlank(arg)) {
                args.add(arg.trim())
            }
        }

        org.jreleaser.model.internal.signing.Signing.Command toModel() {
            org.jreleaser.model.internal.signing.Signing.Command command = new org.jreleaser.model.internal.signing.Signing.Command()
            if (executable.present) command.executable = executable.get()
            if (keyName.present) command.keyName = keyName.get()
            if (homeDir.present) command.homeDir = homeDir.get()
            if (publicKeyring.present) command.publicKeyring = publicKeyring.get()
            if (defaultKeyring.present) command.defaultKeyring = defaultKeyring.get()
            command.args = (List<String>) args.getOrElse([])
            command
        }
    }

    static class CosignImpl implements Cosign {
        final Property<String> version
        final RegularFileProperty privateKeyFile
        final RegularFileProperty publicKeyFile

        @Inject
        CosignImpl(ObjectFactory objects) {
            version = objects.property(String).convention(Providers.<String> notDefined())
            privateKeyFile = objects.fileProperty().convention(Providers.notDefined())
            publicKeyFile = objects.fileProperty().convention(Providers.notDefined())
        }

        @Override
        void setPrivateKeyFile(String privateKeyFile) {
            this.privateKeyFile.set(new File(privateKeyFile))
        }

        @Override
        void setPublicKeyFile(String publicKeyFile) {
            this.publicKeyFile.set(new File(publicKeyFile))
        }

        @Internal
        boolean isSet() {
            return version.present ||
                privateKeyFile.present ||
                publicKeyFile.present
        }

        org.jreleaser.model.internal.signing.Signing.Cosign toModel() {
            org.jreleaser.model.internal.signing.Signing.Cosign cosign = new org.jreleaser.model.internal.signing.Signing.Cosign()
            if (version.present) cosign.version = version.get()
            if (privateKeyFile.present) cosign.privateKeyFile = privateKeyFile.get().asFile.toPath().toString()
            if (publicKeyFile.present) cosign.publicKeyFile = publicKeyFile.get().asFile.toPath().toString()
            cosign
        }
    }
}

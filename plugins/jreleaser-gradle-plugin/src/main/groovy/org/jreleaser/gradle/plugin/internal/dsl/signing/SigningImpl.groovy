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
    final Pgp pgp
    final Cosign cosign
    final Minisign minisign

    @Inject
    SigningImpl(ObjectFactory objects) {
        active = objects.property(Active).convention(Providers.<Active> notDefined())
        pgp = objects.newInstance(PgpImpl, objects)
        cosign = objects.newInstance(CosignImpl, objects)
        minisign = objects.newInstance(MinisignImpl, objects)
    }

    @Internal
    boolean isSet() {
        return active.present ||
            ((PgpImpl) pgp).isSet() ||
            ((CosignImpl) cosign).isSet() ||
            ((MinisignImpl) minisign).isSet()
    }

    @Override
    void setActive(String str) {
        if (isNotBlank(str)) {
            active.set(Active.of(str.trim()))
        }
    }

    @Override
    Property<Boolean> getArmored() {
        pgp.armored
    }

    @Override
    Property<Boolean> getVerify() {
        pgp.verify
    }

    @Override
    Property<String> getPassphrase() {
        pgp.passphrase
    }

    @Override
    Property<String> getPublicKey() {
        pgp.publicKey
    }

    @Override
    Property<String> getSecretKey() {
        pgp.secretKey
    }

    @Override
    Property<org.jreleaser.model.Signing.Mode> getMode() {
        pgp.mode
    }

    @Override
    void setMode(String str) {
        pgp.setMode(str)
    }

    @Override
    Property<Boolean> getArtifacts() {
        pgp.artifacts
    }

    @Override
    Property<Boolean> getFiles() {
        pgp.files
    }

    @Override
    Property<Boolean> getChecksums() {
        pgp.checksums
    }

    @Override
    Property<Boolean> getCatalogs() {
        pgp.catalogs
    }

    @Override
    Command getCommand() {
        pgp.command
    }

    @Override
    void command(Action<? super Command> action) {
        pgp.command(action)
    }

    @Override
    void pgp(Action<? super Pgp> action) {
        action.execute(pgp)
    }

    @Override
    void cosign(Action<? super Cosign> action) {
        action.execute(cosign)
    }

    @Override
    void minisign(Action<? super Minisign> action) {
        action.execute(minisign)
    }

    org.jreleaser.model.internal.signing.Signing toModel() {
        org.jreleaser.model.internal.signing.Signing signing = new org.jreleaser.model.internal.signing.Signing()
        if (active.present) signing.active = active.get()
        signing.pgp = ((PgpImpl) pgp).toModel()
        signing.cosign = ((CosignImpl) cosign).toModel()
        signing.minisign = ((MinisignImpl) minisign).toModel()
        signing
    }

    static abstract class SigningToolImpl implements SigningTool {
        final Property<Active> active
        final Property<Boolean> verify
        final Property<String> passphrase
        final Property<Boolean> artifacts
        final Property<Boolean> files
        final Property<Boolean> checksums
        final Property<Boolean> catalogs

        @Inject
        SigningToolImpl(ObjectFactory objects) {
            active = objects.property(Active).convention(Providers.<Active> notDefined())
            verify = objects.property(Boolean).convention(Providers.<Boolean> notDefined())
            passphrase = objects.property(String).convention(Providers.<String> notDefined())
            artifacts = objects.property(Boolean).convention(Providers.<Boolean> notDefined())
            files = objects.property(Boolean).convention(Providers.<Boolean> notDefined())
            checksums = objects.property(Boolean).convention(Providers.<Boolean> notDefined())
            catalogs = objects.property(Boolean).convention(Providers.<Boolean> notDefined())
        }

        @Internal
        boolean isSet() {
            return active.present ||
                verify.present ||
                passphrase.present ||
                artifacts.present ||
                files.present ||
                checksums.present ||
                catalogs.present
        }

        @Override
        void setActive(String str) {
            if (isNotBlank(str)) {
                active.set(Active.of(str.trim()))
            }
        }

        void toModel(org.jreleaser.model.internal.signing.SigningTool tool) {
            if (active.present) tool.active = active.get()
            if (verify.present) tool.verify = verify.get()
            if (passphrase.present) tool.passphrase = passphrase.get()
            if (artifacts.present) tool.artifacts = artifacts.get()
            if (files.present) tool.files = files.get()
            if (checksums.present) tool.checksums = checksums.get()
            if (catalogs.present) tool.catalogs = catalogs.get()
        }
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

    static class PgpImpl extends SigningToolImpl implements Pgp {
        final Property<Boolean> armored
        final Property<String> publicKey
        final Property<String> secretKey
        final Property<org.jreleaser.model.Signing.Mode> mode
        final Command command

        @Inject
        PgpImpl(ObjectFactory objects) {
            super(objects)
            armored = objects.property(Boolean).convention(Providers.<Boolean> notDefined())
            publicKey = objects.property(String).convention(Providers.<String> notDefined())
            secretKey = objects.property(String).convention(Providers.<String> notDefined())
            mode = objects.property(org.jreleaser.model.Signing.Mode).convention(org.jreleaser.model.Signing.Mode.MEMORY)
            command = objects.newInstance(CommandImpl, objects)
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

        @Internal
        boolean isSet() {
            return super.isSet() ||
                armored.present ||
                publicKey.present ||
                secretKey.present ||
                ((CommandImpl) command).isSet()
        }

        org.jreleaser.model.internal.signing.Signing.Pgp toModel() {
            org.jreleaser.model.internal.signing.Signing.Pgp pgp = new org.jreleaser.model.internal.signing.Signing.Pgp()
            toModel(pgp)
            if (armored.present) pgp.armored = armored.get()
            if (publicKey.present) pgp.publicKey = publicKey.get()
            if (secretKey.present) pgp.secretKey = secretKey.get()
            if (mode.present) pgp.mode = mode.get()
            pgp.command = ((CommandImpl) command).toModel()
            pgp
        }
    }

    static class CosignImpl extends SigningToolImpl implements Cosign {
        final Property<String> version
        final RegularFileProperty secretKeyFile
        final RegularFileProperty publicKeyFile

        @Inject
        CosignImpl(ObjectFactory objects) {
            super(objects)
            version = objects.property(String).convention(Providers.<String> notDefined())
            secretKeyFile = objects.fileProperty().convention(Providers.notDefined())
            publicKeyFile = objects.fileProperty().convention(Providers.notDefined())
        }

        RegularFileProperty getPrivateKeyFile() {
            secretKeyFile
        }

        @Override
        void setPrivateKeyFile(String secretKeyFile) {
            setSecretKeyFile(secretKeyFile)
        }

        @Override
        void setSecretKeyFile(String secretKeyFile) {
            this.secretKeyFile.set(new File(secretKeyFile))
        }

        @Override
        void setPublicKeyFile(String publicKeyFile) {
            this.publicKeyFile.set(new File(publicKeyFile))
        }

        @Internal
        boolean isSet() {
            return super.isSet() ||
                version.present ||
                secretKeyFile.present ||
                publicKeyFile.present
        }

        org.jreleaser.model.internal.signing.Signing.Cosign toModel() {
            org.jreleaser.model.internal.signing.Signing.Cosign cosign = new org.jreleaser.model.internal.signing.Signing.Cosign()
            toModel(cosign)
            if (version.present) cosign.version = version.get()
            if (secretKeyFile.present) cosign.secretKeyFile = privateKeyFile.get().asFile.toPath().toString()
            if (publicKeyFile.present) cosign.publicKeyFile = publicKeyFile.get().asFile.toPath().toString()
            cosign
        }
    }

    static class MinisignImpl extends SigningToolImpl implements Minisign {
        final Property<String> version
        final RegularFileProperty secretKeyFile
        final RegularFileProperty publicKeyFile

        @Inject
        MinisignImpl(ObjectFactory objects) {
            super(objects)
            version = objects.property(String).convention(Providers.<String> notDefined())
            secretKeyFile = objects.fileProperty().convention(Providers.notDefined())
            publicKeyFile = objects.fileProperty().convention(Providers.notDefined())
        }

        @Override
        void setSecretKeyFile(String secretKeyFile) {
            this.secretKeyFile.set(new File(secretKeyFile))
        }

        @Override
        void setPublicKeyFile(String publicKeyFile) {
            this.publicKeyFile.set(new File(publicKeyFile))
        }

        @Internal
        boolean isSet() {
            return super.isSet() ||
                version.present ||
                secretKeyFile.present ||
                publicKeyFile.present
        }

        org.jreleaser.model.internal.signing.Signing.Minisign toModel() {
            org.jreleaser.model.internal.signing.Signing.Minisign minisign = new org.jreleaser.model.internal.signing.Signing.Minisign()
            toModel(minisign)
            if (version.present) minisign.version = version.get()
            if (secretKeyFile.present) minisign.secretKeyFile = secretKeyFile.get().asFile.toPath().toString()
            if (publicKeyFile.present) minisign.publicKeyFile = publicKeyFile.get().asFile.toPath().toString()
            minisign
        }
    }
}

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
package org.jreleaser.gradle.plugin.internal.dsl

import groovy.transform.CompileStatic
import org.gradle.api.internal.provider.Providers
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.jreleaser.gradle.plugin.dsl.Signing
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
    final Property<Boolean> armored
    final Property<String> passphrase
    final Property<String> publicKey
    final Property<String> secretKey
    final Property<org.jreleaser.model.Signing.Mode> mode
    final Property<Boolean> artifacts
    final Property<Boolean> files
    final Property<Boolean> checksums
    final Property<String> executable
    final Property<String> keyName
    final Property<String> homeDir
    final Property<String> publicKeyring
    final Property<Boolean> defaultKeyring
    final ListProperty<String> args

    @Inject
    SigningImpl(ObjectFactory objects) {
        active = objects.property(Active).convention(Providers.notDefined())
        armored = objects.property(Boolean).convention(Providers.notDefined())
        passphrase = objects.property(String).convention(Providers.notDefined())
        publicKey = objects.property(String).convention(Providers.notDefined())
        secretKey = objects.property(String).convention(Providers.notDefined())
        mode = objects.property(org.jreleaser.model.Signing.Mode).convention(org.jreleaser.model.Signing.Mode.MEMORY)
        artifacts = objects.property(Boolean).convention(Providers.notDefined())
        files = objects.property(Boolean).convention(Providers.notDefined())
        checksums = objects.property(Boolean).convention(Providers.notDefined())
        executable = objects.property(String).convention(Providers.notDefined())
        keyName = objects.property(String).convention(Providers.notDefined())
        homeDir = objects.property(String).convention(Providers.notDefined())
        publicKeyring = objects.property(String).convention(Providers.notDefined())
        defaultKeyring = objects.property(Boolean).convention(Providers.notDefined())
        args = objects.listProperty(String).convention(Providers.notDefined())
    }

    @Internal
    boolean isSet() {
        return active.present ||
            armored.present ||
            passphrase.present ||
            publicKey.present ||
            artifacts.present ||
            files.present ||
            checksums.present ||
            secretKey.present ||
            executable.present ||
            keyName.present ||
            homeDir.present ||
            publicKeyring.present ||
            defaultKeyring.present ||
            args.present
    }

    @Override
    void addArg(String arg) {
        if (isNotBlank(arg)) {
            args.add(arg.trim())
        }
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

    org.jreleaser.model.Signing toModel() {
        org.jreleaser.model.Signing signing = new org.jreleaser.model.Signing()
        if (active.present) signing.active = active.get()
        if (armored.present) signing.armored = armored.get()
        if (passphrase.present) signing.passphrase = passphrase.get()
        if (publicKey.present) signing.publicKey = publicKey.get()
        if (secretKey.present) signing.secretKey = secretKey.get()
        if (mode.present) signing.mode = mode.get()
        if (artifacts.present) signing.artifacts = artifacts.get()
        if (files.present) signing.files = files.get()
        if (checksums.present) signing.checksums = checksums.get()
        if (executable.present) signing.executable = executable.get()
        if (keyName.present) signing.keyName = keyName.get()
        if (homeDir.present) signing.homeDir = homeDir.get()
        if (publicKeyring.present) signing.publicKeyring = publicKeyring.get()
        if (defaultKeyring.present) signing.defaultKeyring = defaultKeyring.get()
        signing.args = (List<String>) args.getOrElse([])
        signing
    }
}

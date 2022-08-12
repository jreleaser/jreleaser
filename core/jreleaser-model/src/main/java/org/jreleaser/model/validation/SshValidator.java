/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2022 The JReleaser authors.
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
package org.jreleaser.model.validation;

import org.jreleaser.model.JReleaserContext;
import org.jreleaser.model.Ssh;
import org.jreleaser.util.Env;
import org.jreleaser.util.Errors;

import static org.jreleaser.util.CollectionUtils.listOf;

/**
 * @author Andres Almiray
 * @since 1.1.0
 */
public abstract class SshValidator extends Validator {
    public static void validateSsh(JReleaserContext context, Ssh ssh, String name,
                                   String envPrefix, String propPrefix, Errors errors) {
        ssh.setUsername(
            checkProperty(context,
                listOf(
                    envPrefix + "_" + Env.toVar(name) + "_USERNAME",
                    "SSH_" + Env.toVar(name) + "_USERNAME",
                    envPrefix + "_USERNAME",
                    "SSH_USERNAME"),
                propPrefix + ".username",
                ssh.getUsername(),
                errors,
                context.isDryrun()));

        ssh.setPassword(
            checkProperty(context,
                listOf(
                    envPrefix + "_" + Env.toVar(name) + "_PASSWORD",
                    "SSH_" + Env.toVar(name) + "_PASSWORD",
                    envPrefix + "_PASSWORD",
                    "SSH_PASSWORD"),
                propPrefix + ".password",
                ssh.getPassword(),
                errors,
                context.isDryrun()));

        ssh.setHost(
            checkProperty(context,
                listOf(
                    envPrefix + "_" + Env.toVar(name) + "_HOST",
                    "SSH_" + Env.toVar(name) + "_HOST",
                    envPrefix + "_HOST",
                    "SSH_HOST"),
                propPrefix + ".host",
                ssh.getHost(),
                errors,
                context.isDryrun()));

        ssh.setPort(
            checkProperty(context,
                listOf(
                    envPrefix + "_" + Env.toVar(name) + "_PORT",
                    "SSH_" + Env.toVar(name) + "_PORT",
                    envPrefix + "_PORT",
                    "SSH_PORT"),
                propPrefix + ".port",
                ssh.getPort(),
                errors,
                context.isDryrun()));

        ssh.setPublicKey(
            checkProperty(context,
                listOf(
                    envPrefix + "_" + Env.toVar(name) + "_PUBLIC_KEY",
                    "SSH_" + Env.toVar(name) + "_PUBLIC_KEY",
                    envPrefix + "_PUBLIC_KEY",
                    "SSH_PUBLIC_KEY"),
                propPrefix + ".publicKey",
                ssh.getPublicKey(),
                errors,
                true));

        ssh.setPrivateKey(
            checkProperty(context,
                listOf(
                    envPrefix + "_" + Env.toVar(name) + "_PRIVATE_KEY",
                    "SSH_" + Env.toVar(name) + "_PRIVATE_KEY",
                    envPrefix + "_PRIVATE_KEY",
                    "SSH_PRIVATE_KEY"),
                propPrefix + ".privateKey",
                ssh.getPrivateKey(),
                errors,
                true));

        ssh.setPassphrase(
            checkProperty(context,
                listOf(
                    envPrefix + "_" + Env.toVar(name) + "_PASSPHRASE",
                    "SSH_" + Env.toVar(name) + "_PASSPHRASE",
                    envPrefix + "_PASSPHRASE",
                    "SSH_PASSPHRASE"),
                propPrefix + ".passphrase",
                ssh.getPassphrase(),
                errors,
                true));

        ssh.setFingerprint(
            checkProperty(context,
                listOf(
                    envPrefix + "_" + Env.toVar(name) + "_FINGERPRINT",
                    "SSH_" + Env.toVar(name) + "_FINGERPRINT",
                    envPrefix + "_FINGERPRINT",
                    "SSH_FINGERPRINT"),
                propPrefix + ".fingerprint",
                ssh.getFingerprint(),
                errors,
                true));
    }
}

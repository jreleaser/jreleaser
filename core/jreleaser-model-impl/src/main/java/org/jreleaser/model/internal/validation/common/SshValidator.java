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
package org.jreleaser.model.internal.validation.common;

import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.internal.common.Ssh;
import org.jreleaser.model.internal.servers.SshServer;
import org.jreleaser.util.Errors;

import static org.jreleaser.model.internal.validation.common.Validator.checkProperty;
import static org.jreleaser.util.CollectionUtils.setOf;

/**
 * @author Andres Almiray
 * @since 1.1.0
 */
public final class SshValidator {
    private static final String SSH = "ssh";
    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";
    private static final String HOST = "host";
    private static final String PORT = "port";
    private static final String PUBLIC_KEY = "public.key";
    private static final String PRIVATE_KEY = "private.key";
    private static final String PASSPHRASE = "passphrase";
    private static final String FINGERPRINT = "fingerprint";
    private static final String DOT = ".";

    private SshValidator() {
        // noop
    }

    public static void validateSsh(JReleaserContext context, Ssh ssh, String prefix, String type, String name, Errors errors) {
        validateSsh(context, ssh, null, prefix, type, name, errors, context.isDryrun());
    }

    public static void validateSsh(JReleaserContext context, Ssh ssh, String prefix, String type, String name, Errors errors, boolean continueOnError) {
        validateSsh(context, ssh, null, prefix, type, name, errors, continueOnError);
    }

    public static void validateSsh(JReleaserContext context, Ssh ssh, SshServer server, String prefix, String type, String name, Errors errors) {
        validateSsh(context, ssh, server, prefix, type, name, errors, context.isDryrun());
    }

    public static void validateSsh(JReleaserContext context, Ssh ssh, SshServer server, String prefix, String type, String name, Errors errors, boolean continueOnError) {
        ssh.setUsername(
            checkProperty(context,
                setOf(
                    prefix + DOT + type + DOT + name + DOT + USERNAME,
                    prefix + DOT + SSH + DOT + name + DOT + USERNAME,
                    prefix + DOT + type + DOT + USERNAME,
                    prefix + DOT + SSH + DOT + USERNAME,
                    type + DOT + name + DOT + USERNAME,
                    SSH + DOT + name + DOT + USERNAME,
                    type + DOT + USERNAME,
                    SSH + DOT + USERNAME),
                prefix + DOT + type + DOT + name + DOT + USERNAME,
                ssh.getUsername(),
                null != server ? server.getUsername() : null,
                errors,
                continueOnError));

        ssh.setPassword(
            checkProperty(context,
                setOf(
                    prefix + DOT + type + DOT + name + DOT + PASSWORD,
                    prefix + DOT + SSH + DOT + name + DOT + PASSWORD,
                    prefix + DOT + type + DOT + PASSWORD,
                    prefix + DOT + SSH + DOT + PASSWORD,
                    type + DOT + name + DOT + PASSWORD,
                    SSH + DOT + name + DOT + PASSWORD,
                    type + DOT + PASSWORD,
                    SSH + DOT + PASSWORD),
                prefix + DOT + type + DOT + name + DOT + PASSWORD,
                ssh.getPassword(),
                null != server ? server.getPassword() : null,
                errors,
                continueOnError));

        ssh.setHost(
            checkProperty(context,
                setOf(
                    prefix + DOT + type + DOT + name + DOT + HOST,
                    prefix + DOT + SSH + DOT + name + DOT + HOST,
                    prefix + DOT + type + DOT + HOST,
                    prefix + DOT + SSH + DOT + HOST,
                    type + DOT + name + DOT + HOST,
                    SSH + DOT + name + DOT + HOST,
                    type + DOT + HOST,
                    SSH + DOT + HOST),
                prefix + DOT + type + DOT + name + DOT + HOST,
                ssh.getHost(),
                null != server ? server.getHost() : null,
                errors,
                continueOnError));

        ssh.setPort(
            checkProperty(context,
                setOf(
                    prefix + DOT + type + DOT + name + DOT + PORT,
                    prefix + DOT + SSH + DOT + name + DOT + PORT,
                    prefix + DOT + type + DOT + PORT,
                    prefix + DOT + SSH + DOT + PORT,
                    type + DOT + name + DOT + PORT,
                    SSH + DOT + name + DOT + PORT,
                    type + DOT + PORT,
                    SSH + DOT + PORT),
                prefix + DOT + type + DOT + name + DOT + PORT,
                ssh.getPort(),
                null != server ? server.getPort() : null,
                errors,
                continueOnError));

        ssh.setPublicKey(
            checkProperty(context,
                setOf(
                    prefix + DOT + type + DOT + name + DOT + PUBLIC_KEY,
                    prefix + DOT + SSH + DOT + name + DOT + PUBLIC_KEY,
                    prefix + DOT + type + DOT + PUBLIC_KEY,
                    prefix + DOT + SSH + DOT + PUBLIC_KEY,
                    type + DOT + name + DOT + PUBLIC_KEY,
                    SSH + DOT + name + DOT + PUBLIC_KEY,
                    type + DOT + PUBLIC_KEY,
                    SSH + DOT + PUBLIC_KEY),
                prefix + DOT + type + DOT + name + ".publicKey",
                ssh.getPublicKey(),
                null != server ? server.getPublicKey() : null,
                errors,
                true));

        ssh.setPrivateKey(
            checkProperty(context,
                setOf(
                    prefix + DOT + type + DOT + name + DOT + PRIVATE_KEY,
                    prefix + DOT + SSH + DOT + name + DOT + PRIVATE_KEY,
                    prefix + DOT + type + DOT + PRIVATE_KEY,
                    prefix + DOT + SSH + DOT + PRIVATE_KEY,
                    type + DOT + name + DOT + PRIVATE_KEY,
                    SSH + DOT + name + DOT + PRIVATE_KEY,
                    type + DOT + PRIVATE_KEY,
                    SSH + DOT + PRIVATE_KEY),
                prefix + DOT + type + DOT + name + ".privateKey",
                ssh.getPrivateKey(),
                null != server ? server.getPrivateKey() : null,
                errors,
                true));

        ssh.setPassphrase(
            checkProperty(context,
                setOf(
                    prefix + DOT + type + DOT + name + DOT + PASSPHRASE,
                    prefix + DOT + SSH + DOT + name + DOT + PASSPHRASE,
                    prefix + DOT + type + DOT + PASSPHRASE,
                    prefix + DOT + SSH + DOT + PASSPHRASE,
                    type + DOT + name + DOT + PASSPHRASE,
                    SSH + DOT + name + DOT + PASSPHRASE,
                    type + DOT + PASSPHRASE,
                    SSH + DOT + PASSPHRASE),
                prefix + DOT + type + DOT + name + DOT + PASSPHRASE,
                ssh.getPassphrase(),
                null != server ? server.getPassphrase() : null,
                errors,
                true));

        ssh.setFingerprint(
            checkProperty(context,
                setOf(
                    prefix + DOT + type + DOT + name + DOT + FINGERPRINT,
                    prefix + DOT + SSH + DOT + name + DOT + FINGERPRINT,
                    prefix + DOT + type + DOT + FINGERPRINT,
                    prefix + DOT + SSH + DOT + FINGERPRINT,
                    type + DOT + name + DOT + FINGERPRINT,
                    SSH + DOT + name + DOT + FINGERPRINT,
                    type + DOT + FINGERPRINT,
                    SSH + DOT + FINGERPRINT),
                prefix + DOT + type + DOT + name + DOT + FINGERPRINT,
                ssh.getFingerprint(),
                null != server ? server.getFingerprint() : null,
                errors,
                true));
    }
}

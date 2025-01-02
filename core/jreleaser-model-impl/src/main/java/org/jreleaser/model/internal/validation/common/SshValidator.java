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
import org.jreleaser.util.Errors;

import static org.jreleaser.model.internal.validation.common.Validator.checkProperty;
import static org.jreleaser.util.CollectionUtils.listOf;

/**
 * @author Andres Almiray
 * @since 1.1.0
 */
public final class SshValidator {
    private SshValidator() {
        // noop
    }

    public static void validateSsh(JReleaserContext context, Ssh ssh, String type,
                                   String name, String prefix, Errors errors) {
        ssh.setUsername(
            checkProperty(context,
                listOf(
                    prefix + type + "." + name + ".username",
                    prefix + "ssh." + name + ".username",
                    prefix + type + ".username",
                    prefix + "ssh" + ".username",
                    type + "." + name + ".username",
                    "ssh." + name + ".username",
                    type + ".username",
                    "ssh.username"),
                prefix + type + "." + name + ".username",
                ssh.getUsername(),
                errors,
                context.isDryrun()));

        ssh.setPassword(
            checkProperty(context,
                listOf(
                    prefix + type + "." + name + ".password",
                    prefix + "ssh." + name + ".password",
                    prefix + type + ".password",
                    prefix + "ssh" + ".password",
                    type + "." + name + ".password",
                    "ssh." + name + ".password",
                    type + ".password",
                    "ssh.password"),
                prefix + type + "." + name + ".password",
                ssh.getPassword(),
                errors,
                context.isDryrun()));

        ssh.setHost(
            checkProperty(context,
                listOf(
                    prefix + type + "." + name + ".host",
                    prefix + "ssh." + name + ".host",
                    prefix + type + ".host",
                    prefix + "ssh" + ".host",
                    type + "." + name + ".host",
                    "ssh." + name + ".host",
                    type + ".host",
                    "ssh.host"),
                prefix + type + "." + name + ".host",
                ssh.getHost(),
                errors,
                context.isDryrun()));

        ssh.setPort(
            checkProperty(context,
                listOf(
                    prefix + type + "." + name + ".port",
                    prefix + "ssh." + name + ".port",
                    prefix + type + ".port",
                    prefix + "ssh" + ".port",
                    type + "." + name + ".port",
                    "ssh." + name + ".port",
                    type + ".port",
                    "ssh.port"),
                prefix + type + "." + name + ".port",
                ssh.getPort(),
                errors,
                context.isDryrun()));

        ssh.setPublicKey(
            checkProperty(context,
                listOf(
                    prefix + type + "." + name + ".public.key",
                    prefix + "ssh." + name + ".public.key",
                    prefix + type + ".public.key",
                    prefix + "ssh" + ".public.key",
                    type + "." + name + ".public.key",
                    "ssh." + name + ".public.key",
                    type + ".public.key",
                    "ssh.public.key"),
                prefix + type + "." + name + ".publicKey",
                ssh.getPublicKey(),
                errors,
                true));

        ssh.setPrivateKey(
            checkProperty(context,
                listOf(
                    prefix + type + "." + name + ".private.key",
                    prefix + "ssh." + name + ".private.key",
                    prefix + type + ".private.key",
                    prefix + "ssh" + ".private.key",
                    type + "." + name + ".private.key",
                    "ssh." + name + ".private.key",
                    type + ".private.key",
                    "ssh.private.key"),
                prefix + type + "." + name + ".privateKey",
                ssh.getPrivateKey(),
                errors,
                true));

        ssh.setPassphrase(
            checkProperty(context,
                listOf(
                    prefix + type + "." + name + ".passphrase",
                    prefix + "ssh." + name + ".passphrase",
                    prefix + type + ".passphrase",
                    prefix + "ssh" + ".passphrase",
                    type + "." + name + ".passphrase",
                    "ssh." + name + ".passphrase",
                    type + ".passphrase",
                    "ssh.passphrase"),
                prefix + type + "." + name + ".passphrase",
                ssh.getPassphrase(),
                errors,
                true));

        ssh.setFingerprint(
            checkProperty(context,
                listOf(
                    prefix + type + "." + name + ".fingerprint",
                    prefix + "ssh." + name + ".fingerprint",
                    prefix + type + ".fingerprint",
                    prefix + "ssh" + ".fingerprint",
                    type + "." + name + ".fingerprint",
                    "ssh." + name + ".fingerprint",
                    type + ".fingerprint",
                    "ssh.fingerprint"),
                prefix + type + "." + name + ".fingerprint",
                ssh.getFingerprint(),
                errors,
                true));
    }
}

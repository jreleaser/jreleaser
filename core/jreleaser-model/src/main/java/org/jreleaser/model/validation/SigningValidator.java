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

import org.jreleaser.bundle.RB;
import org.jreleaser.model.JReleaserContext;
import org.jreleaser.model.Signing;
import org.jreleaser.util.Errors;
import org.jreleaser.util.PlatformUtils;

import static org.jreleaser.model.Signing.COSIGN_PASSWORD;
import static org.jreleaser.model.Signing.COSIGN_PRIVATE_KEY;
import static org.jreleaser.model.Signing.COSIGN_PUBLIC_KEY;
import static org.jreleaser.model.Signing.GPG_EXECUTABLE;
import static org.jreleaser.model.Signing.GPG_HOMEDIR;
import static org.jreleaser.model.Signing.GPG_KEYNAME;
import static org.jreleaser.model.Signing.GPG_PASSPHRASE;
import static org.jreleaser.model.Signing.GPG_PUBLIC_KEY;
import static org.jreleaser.model.Signing.GPG_PUBLIC_KEYRING;
import static org.jreleaser.model.Signing.GPG_SECRET_KEY;
import static org.jreleaser.util.StringUtils.isBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public abstract class SigningValidator extends Validator {
    public static void validateSigning(JReleaserContext context, JReleaserContext.Mode mode, Errors errors) {
        if (!mode.validateConfig()) {
            return;
        }

        context.getLogger().debug("signing");
        Signing signing = context.getModel().getSigning();

        if (!signing.resolveEnabled(context.getModel().getProject())) return;

        if (!signing.isArmoredSet()) {
            signing.setArmored(true);
        }

        boolean cosign = signing.resolveMode() == Signing.Mode.COSIGN;

        signing.setPassphrase(
            checkProperty(context,
                cosign ? COSIGN_PASSWORD : GPG_PASSPHRASE,
                "signing.passphrase",
                signing.getPassphrase(),
                errors,
                context.isDryrun()));

        if (signing.resolveMode() == Signing.Mode.COMMAND) {
            signing.getCommand().setExecutable(
                checkProperty(context,
                    GPG_EXECUTABLE,
                    "signing.command.executable",
                    signing.getCommand().getExecutable(),
                    "gpg" + (PlatformUtils.isWindows() ? ".exe" : "")));

            signing.getCommand().setHomeDir(
                checkProperty(context,
                    GPG_HOMEDIR,
                    "signing.command.homeDir",
                    signing.getCommand().getHomeDir(),
                    ""));

            signing.getCommand().setKeyName(
                checkProperty(context,
                    GPG_KEYNAME,
                    "signing.command.keyName",
                    signing.getCommand().getKeyName(),
                    ""));

            signing.getCommand().setPublicKeyring(
                checkProperty(context,
                    GPG_PUBLIC_KEYRING,
                    "signing.command.publicKeyRing",
                    signing.getCommand().getPublicKeyring(),
                    ""));
        } else if (signing.resolveMode() == Signing.Mode.COSIGN) {
            if (isBlank(signing.getCosign().getVersion())) {
                errors.configuration(RB.$("validation_is_missing", "signing.cosign.version"));
            }

            signing.getCosign().setPrivateKeyFile(
                checkProperty(context,
                    COSIGN_PRIVATE_KEY,
                    "signing.cosign.privateKeyFile",
                    signing.getCosign().getPrivateKeyFile(),
                    ""));

            signing.getCosign().setPublicKeyFile(
                checkProperty(context,
                    COSIGN_PUBLIC_KEY,
                    "signing.cosign.publicKeyFile",
                    signing.getCosign().getPublicKeyFile(),
                    ""));
        } else {
            signing.setPublicKey(
                checkProperty(context,
                    GPG_PUBLIC_KEY,
                    "signing.publicKey",
                    signing.getPublicKey(),
                    errors,
                    context.isDryrun()));

            signing.setSecretKey(
                checkProperty(context,
                    GPG_SECRET_KEY,
                    "signing.secretKey",
                    signing.getSecretKey(),
                    errors,
                    context.isDryrun()));
        }
    }
}

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
package org.jreleaser.model.validation;

import org.jreleaser.model.Active;
import org.jreleaser.model.JReleaserContext;
import org.jreleaser.model.Signing;
import org.jreleaser.util.Env;
import org.jreleaser.util.Errors;
import org.jreleaser.util.PlatformUtils;

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

        signing.setPassphrase(
            checkProperty(context,
                GPG_PASSPHRASE,
                "signing.passphrase",
                signing.getPassphrase(),
                errors,
                context.isDryrun()));

        if (signing.resolveMode() == Signing.Mode.COMMAND) {
            signing.setExecutable(
                checkProperty(context,
                    GPG_EXECUTABLE,
                    "signing.executable",
                    signing.getExecutable(),
                    "gpg" + (PlatformUtils.isWindows() ? ".exe" : "")));

            signing.setHomeDir(
                checkProperty(context,
                    GPG_HOMEDIR,
                    "signing.homeDir",
                    signing.getHomeDir(),
                    ""));

            signing.setKeyName(
                checkProperty(context,
                    GPG_KEYNAME,
                    "signing.keyName",
                    signing.getKeyName(),
                    ""));

            signing.setPublicKeyring(
                checkProperty(context,
                    GPG_PUBLIC_KEYRING,
                    "signing.publicKeyRing",
                    signing.getPublicKeyring(),
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

        if (context.isDryrun() &&
            (isBlank(Env.resolve(GPG_EXECUTABLE, signing.getExecutable())) ||
                isBlank(Env.resolve(GPG_PASSPHRASE, signing.getPassphrase())) ||
                isBlank(Env.resolve(GPG_PUBLIC_KEY, signing.getPublicKey())) ||
                isBlank(Env.resolve(GPG_SECRET_KEY, signing.getSecretKey())))) {
            signing.setActive(Active.NEVER);
            signing.disable();
        }
    }
}

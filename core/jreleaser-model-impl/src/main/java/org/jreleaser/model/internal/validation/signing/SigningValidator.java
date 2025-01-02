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
package org.jreleaser.model.internal.validation.signing;

import org.jreleaser.bundle.RB;
import org.jreleaser.model.api.JReleaserContext.Mode;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.internal.deploy.maven.MavenDeployer;
import org.jreleaser.model.internal.signing.Signing;
import org.jreleaser.util.DefaultVersions;
import org.jreleaser.util.Errors;
import org.jreleaser.util.PlatformUtils;

import static org.jreleaser.model.api.signing.Signing.COSIGN_PASSWORD;
import static org.jreleaser.model.api.signing.Signing.COSIGN_PRIVATE_KEY;
import static org.jreleaser.model.api.signing.Signing.COSIGN_PUBLIC_KEY;
import static org.jreleaser.model.api.signing.Signing.GPG_EXECUTABLE;
import static org.jreleaser.model.api.signing.Signing.GPG_HOMEDIR;
import static org.jreleaser.model.api.signing.Signing.GPG_KEYNAME;
import static org.jreleaser.model.api.signing.Signing.GPG_PASSPHRASE;
import static org.jreleaser.model.api.signing.Signing.GPG_PUBLIC_KEY;
import static org.jreleaser.model.api.signing.Signing.GPG_PUBLIC_KEYRING;
import static org.jreleaser.model.api.signing.Signing.GPG_SECRET_KEY;
import static org.jreleaser.model.internal.validation.common.Validator.checkProperty;
import static org.jreleaser.model.internal.validation.common.Validator.resolveActivatable;
import static org.jreleaser.util.StringUtils.isBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public final class SigningValidator {
    private SigningValidator() {
        // noop
    }

    public static void validateSigning(JReleaserContext context, Mode mode, Errors errors) {
        if (!mode.validateConfig() && !mode.validateDeploy()) {
            errors = new Errors();
        }

        context.getLogger().debug("signing");
        Signing signing = context.getModel().getSigning();

        resolveActivatable(context, signing, "signing", "NEVER");
        if (!signing.resolveEnabled(context.getModel().getProject())) {
            context.getLogger().debug(RB.$("validation.disabled"));
            return;
        }

        if (!signing.isArmoredSet()) {
            signing.setArmored(true);
        }

        boolean cosign = signing.resolveMode() == org.jreleaser.model.Signing.Mode.COSIGN;

        signing.setPassphrase(
            checkProperty(context,
                cosign ? COSIGN_PASSWORD : GPG_PASSPHRASE,
                "signing.passphrase",
                signing.getPassphrase(),
                errors,
                context.isDryrun()));

        if (signing.resolveMode() == org.jreleaser.model.Signing.Mode.COMMAND) {
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

            if (signing.isVerify()) {
                signing.getCommand().setPublicKeyring(
                    checkProperty(context,
                        GPG_PUBLIC_KEYRING,
                        "signing.command.publicKeyRing",
                        signing.getCommand().getPublicKeyring(),
                        ""));
            }
        } else if (signing.resolveMode() == org.jreleaser.model.Signing.Mode.COSIGN) {
            if (isBlank(signing.getCosign().getVersion())) {
                signing.getCosign().setVersion(DefaultVersions.getInstance().getCosignVersion());
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
            if (signing.isVerify()) {
                signing.setPublicKey(
                    checkProperty(context,
                        GPG_PUBLIC_KEY,
                        "signing.publicKey",
                        signing.getPublicKey(),
                        errors));
            }

            signing.setSecretKey(
                checkProperty(context,
                    GPG_SECRET_KEY,
                    "signing.secretKey",
                    signing.getSecretKey(),
                    errors));
        }
    }

    public static void postValidateSigning(JReleaserContext context, Mode mode, Errors errors) {
        Signing signing = context.getModel().getSigning();
        if (!signing.isEnabled()) return;

        boolean checkSign = mode == Mode.DEPLOY && context.getModel().getDeploy().getMaven()
            .getActiveDeployers().stream()
            .map(MavenDeployer::isSign)
            .filter(b -> b)
            .findAny()
            .orElseGet(() -> false);

        if (!checkSign) {
            return;
        }

        context.getLogger().debug("signing");

        if (signing.resolveMode() == org.jreleaser.model.Signing.Mode.COMMAND) {
            if (signing.isVerify()) {
                signing.getCommand().setPublicKeyring(
                    checkProperty(context,
                        GPG_PUBLIC_KEYRING,
                        "signing.command.publicKeyRing",
                        signing.getCommand().getPublicKeyring(),
                        ""));
            }
        } else if (signing.resolveMode() != org.jreleaser.model.Signing.Mode.COSIGN) {
            if (signing.isVerify()) {
                signing.setPublicKey(
                    checkProperty(context,
                        GPG_PUBLIC_KEY,
                        "signing.publicKey",
                        signing.getPublicKey(),
                        errors));
            }
        }
    }
}

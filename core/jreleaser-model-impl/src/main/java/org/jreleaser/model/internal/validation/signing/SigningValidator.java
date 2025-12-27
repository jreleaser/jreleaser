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
import org.jreleaser.model.Active;
import org.jreleaser.model.api.JReleaserContext.Mode;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.internal.deploy.maven.MavenDeployer;
import org.jreleaser.model.internal.environment.Environment;
import org.jreleaser.model.internal.signing.Signing;
import org.jreleaser.util.DefaultVersions;
import org.jreleaser.util.Errors;
import org.jreleaser.util.PlatformUtils;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.jreleaser.model.api.signing.Signing.COSIGN_PASSWORD;
import static org.jreleaser.model.api.signing.Signing.COSIGN_PRIVATE_KEY;
import static org.jreleaser.model.api.signing.Signing.COSIGN_PUBLIC_KEY;
import static org.jreleaser.model.api.signing.Signing.COSIGN_SECRET_KEY;
import static org.jreleaser.model.api.signing.Signing.GPG_EXECUTABLE;
import static org.jreleaser.model.api.signing.Signing.GPG_HOMEDIR;
import static org.jreleaser.model.api.signing.Signing.GPG_KEYNAME;
import static org.jreleaser.model.api.signing.Signing.GPG_PASSPHRASE;
import static org.jreleaser.model.api.signing.Signing.GPG_PUBLIC_KEY;
import static org.jreleaser.model.api.signing.Signing.GPG_PUBLIC_KEYRING;
import static org.jreleaser.model.api.signing.Signing.GPG_SECRET_KEY;
import static org.jreleaser.model.api.signing.Signing.MINISIGN_PASSWORD;
import static org.jreleaser.model.api.signing.Signing.MINISIGN_PUBLIC_KEY;
import static org.jreleaser.model.api.signing.Signing.MINISIGN_SECRET_KEY;
import static org.jreleaser.model.internal.validation.common.Validator.checkProperty;
import static org.jreleaser.model.internal.validation.common.Validator.resolveActivatable;
import static org.jreleaser.util.CollectionUtils.listOf;
import static org.jreleaser.util.Env.envKey;
import static org.jreleaser.util.Env.sysKey;
import static org.jreleaser.util.StringUtils.isBlank;
import static org.jreleaser.util.StringUtils.isNotBlank;

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

        boolean activeSet = signing.isActiveSet();
        resolveActivatable(context, signing, "signing", "");

        validatePgp(context, mode, signing.getPgp(), errors);
        validateCosign(context, mode, signing.getCosign(), errors);
        validateMinisign(context, mode, signing.getMinisign(), errors);

        if (!activeSet) {
            boolean enabled = signing.getPgp().isEnabled() ||
                signing.getCosign().isEnabled() ||
                signing.getMinisign().isEnabled();
            if (enabled) {
                signing.setActive(Active.ALWAYS);
                signing.resolveEnabled(context.getModel().getProject());
            } else {
                signing.setActive(Active.NEVER);

            }
        }
        signing.resolveEnabled(context.getModel().getProject());
    }

    private static void validatePgp(JReleaserContext context, Mode mode, Signing.Pgp pgp, Errors errors) {
        context.getLogger().debug("signing.pgp");

        if (null == pgp.getActive()) {
            pgp.setActive(context.getModel().getSigning().getActive());
        }

        resolveActivatable(context, pgp, "signing.pgp", "NEVER");
        if (!pgp.resolveEnabled(context.getModel().getProject())) {
            context.getLogger().debug(RB.$("validation.disabled"));
            return;
        }

        if (!pgp.isArmoredSet()) {
            pgp.setArmored(true);
        }

        String passphrase = validatePassphrase(context, GPG_PASSPHRASE, pgp.getPassphrase(), "signing.pgp.passphrase");
        pgp.setPassphrase(passphrase);

        if (pgp.resolveMode() == org.jreleaser.model.Signing.Mode.COMMAND) {
            pgp.getCommand().setExecutable(
                checkProperty(context,
                    GPG_EXECUTABLE,
                    "signing.pgp.command.executable",
                    pgp.getCommand().getExecutable(),
                    "gpg" + (PlatformUtils.isWindows() ? ".exe" : "")));

            pgp.getCommand().setHomeDir(
                checkProperty(context,
                    GPG_HOMEDIR,
                    "signing.pgp.command.homeDir",
                    pgp.getCommand().getHomeDir(),
                    ""));

            pgp.getCommand().setKeyName(
                checkProperty(context,
                    GPG_KEYNAME,
                    "signing.pgp.command.keyName",
                    pgp.getCommand().getKeyName(),
                    ""));

            if (pgp.isVerify()) {
                pgp.getCommand().setPublicKeyring(
                    checkProperty(context,
                        GPG_PUBLIC_KEYRING,
                        "signing.pgp.command.publicKeyRing",
                        pgp.getCommand().getPublicKeyring(),
                        ""));
            }
        } else {
            if (pgp.isVerify()) {
                pgp.setPublicKey(
                    checkProperty(context,
                        GPG_PUBLIC_KEY,
                        "signing.pgp.publicKey",
                        pgp.getPublicKey(),
                        errors));
            }

            pgp.setSecretKey(
                checkProperty(context,
                    GPG_SECRET_KEY,
                    "signing.pgp.secretKey",
                    pgp.getSecretKey(),
                    errors));
        }
    }

    private static void validateCosign(JReleaserContext context, Mode mode, Signing.Cosign cosign, Errors errors) {
        context.getLogger().debug("signing.cosign");
        Signing signing = context.getModel().getSigning();

        resolveActivatable(context, cosign, "signing.cosign", "NEVER");
        if (!cosign.resolveEnabled(context.getModel().getProject())) {
            context.getLogger().debug(RB.$("validation.disabled"));
            return;
        }

        String passphrase = validatePassphrase(context, COSIGN_PASSWORD, cosign.getPassphrase(), "signing.cosign.passphrase");
        cosign.setPassphrase(passphrase);

        if (isBlank(cosign.getPassphrase()) && isNotBlank(signing.getPgp().getPassphrase())) {
            cosign.setPassphrase(signing.getPgp().getPassphrase());
        }

        if (isBlank(cosign.getVersion())) {
            cosign.setVersion(DefaultVersions.getInstance().getCosignVersion());
        }

        cosign.setSecretKeyFile(
            checkProperty(context,
                listOf(COSIGN_PRIVATE_KEY, COSIGN_SECRET_KEY),
                "signing.cosign.secretKeyFile",
                cosign.getSecretKeyFile(),
                ""));

        cosign.setPublicKeyFile(
            checkProperty(context,
                COSIGN_PUBLIC_KEY,
                "signing.cosign.publicKeyFile",
                cosign.getPublicKeyFile(),
                ""));
    }

    private static void validateMinisign(JReleaserContext context, Mode mode, Signing.Minisign minisign, Errors errors) {
        context.getLogger().debug("signing.minisign");
        Signing signing = context.getModel().getSigning();

        resolveActivatable(context, minisign, "signing.minisign", "NEVER");
        if (!minisign.resolveEnabled(context.getModel().getProject())) {
            context.getLogger().debug(RB.$("validation.disabled"));
            return;
        }

        String passphrase = validatePassphrase(context, MINISIGN_PASSWORD, minisign.getPassphrase(), "signing.minisign.passphrase");
        minisign.setPassphrase(passphrase);

        if (isBlank(minisign.getPassphrase()) && isNotBlank(signing.getPgp().getPassphrase())) {
            minisign.setPassphrase(signing.getPgp().getPassphrase());
        }

        if (isBlank(minisign.getVersion())) {
            minisign.setVersion(DefaultVersions.getInstance().getMinisignVersion());
        }

        minisign.setSecretKeyFile(
            checkProperty(context,
                MINISIGN_SECRET_KEY,
                "signing.minisign.secretKeyFile",
                minisign.getSecretKeyFile(),
                ""));

        minisign.setPublicKeyFile(
            checkProperty(context,
                MINISIGN_PUBLIC_KEY,
                "signing.minisign.publicKeyFile",
                minisign.getPublicKeyFile(),
                ""));

        minisign.setPublicKeyFile(minisign.getResolvedPublicKeyFilePath(context).toString());

        Path secretKeyFile = signing.getMinisign().getResolvedSecretKeyFilePath(context);
        Path publicKeyFile = signing.getMinisign().getResolvedPublicKeyFilePath(context);

        if (!Files.exists(secretKeyFile)) {
            errors.configuration(RB.$("validation_directory_not_exist", "minisign.secretKeyFile", secretKeyFile));
        }
        if (!Files.exists(publicKeyFile)) {
            errors.configuration(RB.$("validation_directory_not_exist", "minisign.publicKeyFile", publicKeyFile));
        }
    }

    private static String validatePassphrase(JReleaserContext context, String passphraseKey, String value, String propertyKey) {
        String passphrase = checkProperty(context,
            passphraseKey,
            propertyKey,
            value,
            new Errors(),
            false);

        if (isBlank(passphrase)) {
            Environment environment = context.getModel().getEnvironment();
            String dsl = context.getConfigurer().toString();
            String configFilePath = environment.getPropertiesFile().toAbsolutePath().normalize().toString();
            String envKey = envKey(passphraseKey);
            String sysKey = sysKey(passphraseKey);
            context.getLogger().warn(RB.$("signing.passphrase.blank", dsl, sysKey, envKey, configFilePath, passphraseKey));
            passphrase = "";
        }

        return passphrase;
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

        context.getLogger().debug("signing.pgp");

        Signing.Pgp pgp = signing.getPgp();
        if (pgp.isEnabled()) {
            if (pgp.resolveMode() == org.jreleaser.model.Signing.Mode.COMMAND) {
                if (pgp.isVerify()) {
                    pgp.getCommand().setPublicKeyring(
                        checkProperty(context,
                            GPG_PUBLIC_KEYRING,
                            "signing.pgp.command.publicKeyRing",
                            pgp.getCommand().getPublicKeyring(),
                            ""));
                }
            } else if (pgp.resolveMode() == org.jreleaser.model.Signing.Mode.MEMORY ||
                pgp.resolveMode() == org.jreleaser.model.Signing.Mode.FILE) {
                if (pgp.isVerify()) {
                    pgp.setPublicKey(
                        checkProperty(context,
                            GPG_PUBLIC_KEY,
                            "signing.pgp.publicKey",
                            pgp.getPublicKey(),
                            errors));
                }
            }
        }
    }
}

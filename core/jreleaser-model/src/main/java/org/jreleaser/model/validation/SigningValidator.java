/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2021 Andres Almiray.
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

import static org.jreleaser.model.Signing.GPG_PASSPHRASE;
import static org.jreleaser.model.Signing.GPG_PUBLIC_KEY;
import static org.jreleaser.model.Signing.GPG_SECRET_KEY;
import static org.jreleaser.util.StringUtils.isBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public abstract class SigningValidator extends Validator {
    public static void validateSigning(JReleaserContext context, JReleaserContext.Mode mode, Errors errors) {
        if (mode != JReleaserContext.Mode.FULL) {
            return;
        }

        context.getLogger().debug("signing");
        Signing signing = context.getModel().getSigning();

        if (!signing.resolveEnabled(context.getModel().getProject())) return;

        if (!signing.isArmoredSet()) {
            signing.setArmored(true);
        }

        signing.setPassphrase(
            checkProperty(context.getModel().getEnvironment(),
                GPG_PASSPHRASE,
                "signing.passphrase",
                signing.getPassphrase(),
                errors,
                context.isDryrun()));

        signing.setPublicKey(
            checkProperty(context.getModel().getEnvironment(),
                GPG_PUBLIC_KEY,
                "signing.publicKey",
                signing.getPublicKey(),
                errors,
                context.isDryrun()));

        signing.setSecretKey(
            checkProperty(context.getModel().getEnvironment(),
                GPG_SECRET_KEY,
                "signing.secretKey",
                signing.getSecretKey(),
                errors,
                context.isDryrun()));

        if (context.isDryrun() &&
            (isBlank(Env.resolve(GPG_PASSPHRASE, signing.getPassphrase())) ||
                isBlank(Env.resolve(GPG_PUBLIC_KEY, signing.getPublicKey())) ||
                isBlank(Env.resolve(GPG_SECRET_KEY, signing.getSecretKey())))) {
            signing.setActive(Active.NEVER);
            signing.disable();
        }
    }
}

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

import org.jreleaser.model.JReleaserContext;
import org.jreleaser.model.Signing;

import java.util.List;

import static org.jreleaser.model.Signing.GPG_PASSPHRASE;
import static org.jreleaser.model.Signing.GPG_PUBLIC_KEY;
import static org.jreleaser.model.Signing.GPG_SECRET_KEY;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public abstract class SignValidator extends Validator {
    public static void validateSign(JReleaserContext context, List<String> errors) {
        Signing signing = context.getModel().getSigning();

        if (!signing.isEnabled()) return;

        if (!signing.isArmoredSet()) {
            signing.setArmored(true);
        }

        signing.setPassphrase(
            checkProperty(context.getModel().getEnvironment(),
                GPG_PASSPHRASE,
                "signing.secretKey",
                signing.getPassphrase(),
                errors));

        signing.setPublicKey(
            checkProperty(context.getModel().getEnvironment(),
                GPG_PUBLIC_KEY,
                "signing.secretKey",
                signing.getPublicKey(),
                errors));

        signing.setSecretKey(
            checkProperty(context.getModel().getEnvironment(),
                GPG_SECRET_KEY,
                "signing.secretKey",
                signing.getSecretKey(),
                errors));
    }
}

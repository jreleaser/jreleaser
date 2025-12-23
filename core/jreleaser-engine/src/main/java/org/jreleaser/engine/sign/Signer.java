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
package org.jreleaser.engine.sign;

import org.jreleaser.bundle.RB;
import org.jreleaser.model.api.signing.SigningException;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.internal.signing.Signing;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public final class Signer {
    private Signer() {
        // noop
    }

    public static void sign(JReleaserContext context) throws SigningException {
        context.getLogger().info(RB.$("signing.header"));
        context.getLogger().increaseIndent();
        context.getLogger().setPrefix("sign");

        if (!context.getModel().getSigning().isEnabled()) {
            context.getLogger().info(RB.$("signing.not.enabled"));
            context.getLogger().restorePrefix();
            context.getLogger().decreaseIndent();
            return;
        }

        try {
            Signing signing = context.getModel().getSigning();
            if (signing.getPgp().isEnabled()) {
                try {
                    context.getLogger().setPrefix("gpg");
                    new PgpSigner(context, signing.getPgp()).sign();
                } finally {
                    context.getLogger().restorePrefix();
                }
            }
            if (signing.getCosign().isEnabled()) {
                try {
                    context.getLogger().setPrefix("cosign");
                    new CosignSigner(context, signing.getCosign()).sign();
                } finally {
                    context.getLogger().restorePrefix();
                }
            }
            if (signing.getMinisign().isEnabled()) {
                try {
                    context.getLogger().setPrefix("minisign");
                    new MinisignSigner(context, signing.getMinisign()).sign();
                } finally {
                    context.getLogger().restorePrefix();
                }
            }
        } finally {
            context.getLogger().restorePrefix();
            context.getLogger().decreaseIndent();
        }
    }
}

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
import org.jreleaser.model.Gitter;
import org.jreleaser.model.JReleaserContext;
import org.jreleaser.util.Errors;

import java.nio.file.Files;

import static org.jreleaser.model.Gitter.GITTER_WEBHOOK;
import static org.jreleaser.util.StringUtils.isBlank;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.2.0
 */
public abstract class GitterValidator extends Validator {
    private static final String DEFAULT_GITTER_TPL = "src/jreleaser/templates/gitter.tpl";

    public static void validateGitter(JReleaserContext context, Gitter gitter, Errors errors) {
        if (!gitter.resolveEnabled(context.getModel().getProject())) return;
        context.getLogger().debug("announce.gitter");

        gitter.setWebhook(
            checkProperty(context,
                GITTER_WEBHOOK,
                "gitter.webhook",
                gitter.getWebhook(),
                errors,
                context.isDryrun()));

        if (isBlank(gitter.getMessage()) && isBlank(gitter.getMessageTemplate())) {
            if (Files.exists(context.getBasedir().resolve(DEFAULT_GITTER_TPL))) {
                gitter.setMessageTemplate(DEFAULT_GITTER_TPL);
            } else {
                gitter.setMessage(RB.$("default.release.message"));
            }
        }

        if (isNotBlank(gitter.getMessageTemplate()) &&
            !Files.exists(context.getBasedir().resolve(gitter.getMessageTemplate().trim()))) {
            errors.configuration(RB.$("validation_directory_not_exist", "gitter.messageTemplate", gitter.getMessageTemplate()));
        }

        validateTimeout(gitter);
    }
}
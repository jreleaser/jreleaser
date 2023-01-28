/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2023 The JReleaser authors.
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
package org.jreleaser.model.internal.validation.announce;

import org.jreleaser.bundle.RB;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.internal.announce.GitterAnnouncer;
import org.jreleaser.util.Errors;

import java.nio.file.Files;

import static org.jreleaser.model.api.announce.GitterAnnouncer.GITTER_WEBHOOK;
import static org.jreleaser.model.internal.validation.common.Validator.checkProperty;
import static org.jreleaser.model.internal.validation.common.Validator.resolveActivatable;
import static org.jreleaser.model.internal.validation.common.Validator.validateTimeout;
import static org.jreleaser.util.CollectionUtils.listOf;
import static org.jreleaser.util.StringUtils.isBlank;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.2.0
 */
public final class GitterAnnouncerValidator {
    private static final String DEFAULT_GITTER_TPL = "src/jreleaser/templates/gitter.tpl";

    private GitterAnnouncerValidator() {
        // noop
    }

    public static void validateGitter(JReleaserContext context, GitterAnnouncer gitter, Errors errors) {
        context.getLogger().debug("announce.gitter");
        resolveActivatable(context, gitter, "announce.gitter", "NEVER");
        if (!gitter.resolveEnabledWithSnapshot(context.getModel().getProject())) {
            context.getLogger().debug(RB.$("validation.disabled"));
            return;
        }

        gitter.setWebhook(
            checkProperty(context,
                listOf(
                    "announce.gitter.webhook",
                    GITTER_WEBHOOK),
                "announce.gitter.webhook",
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
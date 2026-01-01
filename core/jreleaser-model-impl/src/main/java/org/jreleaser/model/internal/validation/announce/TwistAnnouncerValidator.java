/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2026 The JReleaser authors.
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
import org.jreleaser.model.internal.announce.TwistAnnouncer;
import org.jreleaser.util.Errors;

import java.nio.file.Files;

import static org.jreleaser.model.api.announce.TwistAnnouncer.TWIST_WEBHOOK;
import static org.jreleaser.model.internal.validation.common.Validator.checkProperty;
import static org.jreleaser.model.internal.validation.common.Validator.resolveActivatable;
import static org.jreleaser.model.internal.validation.common.Validator.validateTimeout;
import static org.jreleaser.util.CollectionUtils.listOf;
import static org.jreleaser.util.StringUtils.isBlank;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Usman Shaikh
 * @since 1.23.0
 */
public final class TwistAnnouncerValidator {
    private static final String DEFAULT_TWIST_TPL = "src/jreleaser/templates/twist.tpl";

    private TwistAnnouncerValidator() {
        // noop
    }

    public static void validateTwist(JReleaserContext context, TwistAnnouncer announcer, Errors errors) {
        context.getLogger().debug("announce.twist");
        resolveActivatable(context, announcer, "announce.twist", "NEVER");
        if (!announcer.resolveEnabledWithSnapshot(context.getModel().getProject())) {
            context.getLogger().debug(RB.$("validation.disabled"));
            return;
        }

        announcer.setWebhook(
            checkProperty(context,
                listOf(
                    "announce.twist.webhook",
                    TWIST_WEBHOOK),
                "announce.twist.webhook",
                announcer.getWebhook(),
                errors,
                context.isDryrun()));

        if (isBlank(announcer.getMessageTemplate())) {
            announcer.setMessageTemplate(DEFAULT_TWIST_TPL);
        }

        if (isNotBlank(announcer.getMessageTemplate()) &&
            !Files.exists(context.getBasedir().resolve(announcer.getMessageTemplate().trim()))) {
            errors.configuration(RB.$("validation_directory_not_exist", "twist.messageTemplate", announcer.getMessageTemplate()));
            announcer.disable();
            return;
        }

        validateTimeout(announcer);
    }
}

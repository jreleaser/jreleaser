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
package org.jreleaser.model.internal.validation.announce;

import org.jreleaser.bundle.RB;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.internal.announce.GoogleChatAnnouncer;
import org.jreleaser.util.Errors;

import java.nio.file.Files;

import static org.jreleaser.model.api.announce.GoogleChatAnnouncer.GOOGLE_CHAT_WEBHOOK;
import static org.jreleaser.model.internal.validation.common.Validator.checkProperty;
import static org.jreleaser.model.internal.validation.common.Validator.resolveActivatable;
import static org.jreleaser.model.internal.validation.common.Validator.validateTimeout;
import static org.jreleaser.util.CollectionUtils.listOf;
import static org.jreleaser.util.StringUtils.isBlank;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Anyul Rivas
 * @since 0.5.0
 */
public final class GoogleChatAnnouncerValidator {
    private static final String DEFAULT_GOOGLE_CHAT_TPL = "src/jreleaser/templates/googleChat.tpl";

    private GoogleChatAnnouncerValidator() {
        // noop
    }

    public static void validateGoogleChat(JReleaserContext context, GoogleChatAnnouncer announcer, Errors errors) {
        context.getLogger().debug("announce.googleChat");
        resolveActivatable(context, announcer, "announce.google.chat", "NEVER");
        if (!announcer.resolveEnabledWithSnapshot(context.getModel().getProject())) {
            context.getLogger().debug(RB.$("validation.disabled"));
            return;
        }

        announcer.setWebhook(
            checkProperty(context,
                listOf(
                    "announce.google.chat.webhook",
                    GOOGLE_CHAT_WEBHOOK),
                "announce.googleChat.webhook",
                announcer.getWebhook(),
                errors,
                context.isDryrun()));

        if (isBlank(announcer.getMessage()) && isBlank(announcer.getMessageTemplate())) {
            if (Files.exists(context.getBasedir().resolve(DEFAULT_GOOGLE_CHAT_TPL))) {
                announcer.setMessageTemplate(DEFAULT_GOOGLE_CHAT_TPL);
            } else {
                announcer.setMessage(RB.$("default.release.message"));
            }
        }

        if (isNotBlank(announcer.getMessageTemplate()) &&
            !Files.exists(context.getBasedir().resolve(announcer.getMessageTemplate().trim()))) {
            errors.configuration(RB.$("validation_directory_not_exist", "googleChat.messageTemplate", announcer.getMessageTemplate()));
        }

        validateTimeout(announcer);
    }
}
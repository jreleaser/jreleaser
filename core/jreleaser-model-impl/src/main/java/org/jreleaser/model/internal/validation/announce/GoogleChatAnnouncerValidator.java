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

    public static void validateGoogleChat(JReleaserContext context, GoogleChatAnnouncer googleChat, Errors errors) {
        context.getLogger().debug("announce.googleChat");
        resolveActivatable(context, googleChat, "announce.google.chat", "NEVER");
        if (!googleChat.resolveEnabledWithSnapshot(context.getModel().getProject())) {
            context.getLogger().debug(RB.$("validation.disabled"));
            return;
        }

        googleChat.setWebhook(
            checkProperty(context,
                listOf(
                    "announce.google.chat.webhook",
                    GOOGLE_CHAT_WEBHOOK),
                "announce.googleChat.webhook",
                googleChat.getWebhook(),
                errors,
                context.isDryrun()));

        if (isBlank(googleChat.getMessage()) && isBlank(googleChat.getMessageTemplate())) {
            if (Files.exists(context.getBasedir().resolve(DEFAULT_GOOGLE_CHAT_TPL))) {
                googleChat.setMessageTemplate(DEFAULT_GOOGLE_CHAT_TPL);
            } else {
                googleChat.setMessage(RB.$("default.release.message"));
            }
        }

        if (isNotBlank(googleChat.getMessageTemplate()) &&
            !Files.exists(context.getBasedir().resolve(googleChat.getMessageTemplate().trim()))) {
            errors.configuration(RB.$("validation_directory_not_exist", "googleChat.messageTemplate", googleChat.getMessageTemplate()));
        }

        validateTimeout(googleChat);
    }
}
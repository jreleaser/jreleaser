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
import org.jreleaser.model.GoogleChat;
import org.jreleaser.model.JReleaserContext;
import org.jreleaser.util.Errors;

import java.nio.file.Files;

import static org.jreleaser.model.GoogleChat.GOOGLE_CHAT_WEBHOOK;
import static org.jreleaser.util.StringUtils.isBlank;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Anyul Rivas
 * @since 0.5.0
 */
public abstract class GoogleChatValidator extends Validator {
    private static final String DEFAULT_GOOGLE_CHAT_TPL = "src/jreleaser/templates/googleChat.tpl";

    public static void validateGoogleChat(JReleaserContext context, GoogleChat googleChat, Errors errors) {
        if (!googleChat.resolveEnabled(context.getModel().getProject())) return;
        context.getLogger().debug("announce.googleChat");

        googleChat.setWebhook(
            checkProperty(context,
                GOOGLE_CHAT_WEBHOOK,
                "googleChat.webhook",
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
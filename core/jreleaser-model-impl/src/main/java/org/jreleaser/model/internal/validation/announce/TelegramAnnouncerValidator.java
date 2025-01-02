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
import org.jreleaser.model.internal.announce.TelegramAnnouncer;
import org.jreleaser.util.Errors;

import java.nio.file.Files;

import static org.jreleaser.model.api.announce.TelegramAnnouncer.TELEGRAM_CHAT_ID;
import static org.jreleaser.model.api.announce.TelegramAnnouncer.TELEGRAM_TOKEN;
import static org.jreleaser.model.internal.validation.common.Validator.checkProperty;
import static org.jreleaser.model.internal.validation.common.Validator.resolveActivatable;
import static org.jreleaser.model.internal.validation.common.Validator.validateTimeout;
import static org.jreleaser.util.CollectionUtils.listOf;
import static org.jreleaser.util.StringUtils.isBlank;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.8.0
 */
public final class TelegramAnnouncerValidator {
    private static final String DEFAULT_TELEGRAM_TPL = "src/jreleaser/templates/telegram.tpl";

    private TelegramAnnouncerValidator() {
        // noop
    }

    public static void validateTelegram(JReleaserContext context, TelegramAnnouncer announcer, Errors errors) {
        context.getLogger().debug("announce.telegram");
        resolveActivatable(context, announcer, "announce.telegram", "NEVER");
        if (!announcer.resolveEnabledWithSnapshot(context.getModel().getProject())) {
            context.getLogger().debug(RB.$("validation.disabled"));
            return;
        }

        announcer.setToken(
            checkProperty(context,
                listOf(
                    "announce.telegram.token",
                    TELEGRAM_TOKEN),
                "announce.telegram.token",
                announcer.getToken(),
                errors,
                context.isDryrun()));

        announcer.setChatId(
            checkProperty(context,
                listOf(
                    "announce.telegram.chat.id",
                    TELEGRAM_CHAT_ID),
                "announce.telegram.chatId",
                announcer.getChatId(),
                errors,
                context.isDryrun()));

        if (isBlank(announcer.getMessage()) && isBlank(announcer.getMessageTemplate())) {
            if (Files.exists(context.getBasedir().resolve(DEFAULT_TELEGRAM_TPL))) {
                announcer.setMessageTemplate(DEFAULT_TELEGRAM_TPL);
            } else {
                announcer.setMessage(RB.$("default.release.message"));
            }
        }

        if (isNotBlank(announcer.getMessageTemplate()) &&
            !Files.exists(context.getBasedir().resolve(announcer.getMessageTemplate().trim()))) {
            errors.configuration(RB.$("validation_directory_not_exist", "telegram.messageTemplate", announcer.getMessageTemplate()));
        }

        validateTimeout(announcer);
    }
}
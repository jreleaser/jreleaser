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
package org.jreleaser.model.internal.validation.announce;

import org.jreleaser.bundle.RB;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.internal.announce.TelegramAnnouncer;
import org.jreleaser.model.internal.validation.common.Validator;
import org.jreleaser.util.Errors;

import java.nio.file.Files;

import static org.jreleaser.model.api.announce.TelegramAnnouncer.TELEGRAM_CHAT_ID;
import static org.jreleaser.model.api.announce.TelegramAnnouncer.TELEGRAM_TOKEN;
import static org.jreleaser.util.StringUtils.isBlank;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.8.0
 */
public abstract class TelegramAnnouncerValidator extends Validator {
    private static final String DEFAULT_TELEGRAM_TPL = "src/jreleaser/templates/telegram.tpl";

    public static void validateTelegram(JReleaserContext context, TelegramAnnouncer telegram, Errors errors) {
        context.getLogger().debug("announce.telegram");
        if (!telegram.resolveEnabled(context.getModel().getProject())) {
            context.getLogger().debug(RB.$("validation.disabled"));
            return;
        }

        telegram.setToken(
            checkProperty(context,
                TELEGRAM_TOKEN,
                "telegram.token",
                telegram.getToken(),
                errors,
                context.isDryrun()));

        telegram.setChatId(
            checkProperty(context,
                TELEGRAM_CHAT_ID,
                "telegram.chatId",
                telegram.getChatId(),
                errors,
                context.isDryrun()));

        if (isBlank(telegram.getMessage()) && isBlank(telegram.getMessageTemplate())) {
            if (Files.exists(context.getBasedir().resolve(DEFAULT_TELEGRAM_TPL))) {
                telegram.setMessageTemplate(DEFAULT_TELEGRAM_TPL);
            } else {
                telegram.setMessage(RB.$("default.release.message"));
            }
        }

        if (isNotBlank(telegram.getMessageTemplate()) &&
            !Files.exists(context.getBasedir().resolve(telegram.getMessageTemplate().trim()))) {
            errors.configuration(RB.$("validation_directory_not_exist", "telegram.messageTemplate", telegram.getMessageTemplate()));
        }

        validateTimeout(telegram);
    }
}
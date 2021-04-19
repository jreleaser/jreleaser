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

import org.jreleaser.model.Discord;
import org.jreleaser.model.JReleaserContext;
import org.jreleaser.util.Errors;

import java.nio.file.Files;

import static org.jreleaser.model.Discord.DISCORD_WEBHOOK;
import static org.jreleaser.util.StringUtils.isBlank;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.2.0
 */
public abstract class DiscordValidator extends Validator {
    private static final String DEFAULT_DISCORD_TPL = "src/jreleaser/templates/discord.tpl";

    public static void validateDiscord(JReleaserContext context, Discord discord, Errors errors) {
        if (!discord.resolveEnabled(context.getModel().getProject())) return;
        context.getLogger().debug("announce.discord");

        discord.setWebhook(
            checkProperty(context.getModel().getEnvironment(),
                DISCORD_WEBHOOK,
                "discord.token",
                discord.getWebhook(),
                errors));

        if (isBlank(discord.getMessage()) && isBlank(discord.getMessageTemplate())) {
            if (Files.exists(context.getBasedir().resolve(DEFAULT_DISCORD_TPL))) {
                discord.setMessageTemplate(DEFAULT_DISCORD_TPL);
            } else {
                discord.setMessage("\uD83D\uDE80 {{projectNameCapitalized}} {{projectVersion}} has been released! {{releaseNotesUrl}}");
            }
        }

        if (isNotBlank(discord.getMessageTemplate()) &&
            !Files.exists(context.getBasedir().resolve(discord.getMessageTemplate().trim()))) {
            errors.configuration("discord.messageTemplate does not exist. " + discord.getMessageTemplate());
        }

        if (discord.getConnectTimeout() <= 0 || discord.getConnectTimeout() > 300) {
            discord.setConnectTimeout(20);
        }
        if (discord.getReadTimeout() <= 0 || discord.getReadTimeout() > 300) {
            discord.setReadTimeout(60);
        }
    }
}
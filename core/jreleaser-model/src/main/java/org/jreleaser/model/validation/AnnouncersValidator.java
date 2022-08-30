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
import org.jreleaser.model.Announce;
import org.jreleaser.model.JReleaserContext;
import org.jreleaser.util.Errors;

import static org.jreleaser.model.validation.ArticleValidator.validateArticle;
import static org.jreleaser.model.validation.DiscordValidator.validateDiscord;
import static org.jreleaser.model.validation.DiscussionsValidator.validateDiscussions;
import static org.jreleaser.model.validation.GitterValidator.validateGitter;
import static org.jreleaser.model.validation.GoogleChatValidator.validateGoogleChat;
import static org.jreleaser.model.validation.HttpAnnouncerValidator.validateHttpAnnouncers;
import static org.jreleaser.model.validation.MailValidator.validateMail;
import static org.jreleaser.model.validation.MastodonValidator.validateMastodon;
import static org.jreleaser.model.validation.MattermostValidator.validateMattermost;
import static org.jreleaser.model.validation.SdkmanAnnouncerValidator.validateSdkmanAnnouncer;
import static org.jreleaser.model.validation.SlackValidator.validateSlack;
import static org.jreleaser.model.validation.TeamsValidator.validateTeams;
import static org.jreleaser.model.validation.TelegramValidator.validateTelegram;
import static org.jreleaser.model.validation.TwitterValidator.validateTwitter;
import static org.jreleaser.model.validation.WebhooksValidator.validateWebhooks;
import static org.jreleaser.model.validation.ZulipValidator.validateZulip;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public abstract class AnnouncersValidator extends Validator {
    public static void validateAnnouncers(JReleaserContext context, JReleaserContext.Mode mode, Errors errors) {
        Announce announce = context.getModel().getAnnounce();
        context.getLogger().debug("announce");

        boolean skipValidation = !mode.validateAnnounce() && !mode.validateConfig();
        Errors errorCollector = skipValidation ? new Errors() : errors;
        validateArticle(context, announce.getArticle(), errorCollector);
        validateDiscussions(context, announce.getDiscussions(), errorCollector);
        validateDiscord(context, announce.getDiscord(), errorCollector);
        validateGitter(context, announce.getGitter(), errorCollector);
        validateGoogleChat(context, announce.getGoogleChat(), errorCollector);
        validateHttpAnnouncers(context, announce.getConfiguredHttp(), errorCollector);
        validateMail(context, announce.getMail(), errorCollector);
        validateMastodon(context, announce.getMastodon(), errorCollector);
        validateMattermost(context, announce.getMattermost(), errorCollector);
        validateSdkmanAnnouncer(context, announce.getSdkman(), errorCollector);
        validateSlack(context, announce.getSlack(), errorCollector);
        validateTeams(context, announce.getTeams(), errorCollector);
        validateTelegram(context, announce.getTelegram(), errorCollector);
        validateTwitter(context, announce.getTwitter(), errorCollector);
        validateWebhooks(context, announce.getConfiguredWebhooks(), errorCollector);
        validateZulip(context, announce.getZulip(), errorCollector);

        if (skipValidation) {
            context.getLogger().debug(RB.$("validation.disabled"));
            return;
        }

        boolean activeSet = announce.isActiveSet();
        announce.resolveEnabled(context.getModel().getProject());

        if (announce.isEnabled()) {
            boolean enabled = announce.getArticle().isEnabled() ||
                announce.getDiscord().isEnabled() ||
                announce.getDiscussions().isEnabled() ||
                announce.getGitter().isEnabled() ||
                announce.getGoogleChat().isEnabled() ||
                announce.getConfiguredHttp().isEnabled() ||
                announce.getMail().isEnabled() ||
                announce.getMastodon().isEnabled() ||
                announce.getMattermost().isEnabled() ||
                announce.getSdkman().isEnabled() ||
                announce.getSlack().isEnabled() ||
                announce.getTeams().isEnabled() ||
                announce.getTelegram().isEnabled() ||
                announce.getTwitter().isEnabled() ||
                announce.getConfiguredWebhooks().isEnabled() ||
                announce.getZulip().isEnabled();

            if (!activeSet && !enabled) {
                context.getLogger().debug(RB.$("validation.disabled"));
                announce.disable();
            }
        }
    }
}
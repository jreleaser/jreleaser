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
import org.jreleaser.model.api.JReleaserContext.Mode;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.internal.announce.Announce;
import org.jreleaser.model.internal.validation.common.Validator;
import org.jreleaser.util.Errors;

import static org.jreleaser.model.internal.validation.announce.ArticleAnnouncerValidator.validateArticle;
import static org.jreleaser.model.internal.validation.announce.DiscordAnnouncerValidator.validateDiscord;
import static org.jreleaser.model.internal.validation.announce.DiscussionsAnnouncerValidator.validateDiscussions;
import static org.jreleaser.model.internal.validation.announce.DiscourseAnnouncerValidator.validateDiscourse;
import static org.jreleaser.model.internal.validation.announce.GitterAnnouncerValidator.validateGitter;
import static org.jreleaser.model.internal.validation.announce.GoogleChatAnnouncerValidator.validateGoogleChat;
import static org.jreleaser.model.internal.validation.announce.HttpAnnouncerValidator.validateHttpAnnouncers;
import static org.jreleaser.model.internal.validation.announce.MastodonAnnouncerValidator.validateMastodon;
import static org.jreleaser.model.internal.validation.announce.MattermostAnnouncerValidator.validateMattermost;
import static org.jreleaser.model.internal.validation.announce.SdkmanAnnouncerValidator.validateSdkmanAnnouncer;
import static org.jreleaser.model.internal.validation.announce.SlackAnnouncerValidator.validateSlack;
import static org.jreleaser.model.internal.validation.announce.SmtpAnnouncerValidator.validateSmtp;
import static org.jreleaser.model.internal.validation.announce.TeamsAnnouncerValidator.validateTeams;
import static org.jreleaser.model.internal.validation.announce.TelegramAnnouncerValidator.validateTelegram;
import static org.jreleaser.model.internal.validation.announce.TwitterAnnouncerValidator.validateTwitter;
import static org.jreleaser.model.internal.validation.announce.WebhooksAnnouncerValidator.validateWebhooks;
import static org.jreleaser.model.internal.validation.announce.ZulipAnnouncerValidator.validateZulip;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public abstract class AnnouncersValidator extends Validator {
    public static void validateAnnouncers(JReleaserContext context, Mode mode, Errors errors) {
        Announce announce = context.getModel().getAnnounce();
        context.getLogger().debug("announce");

        if (!mode.validateConfig() && !mode.validateAnnounce()) {
            context.getLogger().debug(RB.$("validation.disabled"));
            return;
        }

        validateArticle(context, announce.getArticle(), errors);
        validateDiscussions(context, announce.getDiscussions(), errors);
        validateDiscord(context, announce.getDiscord(), errors);
        validateDiscourse(context, announce.getDiscourse(), errors);
        validateGitter(context, announce.getGitter(), errors);
        validateGoogleChat(context, announce.getGoogleChat(), errors);
        validateHttpAnnouncers(context, mode, announce.getConfiguredHttp(), errors);
        validateSmtp(context, announce.getMail(), errors);
        validateMastodon(context, announce.getMastodon(), errors);
        validateMattermost(context, announce.getMattermost(), errors);
        validateSdkmanAnnouncer(context, announce.getSdkman(), errors);
        validateSlack(context, announce.getSlack(), errors);
        validateTeams(context, announce.getTeams(), errors);
        validateTelegram(context, announce.getTelegram(), errors);
        validateTwitter(context, announce.getTwitter(), errors);
        validateWebhooks(context, mode, announce.getConfiguredWebhooks(), errors);
        validateZulip(context, announce.getZulip(), errors);

        boolean activeSet = announce.isActiveSet();
        announce.resolveEnabled(context.getModel().getProject());

        if (announce.isEnabled()) {
            boolean enabled = announce.getArticle().isEnabled() ||
                announce.getDiscord().isEnabled() ||
                announce.getDiscourse().isEnabled() ||
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
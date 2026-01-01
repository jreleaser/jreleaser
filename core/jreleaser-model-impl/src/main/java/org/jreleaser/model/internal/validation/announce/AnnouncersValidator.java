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
import org.jreleaser.model.api.JReleaserContext.Mode;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.internal.announce.Announce;
import org.jreleaser.util.Errors;

import static org.jreleaser.model.internal.validation.announce.ArticleAnnouncerValidator.validateArticle;
import static org.jreleaser.model.internal.validation.announce.BlueskyAnnouncerValidator.validateBluesky;
import static org.jreleaser.model.internal.validation.announce.DiscordAnnouncerValidator.validateDiscord;
import static org.jreleaser.model.internal.validation.announce.DiscourseAnnouncerValidator.validateDiscourse;
import static org.jreleaser.model.internal.validation.announce.DiscussionsAnnouncerValidator.validateDiscussions;
import static org.jreleaser.model.internal.validation.announce.GitterAnnouncerValidator.validateGitter;
import static org.jreleaser.model.internal.validation.announce.GoogleChatAnnouncerValidator.validateGoogleChat;
import static org.jreleaser.model.internal.validation.announce.HttpAnnouncerValidator.validateHttpAnnouncers;
import static org.jreleaser.model.internal.validation.announce.LinkedinAnnouncerValidator.validateLinkedin;
import static org.jreleaser.model.internal.validation.announce.MastodonAnnouncerValidator.validateMastodon;
import static org.jreleaser.model.internal.validation.announce.MattermostAnnouncerValidator.validateMattermost;
import static org.jreleaser.model.internal.validation.announce.OpenCollectiveAnnouncerValidator.validateOpenCollective;
import static org.jreleaser.model.internal.validation.announce.RedditAnnouncerValidator.validateReddit;
import static org.jreleaser.model.internal.validation.announce.SdkmanAnnouncerValidator.validateSdkmanAnnouncer;
import static org.jreleaser.model.internal.validation.announce.SlackAnnouncerValidator.validateSlack;
import static org.jreleaser.model.internal.validation.announce.SmtpAnnouncerValidator.validateSmtp;
import static org.jreleaser.model.internal.validation.announce.TeamsAnnouncerValidator.validateTeams;
import static org.jreleaser.model.internal.validation.announce.TelegramAnnouncerValidator.validateTelegram;
import static org.jreleaser.model.internal.validation.announce.TwistAnnouncerValidator.validateTwist;
import static org.jreleaser.model.internal.validation.announce.TwitterAnnouncerValidator.validateTwitter;
import static org.jreleaser.model.internal.validation.announce.WebhooksAnnouncerValidator.validateWebhooks;
import static org.jreleaser.model.internal.validation.announce.ZulipAnnouncerValidator.validateZulip;
import static org.jreleaser.model.internal.validation.common.Validator.mergeErrors;
import static org.jreleaser.model.internal.validation.common.Validator.resolveActivatable;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public final class AnnouncersValidator {
    private AnnouncersValidator() {
        // noop
    }

    public static void validateAnnouncers(JReleaserContext context, Mode mode, Errors errors) {
        Announce announce = context.getModel().getAnnounce();
        context.getLogger().debug("announce");

        if (!mode.validateConfig() && !mode.validateAnnounce()) {
            context.getLogger().debug(RB.$("validation.disabled"));
            return;
        }

        Errors incoming = new Errors();
        validateArticle(context, announce.getArticle(), incoming);
        mergeErrors(context, errors, incoming, announce.getArticle());
        validateBluesky(context, announce.getBluesky(), incoming);
        mergeErrors(context, errors, incoming, announce.getBluesky());
        validateDiscussions(context, announce.getDiscussions(), incoming);
        mergeErrors(context, errors, incoming, announce.getDiscussions());
        validateDiscord(context, announce.getDiscord(), incoming);
        mergeErrors(context, errors, incoming, announce.getDiscord());
        validateDiscourse(context, announce.getDiscourse(), incoming);
        mergeErrors(context, errors, incoming, announce.getDiscourse());
        validateGitter(context, announce.getGitter(), incoming);
        mergeErrors(context, errors, incoming, announce.getGitter());
        validateGoogleChat(context, announce.getGoogleChat(), incoming);
        mergeErrors(context, errors, incoming, announce.getGoogleChat());
        validateLinkedin(context, announce.getLinkedin(), incoming);
        mergeErrors(context, errors, incoming, announce.getLinkedin());
        validateHttpAnnouncers(context, mode, announce.getConfiguredHttp(), incoming);
        mergeErrors(context, errors, incoming, announce.getConfiguredHttp());
        validateSmtp(context, announce.getSmtp(), incoming);
        mergeErrors(context, errors, incoming, announce.getSmtp());
        validateMastodon(context, announce.getMastodon(), incoming);
        mergeErrors(context, errors, incoming, announce.getMastodon());
        validateMattermost(context, announce.getMattermost(), incoming);
        mergeErrors(context, errors, incoming, announce.getMattermost());
        validateReddit(context, announce.getReddit(), incoming);
        mergeErrors(context, errors, incoming, announce.getReddit());
        validateOpenCollective(context, announce.getOpenCollective(), incoming);
        mergeErrors(context, errors, incoming, announce.getOpenCollective());
        validateSdkmanAnnouncer(context, announce.getSdkman(), incoming);
        mergeErrors(context, errors, incoming, announce.getSdkman());
        validateSlack(context, announce.getSlack(), incoming);
        mergeErrors(context, errors, incoming, announce.getSlack());
        validateTeams(context, announce.getTeams(), incoming);
        mergeErrors(context, errors, incoming, announce.getTeams());
        validateTelegram(context, announce.getTelegram(), incoming);
        mergeErrors(context, errors, incoming, announce.getTelegram());
        validateTwist(context, announce.getTwist(), incoming);
        mergeErrors(context, errors, incoming, announce.getTwist());
        validateTwitter(context, announce.getTwitter(), incoming);
        mergeErrors(context, errors, incoming, announce.getTwitter());
        validateWebhooks(context, mode, announce.getConfiguredWebhooks(), incoming);
        mergeErrors(context, errors, incoming, announce.getConfiguredWebhooks());
        validateZulip(context, announce.getZulip(), incoming);
        mergeErrors(context, errors, incoming, announce.getZulip());

        boolean activeSet = announce.isActiveSet();
        resolveActivatable(context, announce, "announce", "ALWAYS");
        announce.resolveEnabled(context.getModel().getProject());

        if (announce.isEnabled()) {
            boolean enabled = announce.getArticle().isEnabled() ||
                announce.getBluesky().isEnabled() ||
                announce.getDiscord().isEnabled() ||
                announce.getDiscourse().isEnabled() ||
                announce.getDiscussions().isEnabled() ||
                announce.getGitter().isEnabled() ||
                announce.getGoogleChat().isEnabled() ||
                announce.getLinkedin().isEnabled() ||
                announce.getConfiguredHttp().isEnabled() ||
                announce.getSmtp().isEnabled() ||
                announce.getMastodon().isEnabled() ||
                announce.getMattermost().isEnabled() ||
                announce.getOpenCollective().isEnabled() ||
                announce.getReddit().isEnabled() ||
                announce.getSdkman().isEnabled() ||
                announce.getSlack().isEnabled() ||
                announce.getTeams().isEnabled() ||
                announce.getTelegram().isEnabled() ||
                announce.getTwist().isEnabled() ||
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
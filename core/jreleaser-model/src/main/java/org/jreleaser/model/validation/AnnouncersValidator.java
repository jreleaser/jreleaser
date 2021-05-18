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

import org.jreleaser.model.Announce;
import org.jreleaser.model.JReleaserContext;
import org.jreleaser.util.Errors;

import static org.jreleaser.model.validation.DiscordValidator.validateDiscord;
import static org.jreleaser.model.validation.DiscussionsValidator.validateDiscussions;
import static org.jreleaser.model.validation.GitterValidator.validateGitter;
import static org.jreleaser.model.validation.MailValidator.validateMail;
import static org.jreleaser.model.validation.MastodonValidator.validateMastodon;
import static org.jreleaser.model.validation.SdkmanValidator.validateSdkman;
import static org.jreleaser.model.validation.SlackValidator.validateSlack;
import static org.jreleaser.model.validation.TeamsValidator.validateTeams;
import static org.jreleaser.model.validation.TwitterValidator.validateTwitter;
import static org.jreleaser.model.validation.ZulipValidator.validateZulip;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public abstract class AnnouncersValidator extends Validator {
    public static void validateAnnouncers(JReleaserContext context, JReleaserContext.Mode mode, Errors errors) {
        if (mode != JReleaserContext.Mode.FULL) {
            return;
        }

        context.getLogger().debug("announce");

        Announce announce = context.getModel().getAnnounce();
        validateDiscussions(context, announce.getDiscussions(), errors);
        validateDiscord(context, announce.getDiscord(), errors);
        validateGitter(context, announce.getGitter(), errors);
        validateMail(context, announce.getMail(), errors);
        validateMastodon(context, announce.getMastodon(), errors);
        validateSdkman(context, announce.getSdkman(), errors);
        validateSlack(context, announce.getSlack(), errors);
        validateTeams(context, announce.getTeams(), errors);
        validateTwitter(context, announce.getTwitter(), errors);
        validateZulip(context, announce.getZulip(), errors);

        if (!announce.isEnabledSet()) {
            announce.setEnabled(announce.getDiscord().isEnabled() ||
                announce.getDiscussions().isEnabled() ||
                announce.getGitter().isEnabled() ||
                announce.getMail().isEnabled() ||
                announce.getMastodon().isEnabled() ||
                announce.getSdkman().isEnabled() ||
                announce.getSlack().isEnabled() ||
                announce.getTeams().isEnabled() ||
                announce.getTwitter().isEnabled() ||
                announce.getZulip().isEnabled());
        }
    }
}
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
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.internal.announce.RedditAnnouncer;
import org.jreleaser.util.Errors;

import java.nio.file.Files;

import static org.jreleaser.model.api.announce.RedditAnnouncer.REDDIT_CLIENT_ID;
import static org.jreleaser.model.api.announce.RedditAnnouncer.REDDIT_CLIENT_SECRET;
import static org.jreleaser.model.api.announce.RedditAnnouncer.REDDIT_PASSWORD;
import static org.jreleaser.model.api.announce.RedditAnnouncer.REDDIT_USERNAME;
import static org.jreleaser.model.internal.validation.common.Validator.checkProperty;
import static org.jreleaser.model.internal.validation.common.Validator.resolveActivatable;
import static org.jreleaser.model.internal.validation.common.Validator.validateTimeout;
import static org.jreleaser.util.CollectionUtils.listOf;
import static org.jreleaser.util.StringUtils.isBlank;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Usman Shaikh
 * @since 1.21.0
 */
public final class RedditAnnouncerValidator {
    private RedditAnnouncerValidator() {
        // noop
    }

    public static void validateReddit(JReleaserContext context, RedditAnnouncer announcer, Errors errors) {
        context.getLogger().debug("announce.reddit");
        resolveActivatable(context, announcer, "announce.reddit", "NEVER");
        if (!announcer.resolveEnabledWithSnapshot(context.getModel().getProject())) {
            context.getLogger().debug(RB.$("validation.disabled"));
            return;
        }

        announcer.setClientId(
            checkProperty(context,
                listOf(
                    "announce.reddit.client.id",
                    REDDIT_CLIENT_ID),
                "announce.reddit.clientId",
                announcer.getClientId(),
                errors,
                context.isDryrun()));

        announcer.setClientSecret(
            checkProperty(context,
                listOf(
                    "announce.reddit.client.secret",
                    REDDIT_CLIENT_SECRET),
                "announce.reddit.clientSecret",
                announcer.getClientSecret(),
                errors,
                context.isDryrun()));

        announcer.setUsername(
            checkProperty(context,
                listOf(
                    "announce.reddit.username",
                    REDDIT_USERNAME),
                "announce.reddit.username",
                announcer.getUsername(),
                errors,
                context.isDryrun()));

        announcer.setPassword(
            checkProperty(context,
                listOf(
                    "announce.reddit.password",
                    REDDIT_PASSWORD),
                "announce.reddit.password",
                announcer.getPassword(),
                errors,
                context.isDryrun()));

        announcer.setSubreddit(
            checkProperty(context,
                listOf(
                    "announce.reddit.subreddit"),
                "announce.reddit.subreddit",
                announcer.getSubreddit(),
                errors));


        if (isNotBlank(announcer.getTextTemplate()) &&
            !Files.exists(context.getBasedir().resolve(announcer.getTextTemplate().trim()))) {
            errors.configuration(RB.$("validation_directory_not_exist", "reddit.textTemplate", announcer.getTextTemplate()));
        }

        if (isBlank(announcer.getTitle())) {
            announcer.setTitle(RB.$("default.discussion.title"));
        }

        if (announcer.getSubmissionType() == org.jreleaser.model.api.announce.RedditAnnouncer.SubmissionType.SELF) {
            if (isBlank(announcer.getText()) && isBlank(announcer.getTextTemplate())) {
                announcer.setText(RB.$("default.release.message"));
            }
        } else {
            if (isBlank(announcer.getUrl())) {
                announcer.setUrl("{{releaseNotesUrl}}");
            }
        }

        validateTimeout(announcer);
    }
}
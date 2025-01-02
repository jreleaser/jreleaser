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
import org.jreleaser.model.internal.announce.TwitterAnnouncer;
import org.jreleaser.util.Errors;

import java.nio.file.Files;

import static org.jreleaser.model.api.announce.TwitterAnnouncer.TWITTER_ACCESS_TOKEN;
import static org.jreleaser.model.api.announce.TwitterAnnouncer.TWITTER_ACCESS_TOKEN_SECRET;
import static org.jreleaser.model.api.announce.TwitterAnnouncer.TWITTER_CONSUMER_KEY;
import static org.jreleaser.model.api.announce.TwitterAnnouncer.TWITTER_CONSUMER_SECRET;
import static org.jreleaser.model.internal.validation.common.Validator.checkProperty;
import static org.jreleaser.model.internal.validation.common.Validator.resolveActivatable;
import static org.jreleaser.model.internal.validation.common.Validator.validateTimeout;
import static org.jreleaser.util.CollectionUtils.listOf;
import static org.jreleaser.util.StringUtils.isBlank;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public final class TwitterAnnouncerValidator {
    private TwitterAnnouncerValidator() {
        // noop
    }

    public static void validateTwitter(JReleaserContext context, TwitterAnnouncer announcer, Errors errors) {
        context.getLogger().debug("announce.twitter");
        resolveActivatable(context, announcer, "announce.twitter", "NEVER");
        if (!announcer.resolveEnabledWithSnapshot(context.getModel().getProject())) {
            context.getLogger().debug(RB.$("validation.disabled"));
            return;
        }

        announcer.setConsumerKey(
            checkProperty(context,
                listOf(
                    "announce.twitter.consumer.key",
                    TWITTER_CONSUMER_KEY),
                "announce.twitter.consumerKey",
                announcer.getConsumerKey(),
                errors,
                context.isDryrun()));

        announcer.setConsumerSecret(
            checkProperty(context,
                listOf(
                    "announce.twitter.consumer.secret",
                    TWITTER_CONSUMER_SECRET),
                "announce.twitter.consumerSecret",
                announcer.getConsumerSecret(),
                errors,
                context.isDryrun()));

        announcer.setAccessToken(
            checkProperty(context,
                listOf(
                    "announce.twitter.access.token",
                    TWITTER_ACCESS_TOKEN),
                "announce.twitter.accessToken",
                announcer.getAccessToken(),
                errors,
                context.isDryrun()));

        announcer.setAccessTokenSecret(
            checkProperty(context,
                listOf(
                    "announce.twitter.access.token.secret",
                    TWITTER_ACCESS_TOKEN_SECRET),
                "announce.twitter.accessTokenSecret",
                announcer.getAccessTokenSecret(),
                errors,
                context.isDryrun()));

        if (isNotBlank(announcer.getStatusTemplate()) &&
            !Files.exists(context.getBasedir().resolve(announcer.getStatusTemplate().trim()))) {
            errors.configuration(RB.$("validation_directory_not_exist", "twitter.statusTemplate", announcer.getStatusTemplate()));
        }

        if (isBlank(announcer.getStatus()) && isBlank(announcer.getStatusTemplate()) && announcer.getStatuses().isEmpty()) {
            announcer.setStatus(RB.$("default.release.message"));
        }

        validateTimeout(announcer);
    }
}
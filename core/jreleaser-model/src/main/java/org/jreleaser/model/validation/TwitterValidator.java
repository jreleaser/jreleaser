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
import org.jreleaser.model.JReleaserContext;
import org.jreleaser.model.Twitter;
import org.jreleaser.util.Errors;

import java.nio.file.Files;

import static org.jreleaser.model.Twitter.TWITTER_ACCESS_TOKEN;
import static org.jreleaser.model.Twitter.TWITTER_ACCESS_TOKEN_SECRET;
import static org.jreleaser.model.Twitter.TWITTER_CONSUMER_KEY;
import static org.jreleaser.model.Twitter.TWITTER_CONSUMER_SECRET;
import static org.jreleaser.util.StringUtils.isBlank;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public abstract class TwitterValidator extends Validator {
    public static void validateTwitter(JReleaserContext context, Twitter twitter, Errors errors) {
        if (!twitter.resolveEnabled(context.getModel().getProject())) return;
        context.getLogger().debug("announce.twitter");

        twitter.setConsumerKey(
            checkProperty(context,
                TWITTER_CONSUMER_KEY,
                "twitter.consumerKey",
                twitter.getConsumerKey(),
                errors,
                context.isDryrun()));

        twitter.setConsumerSecret(
            checkProperty(context,
                TWITTER_CONSUMER_SECRET,
                "twitter.consumerSecret",
                twitter.getConsumerSecret(),
                errors,
                context.isDryrun()));

        twitter.setAccessToken(
            checkProperty(context,
                TWITTER_ACCESS_TOKEN,
                "twitter.accessToken",
                twitter.getAccessToken(),
                errors,
                context.isDryrun()));

        twitter.setAccessTokenSecret(
            checkProperty(context,
                TWITTER_ACCESS_TOKEN_SECRET,
                "twitter.accessTokenSecret",
                twitter.getAccessTokenSecret(),
                errors,
                context.isDryrun()));

        if (isNotBlank(twitter.getStatusTemplate()) &&
            !Files.exists(context.getBasedir().resolve(twitter.getStatusTemplate().trim()))) {
            errors.configuration(RB.$("validation_directory_not_exist", "twitter.statusTemplate", twitter.getStatusTemplate()));
        }

        if (isBlank(twitter.getStatus()) && isBlank(twitter.getStatusTemplate()) && twitter.getStatuses().isEmpty()) {
            twitter.setStatus(RB.$("default.release.message"));
        }

        validateTimeout(twitter);
    }
}
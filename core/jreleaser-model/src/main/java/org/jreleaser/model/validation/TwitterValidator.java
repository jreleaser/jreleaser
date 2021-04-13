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

import org.jreleaser.model.JReleaserContext;
import org.jreleaser.model.Twitter;

import java.util.List;

import static org.jreleaser.model.Twitter.TWITTER_ACCESS_TOKEN;
import static org.jreleaser.model.Twitter.TWITTER_ACCESS_TOKEN_SECRET;
import static org.jreleaser.model.Twitter.TWITTER_CONSUMER_KEY;
import static org.jreleaser.model.Twitter.TWITTER_CONSUMER_SECRET;
import static org.jreleaser.util.StringUtils.isBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public abstract class TwitterValidator extends Validator {
    public static void validateTwitter(JReleaserContext context, Twitter twitter, List<String> errors) {
        if (!twitter.resolveEnabled(context.getModel().getProject())) return;
        context.getLogger().debug("announce.twitter");

        twitter.setConsumerKey(
            checkProperty(context.getModel().getEnvironment(),
                TWITTER_CONSUMER_KEY,
                "twitter.consumerKey",
                twitter.getConsumerKey(),
                errors));

        twitter.setConsumerSecret(
            checkProperty(context.getModel().getEnvironment(),
                TWITTER_CONSUMER_SECRET,
                "twitter.consumerSecret",
                twitter.getConsumerSecret(),
                errors));

        twitter.setAccessToken(
            checkProperty(context.getModel().getEnvironment(),
                TWITTER_ACCESS_TOKEN,
                "twitter.accessToken",
                twitter.getAccessToken(),
                errors));

        twitter.setAccessTokenSecret(
            checkProperty(context.getModel().getEnvironment(),
                TWITTER_ACCESS_TOKEN_SECRET,
                "twitter.accessTokenSecret",
                twitter.getAccessTokenSecret(),
                errors));

        if (isBlank(twitter.getStatus())) {
            twitter.setStatus("\uD83D\uDE80 {{projectNameCapitalized}} {{projectVersion}} has been released! {{releaseNotesUrl}}");
        }
    }
}
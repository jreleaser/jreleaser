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
package org.jreleaser.sdk.twitter;

import org.jreleaser.util.JReleaserLogger;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

import static java.util.Objects.requireNonNull;
import static org.jreleaser.util.StringUtils.requireNonBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class Twitter {
    private final JReleaserLogger logger;
    private final twitter4j.Twitter twitter;
    private final boolean dryrun;

    public Twitter(JReleaserLogger logger, String apiHost, String consumerKey, String consumerToken,
                   String accessToken, String accessTokenSecret, boolean dryrun) {
        requireNonNull(logger, "'logger' must not be blank");
        requireNonBlank(apiHost, "'apiHost' must not be blank");
        requireNonBlank(consumerKey, "'consumerKey' must not be blank");
        requireNonBlank(consumerToken, "'consumerToken' must not be blank");
        requireNonBlank(accessToken, "'accessToken' must not be blank");
        requireNonBlank(accessTokenSecret, "'accessTokenSecret' must not be blank");

        this.logger = logger;
        this.dryrun = dryrun;
        this.twitter = new TwitterFactory(
            new ConfigurationBuilder()
                .setRestBaseURL(apiHost)
                .setOAuthConsumerKey(consumerKey)
                .setOAuthConsumerSecret(consumerToken)
                .setOAuthAccessToken(accessToken)
                .setOAuthAccessTokenSecret(accessTokenSecret)
                .build())
            .getInstance();

        this.logger.debug("Twitter dryrun set to {}", dryrun);
    }

    public void updateStatus(String status) throws TwitterException {
        wrap(() -> twitter.updateStatus(status));
    }

    private void wrap(TwitterOperation op) throws TwitterException {
        try {
            if (!dryrun) op.execute();
        } catch (twitter4j.TwitterException e) {
            logger.trace(e);
            throw new TwitterException("Twitter operation failed", e);
        }
    }

    private interface TwitterOperation {
        void execute() throws twitter4j.TwitterException;
    }
}

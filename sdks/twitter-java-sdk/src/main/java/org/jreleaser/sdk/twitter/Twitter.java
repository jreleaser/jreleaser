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
package org.jreleaser.sdk.twitter;

import org.jreleaser.bundle.RB;
import org.jreleaser.util.JReleaserLogger;
import twitter4j.Status;
import twitter4j.StatusUpdate;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

import java.util.List;

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

    public Twitter(JReleaserLogger logger,
                   String apiHost,
                   int connectTimeout,
                   int readTimeout,
                   String consumerKey,
                   String consumerToken,
                   String accessToken,
                   String accessTokenSecret,
                   boolean dryrun) {
        requireNonNull(logger, "'logger' must not be null");
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
                .setHttpConnectionTimeout(connectTimeout * 1000)
                .setHttpReadTimeout(readTimeout * 1000)
                .setOAuthConsumerKey(consumerKey)
                .setOAuthConsumerSecret(consumerToken)
                .setOAuthAccessToken(accessToken)
                .setOAuthAccessTokenSecret(accessTokenSecret)
                .build())
            .getInstance();

        this.logger.debug(RB.$("workflow.dryrun"), dryrun);
    }

    public void updateStatus(List<String> statuses) throws TwitterException {
        wrap(() -> {
            String message = statuses.get(0);
            Status status = twitter.updateStatus(message);
            for (int i = 1; i < statuses.size(); i++) {
                status = twitter.updateStatus(new StatusUpdate(statuses.get(i))
                    .inReplyToStatusId(status.getId()));
            }
        });
    }

    private void wrap(TwitterOperation op) throws TwitterException {
        try {
            if (!dryrun) op.execute();
        } catch (twitter4j.TwitterException e) {
            logger.trace(e);
            throw new TwitterException(RB.$("sdk.operation.failed", "Twitter"), e);
        }
    }

    private interface TwitterOperation {
        void execute() throws twitter4j.TwitterException;
    }
}

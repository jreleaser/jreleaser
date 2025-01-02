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
package org.jreleaser.sdk.twitter;

import org.jreleaser.bundle.RB;
import org.jreleaser.logging.JReleaserLogger;
import twitter4j.v1.Status;
import twitter4j.v1.StatusUpdate;
import twitter4j.v1.TwitterV1;

import java.util.List;

import static java.util.Objects.requireNonNull;
import static org.jreleaser.util.StringUtils.requireNonBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class Twitter {
    private final JReleaserLogger logger;
    private final boolean dryrun;
    private final TwitterV1 twitter;

    public Twitter(JReleaserLogger logger,
                   int connectTimeout,
                   int readTimeout,
                   String consumerKey,
                   String consumerToken,
                   String accessToken,
                   String accessTokenSecret,
                   boolean dryrun) {
        requireNonNull(logger, "'logger' must not be null");
        requireNonBlank(consumerKey, "'consumerKey' must not be blank");
        requireNonBlank(consumerToken, "'consumerToken' must not be blank");
        requireNonBlank(accessToken, "'accessToken' must not be blank");
        requireNonBlank(accessTokenSecret, "'accessTokenSecret' must not be blank");

        this.logger = logger;
        this.dryrun = dryrun;
        this.twitter = twitter4j.Twitter.newBuilder()
            .oAuthConsumer(consumerKey, consumerToken)
            .oAuthAccessToken(accessToken, accessTokenSecret)
            .httpConnectionTimeout(connectTimeout * 1000)
            .httpReadTimeout(readTimeout * 1000)
            .build().v1();

        this.logger.debug(RB.$("workflow.dryrun"), dryrun);
    }

    public void updateStatus(List<String> statuses) throws TwitterException {
        wrap(() -> {
            Status status = twitter.tweets().updateStatus(statuses.get(0));
            for (int i = 1; i < statuses.size(); i++) {
                status = twitter.tweets().updateStatus(StatusUpdate.of(statuses.get(i))
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

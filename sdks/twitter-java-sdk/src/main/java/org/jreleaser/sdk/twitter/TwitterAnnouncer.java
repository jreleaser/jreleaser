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

import org.jreleaser.model.JReleaserModel;
import org.jreleaser.model.Twitter;
import org.jreleaser.model.announcer.spi.AbstractAnnouncerBuilder;
import org.jreleaser.model.announcer.spi.AnnounceException;
import org.jreleaser.model.announcer.spi.Announcer;
import org.jreleaser.util.Logger;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class TwitterAnnouncer implements Announcer {
    private final Logger logger;
    private final JReleaserModel model;

    private TwitterAnnouncer(Logger logger, JReleaserModel model) {
        this.logger = logger;
        this.model = model;
    }

    @Override
    public void announce(boolean dryRun) throws AnnounceException {
        Twitter twitter = model.getAnnouncers().getTwitter();

        String status = twitter.getResolvedStatus(model);
        logger.info("Announcing on Twitter: {}", status);

        try {
            UpdateStatusTwitterCommand.builder(logger)
                .consumerKey(twitter.getResolvedConsumerKey())
                .consumerToken(twitter.getResolvedConsumerSecret())
                .accessToken(twitter.getResolvedAccessToken())
                .accessTokenSecret(twitter.getResolvedAccessTokenSecret())
                .status(status)
                .dryRun(dryRun)
                .build()
                .execute();
        } catch (TwitterException e) {
            throw new AnnounceException(e);
        }
    }

    public static Builder builder(Logger logger) {
        Builder builder = new Builder();
        builder.logger(logger);
        return builder;
    }

    public static class Builder extends AbstractAnnouncerBuilder<TwitterAnnouncer, Builder> {
        @Override
        public TwitterAnnouncer build() {
            validate();

            return new TwitterAnnouncer(logger, model);
        }
    }
}

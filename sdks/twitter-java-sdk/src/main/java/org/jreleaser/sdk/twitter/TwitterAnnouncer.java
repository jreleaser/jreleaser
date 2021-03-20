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

import org.jreleaser.model.JReleaserContext;
import org.jreleaser.model.Twitter;
import org.jreleaser.model.announcer.spi.AbstractAnnouncerBuilder;
import org.jreleaser.model.announcer.spi.AnnounceException;
import org.jreleaser.model.announcer.spi.Announcer;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class TwitterAnnouncer implements Announcer {
    private final JReleaserContext context;

    private TwitterAnnouncer(JReleaserContext context) {
        this.context = context;
    }

    @Override
    public void announce() throws AnnounceException {
        Twitter twitter = context.getModel().getAnnounce().getTwitter();
        if (!twitter.isEnabled()) {
            context.getLogger().debug("Twitter announcer is disabled");
            return;
        }

        String status = twitter.getResolvedStatus(context.getModel());
        context.getLogger().info("Announcing on Twitter: {}", status);

        try {
            UpdateStatusTwitterCommand.builder(context.getLogger())
                .consumerKey(twitter.getResolvedConsumerKey())
                .consumerToken(twitter.getResolvedConsumerSecret())
                .accessToken(twitter.getResolvedAccessToken())
                .accessTokenSecret(twitter.getResolvedAccessTokenSecret())
                .status(status)
                .dryrun(context.isDryrun())
                .build()
                .execute();
        } catch (TwitterException e) {
            throw new AnnounceException(e);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends AbstractAnnouncerBuilder<TwitterAnnouncer, Builder> {
        @Override
        public TwitterAnnouncer build() {
            validate();

            return new TwitterAnnouncer(context);
        }
    }
}

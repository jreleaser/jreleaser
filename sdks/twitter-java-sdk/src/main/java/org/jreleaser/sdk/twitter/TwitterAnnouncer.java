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
import org.jreleaser.model.JReleaserContext;
import org.jreleaser.model.Twitter;
import org.jreleaser.model.announcer.spi.AnnounceException;
import org.jreleaser.model.announcer.spi.Announcer;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
@org.jreleaser.infra.nativeimage.annotations.NativeImage
public class TwitterAnnouncer implements Announcer {
    private final JReleaserContext context;

    TwitterAnnouncer(JReleaserContext context) {
        this.context = context;
    }

    @Override
    public String getName() {
        return org.jreleaser.model.Twitter.NAME;
    }

    @Override
    public boolean isEnabled() {
        return context.getModel().getAnnounce().getTwitter().isEnabled();
    }

    @Override
    public void announce() throws AnnounceException {
        Twitter twitter = context.getModel().getAnnounce().getTwitter();

        String status = twitter.getResolvedStatus(context);
        context.getLogger().info(RB.$("twitter.tweet"), status);
        context.getLogger().debug(RB.$("twitter.tweet.size"), status.length());

        try {
            UpdateStatusTwitterCommand.builder(context.getLogger())
                .connectTimeout(twitter.getConnectTimeout())
                .readTimeout(twitter.getReadTimeout())
                .consumerKey(context.isDryrun() ? "**UNDEFINED**" : twitter.getResolvedConsumerKey())
                .consumerToken(context.isDryrun() ? "**UNDEFINED**" : twitter.getResolvedConsumerSecret())
                .accessToken(context.isDryrun() ? "**UNDEFINED**" : twitter.getResolvedAccessToken())
                .accessTokenSecret(context.isDryrun() ? "**UNDEFINED**" : twitter.getResolvedAccessTokenSecret())
                .status(status)
                .dryrun(context.isDryrun())
                .build()
                .execute();
        } catch (TwitterException e) {
            throw new AnnounceException(e);
        }
    }
}

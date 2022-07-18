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
import org.jreleaser.util.Constants;
import org.jreleaser.util.MustacheUtils;
import org.jreleaser.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.jreleaser.util.Constants.KEY_TAG_NAME;
import static org.jreleaser.util.MustacheUtils.applyTemplates;
import static org.jreleaser.util.StringUtils.isNotBlank;
import static org.jreleaser.util.Templates.resolveTemplate;

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

        List<String> statuses = new ArrayList<>();

        if (isNotBlank(twitter.getStatusTemplate())) {
            Map<String, Object> props = new LinkedHashMap<>();
            props.put(Constants.KEY_CHANGELOG, MustacheUtils.passThrough(context.getChangelog()));
            context.getModel().getRelease().getGitService().fillProps(props, context.getModel());
            Arrays.stream(twitter.getResolvedStatusTemplate(context, props)
                    .split(System.lineSeparator()))
                .filter(StringUtils::isNotBlank)
                .map(String::trim)
                .forEach(statuses::add);
        }
        if (statuses.isEmpty() && !twitter.getStatuses().isEmpty()) {
            statuses.addAll(twitter.getStatuses());
            twitter.getStatuses().stream()
                .filter(StringUtils::isNotBlank)
                .map(String::trim)
                .forEach(statuses::add);
        }
        if (statuses.isEmpty()) {
            statuses.add(twitter.getStatus());
        }

        for (int i = 0; i < statuses.size(); i++) {
            String status = getResolvedMessage(context, statuses.get(i));
            context.getLogger().info(RB.$("twitter.tweet"), status);
            context.getLogger().debug(RB.$("twitter.tweet.size"), status.length());
            statuses.set(i, status);
        }

        try {
            UpdateStatusTwitterCommand.builder(context.getLogger())
                .connectTimeout(twitter.getConnectTimeout())
                .readTimeout(twitter.getReadTimeout())
                .consumerKey(context.isDryrun() ? "**UNDEFINED**" : twitter.getResolvedConsumerKey())
                .consumerToken(context.isDryrun() ? "**UNDEFINED**" : twitter.getResolvedConsumerSecret())
                .accessToken(context.isDryrun() ? "**UNDEFINED**" : twitter.getResolvedAccessToken())
                .accessTokenSecret(context.isDryrun() ? "**UNDEFINED**" : twitter.getResolvedAccessTokenSecret())
                .statuses(statuses)
                .dryrun(context.isDryrun())
                .build()
                .execute();
        } catch (TwitterException e) {
            throw new AnnounceException(e);
        }
    }

    private String getResolvedMessage(JReleaserContext context, String message) {
        Map<String, Object> props = context.fullProps();
        applyTemplates(props, context.getModel().getAnnounce().getTwitter().getResolvedExtraProperties());
        props.put(KEY_TAG_NAME, context.getModel().getRelease().getGitService().getEffectiveTagName(context.getModel()));
        return resolveTemplate(message, props);
    }
}

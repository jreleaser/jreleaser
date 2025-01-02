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
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.spi.announce.AnnounceException;
import org.jreleaser.model.spi.announce.Announcer;
import org.jreleaser.mustache.TemplateContext;
import org.jreleaser.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.jreleaser.model.Constants.KEY_PREVIOUS_TAG_NAME;
import static org.jreleaser.model.Constants.KEY_TAG_NAME;
import static org.jreleaser.mustache.MustacheUtils.applyTemplates;
import static org.jreleaser.mustache.Templates.resolveTemplate;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
@org.jreleaser.infra.nativeimage.annotations.NativeImage
public class TwitterAnnouncer implements Announcer<org.jreleaser.model.api.announce.TwitterAnnouncer> {
    private final JReleaserContext context;
    private final org.jreleaser.model.internal.announce.TwitterAnnouncer twitter;

    public TwitterAnnouncer(JReleaserContext context) {
        this.context = context;
        this.twitter = context.getModel().getAnnounce().getTwitter();
    }

    @Override
    public org.jreleaser.model.api.announce.TwitterAnnouncer getAnnouncer() {
        return twitter.asImmutable();
    }

    @Override
    public String getName() {
        return org.jreleaser.model.api.announce.TwitterAnnouncer.TYPE;
    }

    @Override
    public boolean isEnabled() {
        return twitter.isEnabled();
    }

    @Override
    public void announce() throws AnnounceException {
        List<String> statuses = new ArrayList<>();

        if (isNotBlank(twitter.getStatusTemplate())) {
            TemplateContext props = new TemplateContext();
            context.getModel().getRelease().getReleaser().fillProps(props, context.getModel());
            Arrays.stream(twitter.getResolvedStatusTemplate(context, props)
                    .split(System.lineSeparator()))
                .filter(StringUtils::isNotBlank)
                .map(String::trim)
                .forEach(statuses::add);
        }
        if (statuses.isEmpty() && !twitter.getStatuses().isEmpty()) {
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
                .consumerKey(context.isDryrun() ? "**UNDEFINED**" : twitter.getConsumerKey())
                .consumerToken(context.isDryrun() ? "**UNDEFINED**" : twitter.getConsumerSecret())
                .accessToken(context.isDryrun() ? "**UNDEFINED**" : twitter.getAccessToken())
                .accessTokenSecret(context.isDryrun() ? "**UNDEFINED**" : twitter.getAccessTokenSecret())
                .statuses(statuses)
                .dryrun(context.isDryrun())
                .build()
                .execute();
        } catch (TwitterException e) {
            throw new AnnounceException(e);
        }
    }

    private String getResolvedMessage(JReleaserContext context, String message) {
        TemplateContext props = context.fullProps();
        applyTemplates(props, context.getModel().getAnnounce().getTwitter().resolvedExtraProperties());
        props.set(KEY_TAG_NAME, context.getModel().getRelease().getReleaser().getEffectiveTagName(context.getModel()));
        props.set(KEY_PREVIOUS_TAG_NAME, context.getModel().getRelease().getReleaser().getResolvedPreviousTagName(context.getModel()));
        return resolveTemplate(message, props);
    }
}

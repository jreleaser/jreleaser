/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2026 The JReleaser authors.
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
package org.jreleaser.sdk.reddit;

import org.jreleaser.bundle.RB;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.spi.announce.AnnounceException;
import org.jreleaser.model.spi.announce.Announcer;
import org.jreleaser.mustache.MustacheUtils;
import org.jreleaser.mustache.TemplateContext;

import static org.jreleaser.model.Constants.KEY_REDDIT_SUBREDDIT;
import static org.jreleaser.model.Constants.KEY_REDDIT_TITLE;
import static org.jreleaser.mustache.MustacheUtils.applyTemplates;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Usman Shaikh
 * @since 1.21.0
 */
@org.jreleaser.infra.nativeimage.annotations.NativeImage
public class RedditAnnouncer implements Announcer<org.jreleaser.model.api.announce.RedditAnnouncer> {
    private final JReleaserContext context;
    private final org.jreleaser.model.internal.announce.RedditAnnouncer reddit;

    public RedditAnnouncer(JReleaserContext context) {
        this.context = context;
        this.reddit = context.getModel().getAnnounce().getReddit();
    }

    @Override
    public org.jreleaser.model.api.announce.RedditAnnouncer getAnnouncer() {
        return reddit.asImmutable();
    }

    @Override
    public String getName() {
        return org.jreleaser.model.api.announce.RedditAnnouncer.TYPE;
    }

    @Override
    public boolean isEnabled() {
        return reddit.isEnabled();
    }

    @Override
    public void announce() throws AnnounceException {
        String subreddit = reddit.getSubreddit();
        
        String title = reddit.getResolvedTitle(context);

        String text = "";
        String url = "";
        
        if (reddit.getSubmissionType() == org.jreleaser.model.api.announce.RedditAnnouncer.SubmissionType.SELF) {
            if (isNotBlank(reddit.getText())) {
                text = reddit.getResolvedText(context);
            } else {
                TemplateContext props = new TemplateContext();
                context.getModel().getRelease().getReleaser().fillProps(props, context);
                text = reddit.getResolvedTextTemplate(context, props);
            }
        } else {
            url = reddit.getResolvedUrl(context);
        }

        context.getLogger().info(RB.$("reddit.submission.creating"), 
            reddit.getSubmissionType().formatted(), reddit.getSubreddit());
        context.getLogger().debug(RB.$("reddit.submission.title"), title);
        
        if (reddit.getSubmissionType() == org.jreleaser.model.api.announce.RedditAnnouncer.SubmissionType.SELF) {
            context.getLogger().debug(RB.$("reddit.submission.text"), text);
        } else {
            context.getLogger().debug(RB.$("reddit.submission.url"), url);
        }

        try {
            RedditSdk sdk = RedditSdk.builder(context.asImmutable())
                .clientId(reddit.getClientId())
                .clientSecret(reddit.getClientSecret())
                .username(reddit.getUsername())
                .password(reddit.getPassword())
                .connectTimeout(reddit.getConnectTimeout())
                .readTimeout(reddit.getReadTimeout())
                .dryrun(context.isDryrun())
                .build();

            TemplateContext props = context.fullProps();
            props.set(KEY_REDDIT_TITLE, title);
            props.set(KEY_REDDIT_SUBREDDIT, MustacheUtils.passThrough("{{" + KEY_REDDIT_SUBREDDIT + "}}"));
            applyTemplates(context.getLogger(), props, reddit.resolvedExtraProperties());
            
            title = MustacheUtils.applyTemplate(context.getLogger(), title, props);
            
            if (reddit.getSubmissionType() == org.jreleaser.model.api.announce.RedditAnnouncer.SubmissionType.SELF) {
                text = MustacheUtils.applyTemplate(context.getLogger(), text, props);
                sdk.submitTextPost(subreddit, title, text);
            } else {
                url = MustacheUtils.applyTemplate(context.getLogger(), url, props);
                sdk.submitLinkPost(subreddit, title, url);
            }
        } catch (RedditSdkException e) {
            context.getLogger().trace(e);
            throw new AnnounceException(e);
        }
    }
}
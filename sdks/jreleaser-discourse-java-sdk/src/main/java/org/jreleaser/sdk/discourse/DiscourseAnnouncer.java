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
package org.jreleaser.sdk.discourse;

import org.jreleaser.model.Constants;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.spi.announce.AnnounceException;
import org.jreleaser.model.spi.announce.Announcer;
import org.jreleaser.mustache.MustacheUtils;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author shblue21
 * @since 1.3.0
 */
@org.jreleaser.infra.nativeimage.annotations.NativeImage
public class DiscourseAnnouncer implements Announcer<org.jreleaser.model.api.announce.DiscourseAnnouncer> {
    private final JReleaserContext context;
    private final org.jreleaser.model.internal.announce.DiscourseAnnouncer discourse;

    public DiscourseAnnouncer(JReleaserContext context) {
        this.context = context;
        this.discourse = context.getModel().getAnnounce().getDiscourse();
    }

    @Override
    public org.jreleaser.model.api.announce.DiscourseAnnouncer getAnnouncer() {
        return discourse.asImmutable();
    }

    @Override
    public String getName() {
        return org.jreleaser.model.api.announce.DiscourseAnnouncer.TYPE;
    }

    @Override
    public boolean isEnabled() {
        return discourse.isEnabled();
    }

    @Override
    public void announce() throws AnnounceException {
        String message = "";
        if (isNotBlank(discourse.getMessage())) {
            message = discourse.getResolvedMessage(context);
        } else {
            Map<String, Object> props = new LinkedHashMap<>();
            props.put(Constants.KEY_CHANGELOG, MustacheUtils.passThrough(context.getChangelog()));
            context.getModel().getRelease().getReleaser().fillProps(props, context.getModel());
            message = discourse.getResolvedMessageTemplate(context, props);
        }

        String title = discourse.getResolvedTitle(context);
        String categoryName = discourse.getCategoryName();

        context.getLogger().info("title: {}", title);
        context.getLogger().debug("message: {}", message);
        context.getLogger().debug("categoryName: {}", categoryName);

        try {
            DiscourseSdk sdk = DiscourseSdk.builder(context.getLogger())
                .host(discourse.getHost())
                .apiKey(discourse.getApiKey())
                .userName(discourse.getUserName())
                .connectTimeout(discourse.getConnectTimeout())
                .readTimeout(discourse.getReadTimeout())
                .dryrun(context.isDryrun())
                .build();
            sdk.createPost(title, message, categoryName);
        } catch (DiscourseException e) {
            throw new AnnounceException(e);
        }
    }
}

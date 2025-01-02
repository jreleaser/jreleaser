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
package org.jreleaser.sdk.github;

import org.jreleaser.bundle.RB;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.internal.release.GithubReleaser;
import org.jreleaser.model.spi.announce.AnnounceException;
import org.jreleaser.model.spi.announce.Announcer;
import org.jreleaser.mustache.TemplateContext;

import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
@org.jreleaser.infra.nativeimage.annotations.NativeImage
public class DiscussionsAnnouncer implements Announcer<org.jreleaser.model.api.announce.DiscussionsAnnouncer> {
    private final JReleaserContext context;
    private final org.jreleaser.model.internal.announce.DiscussionsAnnouncer discussions;

    public DiscussionsAnnouncer(JReleaserContext context) {
        this.context = context;
        this.discussions = context.getModel().getAnnounce().getDiscussions();
    }

    @Override
    public org.jreleaser.model.api.announce.DiscussionsAnnouncer getAnnouncer() {
        return discussions.asImmutable();
    }

    @Override
    public String getName() {
        return org.jreleaser.model.api.announce.DiscussionsAnnouncer.TYPE;
    }

    @Override
    public boolean isEnabled() {
        return discussions.isEnabled();
    }

    @Override
    public void announce() throws AnnounceException {
        GithubReleaser github = context.getModel().getRelease().getGithub();

        String message = "";
        if (isNotBlank(discussions.getMessage())) {
            message = discussions.getResolvedMessage(context);
        } else {
            TemplateContext props = new TemplateContext();
            context.getModel().getRelease().getReleaser().fillProps(props, context.getModel());
            message = discussions.getResolvedMessageTemplate(context, props);
        }

        String title = discussions.getResolvedTitle(context);
        context.getLogger().info("title: {}", title);
        context.getLogger().debug("message: {}", message);

        if (context.isDryrun()) return;

        try {
            Github api = new Github(context.asImmutable(),
                github.getApiEndpoint(),
                github.getToken(),
                discussions.getConnectTimeout(),
                discussions.getReadTimeout());

            if (api.findDiscussion(discussions.getOrganization(), discussions.getTeam(), title).isPresent()) {
                throw new IllegalStateException(RB.$("ERROR_git_discussion_duplicate",
                    title, discussions.getOrganization(), discussions.getTeam()));
            }

            api.createDiscussion(discussions.getOrganization(), discussions.getTeam(), title, message);
        } catch (IllegalStateException e) {
            context.getLogger().trace(e);
            throw new AnnounceException(e);
        }
    }
}

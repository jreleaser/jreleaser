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
package org.jreleaser.sdk.github;

import org.jreleaser.model.Discussions;
import org.jreleaser.model.JReleaserContext;
import org.jreleaser.model.announcer.spi.AnnounceException;
import org.jreleaser.model.announcer.spi.Announcer;
import org.jreleaser.util.Constants;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class DiscussionsAnnouncer implements Announcer {
    private final JReleaserContext context;

    DiscussionsAnnouncer(JReleaserContext context) {
        this.context = context;
    }

    @Override
    public String getName() {
        return Discussions.NAME;
    }

    @Override
    public boolean isEnabled() {
        return context.getModel().getAnnounce().getDiscussions().isEnabled();
    }

    @Override
    public void announce() throws AnnounceException {
        org.jreleaser.model.Github github = context.getModel().getRelease().getGithub();
        Discussions discussions = context.getModel().getAnnounce().getDiscussions();

        String message = "";
        if (isNotBlank(discussions.getMessage())) {
            message = discussions.getResolvedMessage(context.getModel());
        } else {
            Map<String, Object> props = new LinkedHashMap<>();
            props.put(Constants.KEY_CHANGELOG, context.getChangelog());
            context.getModel().getRelease().getGitService().fillProps(props, context.getModel());
            message = discussions.getResolvedMessageTemplate(context, props);
        }

        String title = discussions.getResolvedTitle(context.getModel());
        context.getLogger().info("title: {}", title);
        context.getLogger().debug("message: {}", message);

        try {
            Github api = new Github(context.getLogger(),
                github.getApiEndpoint(),
                github.getResolvedToken(),
                discussions.getConnectTimeout(),
                discussions.getReadTimeout());

            if (api.findDiscussion(discussions.getOrganization(), discussions.getTeam(), title).isPresent()) {
                throw new IllegalStateException("A discussion titled \"" + title + "\" has already been posted to " +
                    discussions.getOrganization() + "/" + discussions.getTeam());
            }

            api.createDiscussion(discussions.getOrganization(), discussions.getTeam(), title, message);
        } catch (IOException | IllegalStateException e) {
            context.getLogger().trace(e);
            throw new AnnounceException(e);
        }
    }
}

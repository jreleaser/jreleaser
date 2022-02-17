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
package org.jreleaser.sdk.slack;

import org.jreleaser.model.JReleaserContext;
import org.jreleaser.model.Slack;
import org.jreleaser.model.announcer.spi.AnnounceException;
import org.jreleaser.model.announcer.spi.Announcer;
import org.jreleaser.sdk.commons.ClientUtils;
import org.jreleaser.sdk.slack.api.Message;
import org.jreleaser.util.Constants;
import org.jreleaser.util.MustacheUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
@org.jreleaser.infra.nativeimage.annotations.NativeImage
public class SlackAnnouncer implements Announcer {
    private final JReleaserContext context;

    SlackAnnouncer(JReleaserContext context) {
        this.context = context;
    }

    @Override
    public String getName() {
        return org.jreleaser.model.Slack.NAME;
    }

    @Override
    public boolean isEnabled() {
        return context.getModel().getAnnounce().getSlack().isEnabled();
    }

    @Override
    public void announce() throws AnnounceException {
        Slack slack = context.getModel().getAnnounce().getSlack();

        String message = "";
        if (isNotBlank(slack.getMessage())) {
            message = slack.getResolvedMessage(context);
        } else {
            Map<String, Object> props = new LinkedHashMap<>();
            props.put(Constants.KEY_CHANGELOG, MustacheUtils.passThrough(context.getChangelog()));
            context.getModel().getRelease().getGitService().fillProps(props, context.getModel());
            message = slack.getResolvedMessageTemplate(context, props);
        }

        context.getLogger().debug("message: {}", message);

        List<String> errors = new ArrayList<>();
        if (isNotBlank(slack.getResolvedToken())) {
            context.getLogger().info("channel: {}", slack.getChannel());
            try {
                SlackSdk sdk = SlackSdk.builder(context.getLogger())
                    .connectTimeout(slack.getConnectTimeout())
                    .readTimeout(slack.getReadTimeout())
                    .token(context.isDryrun() ? "**UNDEFINED**" : slack.getResolvedToken())
                    .dryrun(context.isDryrun())
                    .build();

                sdk.message(slack.getChannel(), message);
            } catch (SlackException e) {
                context.getLogger().trace(e);
                errors.add(e.toString());
            }
        }

        if (isNotBlank(slack.getResolvedWebhook()) && !context.isDryrun()) {
            try {
                ClientUtils.webhook(context.getLogger(),
                    slack.getResolvedWebhook(),
                    slack.getConnectTimeout(),
                    slack.getReadTimeout(),
                    Message.of(message));
            } catch (AnnounceException e) {
                context.getLogger().trace(e);
                errors.add(e.toString());
            }
        }

        if (!errors.isEmpty()) {
            throw new AnnounceException(String.join(System.lineSeparator(), errors));
        }
    }
}

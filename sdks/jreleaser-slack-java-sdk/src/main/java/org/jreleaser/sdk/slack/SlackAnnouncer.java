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
package org.jreleaser.sdk.slack;

import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.spi.announce.AnnounceException;
import org.jreleaser.model.spi.announce.Announcer;
import org.jreleaser.mustache.TemplateContext;
import org.jreleaser.sdk.commons.ClientUtils;
import org.jreleaser.sdk.slack.api.Message;

import java.util.ArrayList;
import java.util.List;

import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
@org.jreleaser.infra.nativeimage.annotations.NativeImage
public class SlackAnnouncer implements Announcer<org.jreleaser.model.api.announce.SlackAnnouncer> {
    private final JReleaserContext context;
    private final org.jreleaser.model.internal.announce.SlackAnnouncer slack;

    public SlackAnnouncer(JReleaserContext context) {
        this.context = context;
        this.slack = context.getModel().getAnnounce().getSlack();
    }

    @Override
    public org.jreleaser.model.api.announce.SlackAnnouncer getAnnouncer() {
        return slack.asImmutable();
    }

    @Override
    public String getName() {
        return org.jreleaser.model.api.announce.SlackAnnouncer.TYPE;
    }

    @Override
    public boolean isEnabled() {
        return slack.isEnabled();
    }

    @Override
    public void announce() throws AnnounceException {
        String message = "";
        if (isNotBlank(slack.getMessage())) {
            message = slack.getResolvedMessage(context);
        } else {
            TemplateContext props = new TemplateContext();
            context.getModel().getRelease().getReleaser().fillProps(props, context.getModel());
            message = slack.getResolvedMessageTemplate(context, props);
        }

        context.getLogger().debug("message: {}", message);

        List<String> errors = new ArrayList<>();
        if (isNotBlank(slack.getToken())) {
            context.getLogger().info("channel: {}", slack.getChannel());
            try {
                SlackSdk sdk = SlackSdk.builder(context.asImmutable())
                    .connectTimeout(slack.getConnectTimeout())
                    .readTimeout(slack.getReadTimeout())
                    .token(context.isDryrun() ? "**UNDEFINED**" : slack.getToken())
                    .dryrun(context.isDryrun())
                    .build();

                sdk.message(slack.getChannel(), message);
            } catch (SlackException e) {
                context.getLogger().trace(e);
                errors.add(e.toString());
            }
        }

        if (isNotBlank(slack.getWebhook()) && !context.isDryrun()) {
            try {
                ClientUtils.webhook(context.getLogger(),
                    slack.getWebhook(),
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

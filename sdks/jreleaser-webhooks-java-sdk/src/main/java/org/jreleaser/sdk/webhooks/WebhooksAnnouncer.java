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
package org.jreleaser.sdk.webhooks;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jreleaser.bundle.RB;
import org.jreleaser.extensions.api.workflow.WorkflowListenerException;
import org.jreleaser.model.api.JReleaserCommand;
import org.jreleaser.model.api.hooks.ExecutionEvent;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.internal.announce.WebhookAnnouncer;
import org.jreleaser.model.spi.announce.AnnounceException;
import org.jreleaser.model.spi.announce.Announcer;
import org.jreleaser.mustache.TemplateContext;
import org.jreleaser.sdk.commons.ClientUtils;
import org.jreleaser.util.CollectionUtils;

import java.util.Map;

import static org.jreleaser.model.Constants.KEY_CHANGELOG;
import static org.jreleaser.model.Constants.KEY_CHANGELOG_CHANGES;
import static org.jreleaser.model.Constants.KEY_CHANGELOG_CONTRIBUTORS;
import static org.jreleaser.mustache.MustacheUtils.passThrough;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.5.0
 */
@org.jreleaser.infra.nativeimage.annotations.NativeImage
public class WebhooksAnnouncer implements Announcer<org.jreleaser.model.api.announce.WebhooksAnnouncer> {
    private final JReleaserContext context;
    private final org.jreleaser.model.internal.announce.WebhooksAnnouncer webhooks;

    public WebhooksAnnouncer(JReleaserContext context) {
        this.context = context;
        this.webhooks = context.getModel().getAnnounce().getConfiguredWebhooks();
    }

    @Override
    public org.jreleaser.model.api.announce.WebhooksAnnouncer getAnnouncer() {
        return webhooks.asImmutable();
    }

    @Override
    public String getName() {
        return org.jreleaser.model.api.announce.WebhooksAnnouncer.TYPE;
    }

    @Override
    public boolean isEnabled() {
        return webhooks.isEnabled();
    }

    @Override
    public void announce() throws AnnounceException {
        Map<String, WebhookAnnouncer> webhooks = this.webhooks.getWebhooks();

        for (Map.Entry<String, WebhookAnnouncer> e : webhooks.entrySet()) {
            if (e.getValue().isEnabled()) {
                context.getLogger().setPrefix("webhook." + e.getKey());
                try {
                    announce(context, e.getValue(), false);
                } catch (AnnounceException x) {
                    context.getLogger().warn(x.getMessage().trim());
                } finally {
                    context.getLogger().restorePrefix();
                }
            }
        }
    }

    public static void announce(JReleaserContext context, WebhookAnnouncer webhook, boolean discreet) throws AnnounceException {
        String message = "";
        if (isNotBlank(webhook.getMessage())) {
            message = webhook.getResolvedMessage(context);
        } else {
            TemplateContext props = new TemplateContext();
            if ("teams".equals(webhook.getName())) {
                props.set(KEY_CHANGELOG, passThrough(convertLineEndings(context.getChangelog().getResolvedChangelog())));
                props.set(KEY_CHANGELOG_CHANGES, passThrough(convertLineEndings(context.getChangelog().getFormattedChanges())));
                props.set(KEY_CHANGELOG_CONTRIBUTORS, passThrough(convertLineEndings(context.getChangelog().getFormattedContributors())));
            }
            context.getModel().getRelease().getReleaser().fillProps(props, context.getModel());
            message = webhook.getResolvedMessageTemplate(context, props);
        }

        if (webhook.isStructuredMessage() && isNotBlank(webhook.getMessageProperty())) {
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                message = objectMapper.writeValueAsString(CollectionUtils.mapOf(webhook.getMessageProperty(), message));
            } catch (JsonProcessingException e) {
                throw new AnnounceException(RB.$("ERROR_unexpected_json_format"), e);
            }
        }

        if (discreet) {
            context.getLogger().info(RB.$("webhook.message.send"));
        } else {
            context.getLogger().info(message);
        }

        if (!context.isDryrun()) {
            fireAnnouncerEvent(ExecutionEvent.before(JReleaserCommand.ANNOUNCE.toStep()), context, webhook);

            try {
                ClientUtils.webhook(context.getLogger(),
                    webhook.getWebhook(),
                    webhook.getConnectTimeout(),
                    webhook.getReadTimeout(),
                    message);

                fireAnnouncerEvent(ExecutionEvent.success(JReleaserCommand.ANNOUNCE.toStep()), context, webhook);
            } catch (RuntimeException e) {
                fireAnnouncerEvent(ExecutionEvent.failure(JReleaserCommand.ANNOUNCE.toStep(), e), context, webhook);

                throw e;
            }
        }
    }

    private static void fireAnnouncerEvent(ExecutionEvent event, JReleaserContext context, WebhookAnnouncer webhook) {
        try {
            context.fireAnnounceStepEvent(event, webhook.asImmutable());
        } catch (WorkflowListenerException e) {
            context.getLogger().error(RB.$("listener.failure", e.getListener().getClass().getName()));
            context.getLogger().trace(e);
        }
    }

    public static String convertLineEndings(String str) {
        return str.replaceAll("\\n", "\\\\n\\\\n");
    }
}

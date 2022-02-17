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
package org.jreleaser.sdk.webhooks;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jreleaser.bundle.RB;
import org.jreleaser.model.JReleaserContext;
import org.jreleaser.model.Webhook;
import org.jreleaser.model.announcer.spi.AnnounceException;
import org.jreleaser.model.announcer.spi.Announcer;
import org.jreleaser.sdk.commons.ClientUtils;
import org.jreleaser.util.CollectionUtils;
import org.jreleaser.util.Constants;
import org.jreleaser.util.MustacheUtils;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.5.0
 */
@org.jreleaser.infra.nativeimage.annotations.NativeImage
public class WebhooksAnnouncer implements Announcer {
    private final JReleaserContext context;

    WebhooksAnnouncer(JReleaserContext context) {
        this.context = context;
    }

    @Override
    public String getName() {
        return org.jreleaser.model.Webhooks.NAME;
    }

    @Override
    public boolean isEnabled() {
        return context.getModel().getAnnounce().getConfiguredWebhooks().isEnabled();
    }

    @Override
    public void announce() throws AnnounceException {
        Map<String, Webhook> webhooks = context.getModel().getAnnounce().getWebhooks();

        for (Map.Entry<String, Webhook> e : webhooks.entrySet()) {
            if (e.getValue().isEnabled()) {
                context.getLogger().setPrefix("webhook." + e.getKey());
                try {
                    announce(e.getValue());
                } catch (AnnounceException x) {
                    context.getLogger().warn(x.getMessage().trim());
                } finally {
                    context.getLogger().restorePrefix();
                }
            }
        }
    }

    public void announce(Webhook webhook) throws AnnounceException {
        String message = "";
        if (isNotBlank(webhook.getMessage())) {
            message = webhook.getResolvedMessage(context);

            ObjectMapper objectMapper = new ObjectMapper();
            try {
                message = objectMapper.writeValueAsString(CollectionUtils.newMap(webhook.getMessageProperty(), message));
            } catch (JsonProcessingException e) {
                throw new AnnounceException(RB.$("ERROR_unexpected_json_format"), e);
            }
        } else {
            Map<String, Object> props = new LinkedHashMap<>();
            props.put(Constants.KEY_CHANGELOG, MustacheUtils.passThrough(context.getChangelog()));
            context.getModel().getRelease().getGitService().fillProps(props, context.getModel());
            message = webhook.getResolvedMessageTemplate(context, props);
        }

        context.getLogger().info("message: {}", message);

        if (!context.isDryrun()) {
            ClientUtils.webhook(context.getLogger(),
                webhook.getResolvedWebhook(),
                webhook.getConnectTimeout(),
                webhook.getReadTimeout(),
                message);
        }
    }
}

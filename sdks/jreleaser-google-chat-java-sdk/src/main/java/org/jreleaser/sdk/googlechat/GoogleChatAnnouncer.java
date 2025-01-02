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
package org.jreleaser.sdk.googlechat;

import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.spi.announce.AnnounceException;
import org.jreleaser.model.spi.announce.Announcer;
import org.jreleaser.sdk.webhooks.WebhooksAnnouncer;

/**
 * @author Anyul Rivas
 * @since 0.5.0
 */
@org.jreleaser.infra.nativeimage.annotations.NativeImage
public class GoogleChatAnnouncer implements Announcer<org.jreleaser.model.api.announce.GoogleChatAnnouncer> {
    private final JReleaserContext context;
    private final org.jreleaser.model.internal.announce.GoogleChatAnnouncer googleChat;

    public GoogleChatAnnouncer(JReleaserContext context) {
        this.context = context;
        this.googleChat = context.getModel().getAnnounce().getGoogleChat();
    }

    @Override
    public org.jreleaser.model.api.announce.GoogleChatAnnouncer getAnnouncer() {
        return googleChat.asImmutable();
    }

    @Override
    public String getName() {
        return org.jreleaser.model.api.announce.GoogleChatAnnouncer.TYPE;
    }

    @Override
    public boolean isEnabled() {
        return googleChat.isEnabled();
    }

    @Override
    public void announce() throws AnnounceException {
        context.getLogger().setPrefix("webhook." + getName());
        try {
            WebhooksAnnouncer.announce(context, googleChat.asWebhookAnnouncer(), false);
        } catch (AnnounceException x) {
            context.getLogger().warn(x.getMessage().trim());
        } finally {
            context.getLogger().restorePrefix();
        }
    }
}

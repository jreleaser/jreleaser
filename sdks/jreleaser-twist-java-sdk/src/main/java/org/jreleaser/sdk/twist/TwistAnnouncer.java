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
package org.jreleaser.sdk.twist;

import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.spi.announce.AnnounceException;
import org.jreleaser.model.spi.announce.Announcer;
import org.jreleaser.sdk.webhooks.WebhooksAnnouncer;

/**
 * @author Usman Shaikh
 * @since 1.23.0
 */
@org.jreleaser.infra.nativeimage.annotations.NativeImage
public class TwistAnnouncer implements Announcer<org.jreleaser.model.api.announce.TwistAnnouncer> {
    private final JReleaserContext context;
    private final org.jreleaser.model.internal.announce.TwistAnnouncer twist;

    public TwistAnnouncer(JReleaserContext context) {
        this.context = context;
        this.twist = context.getModel().getAnnounce().getTwist();
    }

    @Override
    public org.jreleaser.model.api.announce.TwistAnnouncer getAnnouncer() {
        return twist.asImmutable();
    }

    @Override
    public String getName() {
        return org.jreleaser.model.api.announce.TwistAnnouncer.TYPE;
    }

    @Override
    public boolean isEnabled() {
        return twist.isEnabled();
    }

    @Override
    public void announce() throws AnnounceException {
        context.getLogger().setPrefix("webhook."  + getName());
        try {
            WebhooksAnnouncer.announce(context, twist.asWebhookAnnouncer(), true);
        } catch (AnnounceException e) {
            context.getLogger().warn(e.getMessage().trim());
        } finally {
            context.getLogger().restorePrefix();
        }
    }
}

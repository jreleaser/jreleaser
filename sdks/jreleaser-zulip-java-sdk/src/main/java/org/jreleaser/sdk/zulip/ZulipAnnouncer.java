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
package org.jreleaser.sdk.zulip;

import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.spi.announce.AnnounceException;
import org.jreleaser.model.spi.announce.Announcer;
import org.jreleaser.mustache.TemplateContext;

import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
@org.jreleaser.infra.nativeimage.annotations.NativeImage
public class ZulipAnnouncer implements Announcer<org.jreleaser.model.api.announce.ZulipAnnouncer> {
    private final JReleaserContext context;
    private final org.jreleaser.model.internal.announce.ZulipAnnouncer zulip;

    public ZulipAnnouncer(JReleaserContext context) {
        this.context = context;
        this.zulip = context.getModel().getAnnounce().getZulip();
    }

    @Override
    public org.jreleaser.model.api.announce.ZulipAnnouncer getAnnouncer() {
        return zulip.asImmutable();
    }

    @Override
    public String getName() {
        return org.jreleaser.model.api.announce.ZulipAnnouncer.TYPE;
    }

    @Override
    public boolean isEnabled() {
        return zulip.isEnabled();
    }

    @Override
    public void announce() throws AnnounceException {
        String message = "";
        if (isNotBlank(zulip.getMessage())) {
            message = zulip.getResolvedMessage(context);
        } else {
            TemplateContext props = new TemplateContext();
            context.getModel().getRelease().getReleaser().fillProps(props, context.getModel());
            message = zulip.getResolvedMessageTemplate(context, props);
        }

        String subject = zulip.getResolvedSubject(context);
        context.getLogger().info("channel: {}", zulip.getChannel());
        context.getLogger().info("subject: {}", subject);
        context.getLogger().debug("message: {}", message);

        try {
            ZulipSdk sdk = ZulipSdk.builder(context.asImmutable())
                .apiHost(zulip.getApiHost())
                .account(zulip.getAccount())
                .apiKey(context.isDryrun() ? "**UNDEFINED**" : zulip.getApiKey())
                .connectTimeout(zulip.getConnectTimeout())
                .readTimeout(zulip.getReadTimeout())
                .dryrun(context.isDryrun())
                .build();
            sdk.message(zulip.getChannel(), subject, message);
        } catch (ZulipException e) {
            throw new AnnounceException(e);
        }
    }
}

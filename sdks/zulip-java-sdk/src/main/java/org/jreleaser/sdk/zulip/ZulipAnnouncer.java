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
package org.jreleaser.sdk.zulip;

import org.jreleaser.model.JReleaserContext;
import org.jreleaser.model.Zulip;
import org.jreleaser.model.announcer.spi.AnnounceException;
import org.jreleaser.model.announcer.spi.Announcer;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class ZulipAnnouncer implements Announcer {
    private final JReleaserContext context;

    ZulipAnnouncer(JReleaserContext context) {
        this.context = context;
    }

    @Override
    public String getName() {
        return org.jreleaser.model.Zulip.NAME;
    }

    @Override
    public boolean isEnabled() {
        return context.getModel().getAnnounce().getZulip().isEnabled();
    }

    @Override
    public boolean isSnapshotSupported() {
        return true;
    }

    @Override
    public void announce() throws AnnounceException {
        Zulip zulip = context.getModel().getAnnounce().getZulip();

        String subject = zulip.getResolvedSubject(context.getModel());
        String message = zulip.getResolvedMessage(context.getModel());
        context.getLogger().info("channel: {}", zulip.getChannel());
        context.getLogger().info("subject: {}", subject);
        context.getLogger().info("message: {}", message);

        try {
            MessageZulipCommand.builder(context.getLogger())
                .account(zulip.getAccount())
                .apiKey(zulip.getResolvedApiKey())
                .apiHost(zulip.getApiHost())
                .channel(zulip.getChannel())
                .subject(subject)
                .message(message)
                .dryrun(context.isDryrun())
                .build()
                .execute();
        } catch (ZulipException e) {
            throw new AnnounceException(e);
        }
    }
}

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

import org.jreleaser.model.JReleaserModel;
import org.jreleaser.model.Zulip;
import org.jreleaser.model.announcer.spi.AbstractAnnouncerBuilder;
import org.jreleaser.model.announcer.spi.AnnounceException;
import org.jreleaser.model.announcer.spi.Announcer;
import org.jreleaser.util.Logger;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class ZulipAnnouncer implements Announcer {
    private final Logger logger;
    private final JReleaserModel model;

    private ZulipAnnouncer(Logger logger, JReleaserModel model) {
        this.logger = logger;
        this.model = model;
    }

    @Override
    public void announce(boolean dryrun) throws AnnounceException {
        Zulip zulip = model.getAnnouncers().getZulip();
        if (!zulip.isEnabled()) {
            logger.info("Zulip announcer is disabled");
            return;
        }

        String subject = zulip.getResolvedSubject(model);
        String message = zulip.getResolvedMessage(model);
        logger.info("Announcing on Zulip: {}{}{}", subject, System.lineSeparator(), message);

        try {
            MessageZulipCommand.builder(logger)
                .account(zulip.getAccount())
                .apiKey(zulip.getResolvedApiKey())
                .apiHost(zulip.getApiHost())
                .channel(zulip.getChannel())
                .subject(subject)
                .message(message)
                .dryrun(dryrun)
                .build()
                .execute();
        } catch (ZulipException e) {
            throw new AnnounceException(e);
        }
    }

    public static Builder builder(Logger logger) {
        Builder builder = new Builder();
        builder.logger(logger);
        return builder;
    }

    public static class Builder extends AbstractAnnouncerBuilder<ZulipAnnouncer, Builder> {
        @Override
        public ZulipAnnouncer build() {
            validate();

            return new ZulipAnnouncer(logger, model);
        }
    }
}

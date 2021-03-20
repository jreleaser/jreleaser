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
package org.jreleaser.announce;

import org.jreleaser.model.JReleaserContext;
import org.jreleaser.model.JReleaserModel;
import org.jreleaser.model.announcer.spi.AnnounceException;
import org.jreleaser.model.announcer.spi.AnnouncerBuilder;
import org.jreleaser.sdk.sdkman.SdkmanAnnouncer;
import org.jreleaser.sdk.twitter.TwitterAnnouncer;
import org.jreleaser.sdk.zulip.ZulipAnnouncer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class Announcers {
    public static void announce(JReleaserContext context) throws AnnounceException {
        context.getLogger().info("Announcing release");
        for (AnnouncerBuilder announcer : Announcers.findAnnouncers(context.getModel())) {
            announcer.configureWith(context)
                .build()
                .announce();
        }
    }

    private static <AB extends AnnouncerBuilder> Collection<AB> findAnnouncers(JReleaserModel model) {
        List<AB> announcers = new ArrayList<>();
        if (null != model.getAnnounce().getSdkman() && model.getAnnounce().getSdkman().isEnabled()) {
            announcers.add((AB) SdkmanAnnouncer.builder());
        }
        if (null != model.getAnnounce().getTwitter() && model.getAnnounce().getTwitter().isEnabled()) {
            announcers.add((AB) TwitterAnnouncer.builder());
        }
        if (null != model.getAnnounce().getZulip() && model.getAnnounce().getZulip().isEnabled()) {
            announcers.add((AB) ZulipAnnouncer.builder());
        }

        if (announcers.isEmpty()) {
            throw new IllegalArgumentException("No suitable announcers have been configured");
        }

        return announcers;
    }
}

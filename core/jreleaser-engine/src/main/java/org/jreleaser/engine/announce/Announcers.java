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
package org.jreleaser.engine.announce;

import org.jreleaser.model.JReleaserContext;
import org.jreleaser.model.JReleaserModel;
import org.jreleaser.model.Mail;
import org.jreleaser.model.Sdkman;
import org.jreleaser.model.Twitter;
import org.jreleaser.model.Zulip;
import org.jreleaser.model.announcer.spi.AnnounceException;
import org.jreleaser.model.announcer.spi.Announcer;
import org.jreleaser.model.announcer.spi.AnnouncerBuilder;
import org.jreleaser.model.announcer.spi.AnnouncerBuilderFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public class Announcers {
    public static void announce(JReleaserContext context) throws AnnounceException {
        context.getLogger().info("Announcing release");
        if (!context.getModel().getAnnounce().isEnabled()) {
            context.getLogger().info("Announcing is not enabled. Skipping.");
            return;
        }

        for (AnnouncerBuilder builder : Announcers.findAnnouncers(context)) {
            Announcer announcer = builder.configureWith(context).build();

            context.getLogger().increaseIndent();
            context.getLogger().setPrefix(announcer.getName());

            if (announcer.isEnabled()) {
                if (context.getModel().getProject().isSnapshot() && !announcer.isSnapshotSupported()) {
                    context.getLogger().info("snapshots are not supported. Skipping");
                } else {
                    try {
                        announcer.announce();
                    } catch (AnnounceException e) {
                        context.getLogger().warn(e.getMessage().trim());
                    }
                }
            } else {
                context.getLogger().debug("not enabled");
            }

            context.getLogger().restorePrefix();
            context.getLogger().decreaseIndent();
        }
    }

    private static <AB extends AnnouncerBuilder> Collection<AB> findAnnouncers(JReleaserContext context) {
        JReleaserModel model = context.getModel();

        Map<String, AnnouncerBuilder> builders = StreamSupport.stream(ServiceLoader.load(AnnouncerBuilderFactory.class,
            Announcers.class.getClassLoader()).spliterator(), false)
            .collect(Collectors.toMap(AnnouncerBuilderFactory::getName, AnnouncerBuilderFactory::getBuilder));

        List<AB> announcers = new ArrayList<>();
        if (null != model.getAnnounce().getMail() && model.getAnnounce().getMail().isEnabled()) {
            announcers.add((AB) builders.get(Mail.NAME));
        }
        if (null != model.getAnnounce().getSdkman() && model.getAnnounce().getSdkman().isEnabled()) {
            announcers.add((AB) builders.get(Sdkman.NAME));
        }
        if (null != model.getAnnounce().getTwitter() && model.getAnnounce().getTwitter().isEnabled()) {
            announcers.add((AB) builders.get(Twitter.NAME));
        }
        if (null != model.getAnnounce().getZulip() && model.getAnnounce().getZulip().isEnabled()) {
            announcers.add((AB) builders.get(Zulip.NAME));
        }

        if (announcers.isEmpty()) {
            context.getLogger().info("No announcers have been configured. Skipping.");
        }

        return announcers;
    }
}

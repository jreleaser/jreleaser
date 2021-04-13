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

import org.jreleaser.model.Discussions;
import org.jreleaser.model.JReleaserContext;
import org.jreleaser.model.JReleaserModel;
import org.jreleaser.model.Mail;
import org.jreleaser.model.Sdkman;
import org.jreleaser.model.Slack;
import org.jreleaser.model.Twitter;
import org.jreleaser.model.Zulip;
import org.jreleaser.model.announcer.spi.AnnounceException;
import org.jreleaser.model.announcer.spi.Announcer;
import org.jreleaser.model.announcer.spi.AnnouncerBuilder;
import org.jreleaser.model.announcer.spi.AnnouncerBuilderFactory;

import java.util.LinkedHashMap;
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

        if (context.hasAnnouncerName()) {
            Announcer announcer = Announcers.findAnnouncers(context)
                .get(context.getAnnouncerName());

            if (null == announcer) {
                context.getLogger().warn("Announcer [{}] not found. Skipping.", context.getAnnouncerName());
                return;
            }

            announce(context, announcer);
            return;
        }

        for (Map.Entry<String, Announcer> entry : Announcers.findAnnouncers(context).entrySet()) {
            announce(context, entry.getValue());
        }
    }

    private static void announce(JReleaserContext context, Announcer announcer) {
        context.getLogger().increaseIndent();
        context.getLogger().setPrefix(announcer.getName());

        if (announcer.isEnabled()) {
            try {
                announcer.announce();
            } catch (AnnounceException e) {
                context.getLogger().warn(e.getMessage().trim());
            }
        } else {
            context.getLogger().debug("disabled. Skipping");
        }

        context.getLogger().restorePrefix();
        context.getLogger().decreaseIndent();
    }

    private static Map<String, Announcer> findAnnouncers(JReleaserContext context) {
        JReleaserModel model = context.getModel();

        Map<String, AnnouncerBuilder> builders = StreamSupport.stream(ServiceLoader.load(AnnouncerBuilderFactory.class,
            Announcers.class.getClassLoader()).spliterator(), false)
            .collect(Collectors.toMap(AnnouncerBuilderFactory::getName, AnnouncerBuilderFactory::getBuilder));

        Map<String, Announcer> announcers = new LinkedHashMap<>();
        if (null != model.getAnnounce().getDiscussions()) {
            announcers.put(Discussions.NAME, builders.get(Discussions.NAME).configureWith(context).build());
        }
        if (null != model.getAnnounce().getMail()) {
            announcers.put(Mail.NAME, builders.get(Mail.NAME).configureWith(context).build());
        }
        if (null != model.getAnnounce().getSdkman()) {
            announcers.put(Sdkman.NAME, builders.get(Sdkman.NAME).configureWith(context).build());
        }
        if (null != model.getAnnounce().getSlack()) {
            announcers.put(Slack.NAME, builders.get(Slack.NAME).configureWith(context).build());
        }
        if (null != model.getAnnounce().getTwitter()) {
            announcers.put(Twitter.NAME, builders.get(Twitter.NAME).configureWith(context).build());
        }
        if (null != model.getAnnounce().getZulip()) {
            announcers.put(Zulip.NAME, builders.get(Zulip.NAME).configureWith(context).build());
        }

        if (announcers.isEmpty()) {
            context.getLogger().info("No announcers have been configured. Skipping.");
        }

        return announcers;
    }
}

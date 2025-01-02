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
package org.jreleaser.engine.announce;

import org.jreleaser.bundle.RB;
import org.jreleaser.extensions.api.workflow.WorkflowListenerException;
import org.jreleaser.model.api.JReleaserCommand;
import org.jreleaser.model.api.hooks.ExecutionEvent;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.internal.JReleaserModel;
import org.jreleaser.model.spi.announce.AnnounceException;
import org.jreleaser.model.spi.announce.Announcer;
import org.jreleaser.model.spi.announce.AnnouncerBuilder;
import org.jreleaser.model.spi.announce.AnnouncerBuilderFactory;

import java.util.Map;
import java.util.ServiceLoader;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.jreleaser.model.internal.JReleaserSupport.supportedAnnouncers;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public final class Announcers {
    private Announcers() {
        // noop
    }

    public static void announce(JReleaserContext context) {
        context.getLogger().info(RB.$("announcers.header"));
        context.getLogger().increaseIndent();
        context.getLogger().setPrefix("announce");

        if (!context.getModel().getAnnounce().isEnabled()) {
            context.getLogger().info(RB.$("announcers.not.enabled"));
            context.getLogger().decreaseIndent();
            context.getLogger().restorePrefix();
            return;
        }

        Map<String, Announcer<?>> announcers = Announcers.findAnnouncers(context);
        if (announcers.isEmpty()) {
            context.getLogger().info(RB.$("announcers.not.configured"));
            context.getLogger().decreaseIndent();
            context.getLogger().restorePrefix();
            return;
        }

        if (!context.getIncludedAnnouncers().isEmpty()) {
            boolean announced = false;
            for (String announcerName : context.getIncludedAnnouncers()) {
                // check if the announcer name is valid
                if (!supportedAnnouncers().contains(announcerName)) {
                    context.getLogger().warn(RB.$("ERROR_unsupported_announcer", announcerName));
                    continue;
                }

                Announcer<?> announcer = announcers.get(announcerName);

                if (null == announcer) {
                    context.getLogger().warn(RB.$("announcers.announcer.not.found"), announcerName);
                    continue;
                }

                if (!announcer.isEnabled()) {
                    context.getLogger().warn(RB.$("announcers.announcer.not.enabled"), announcerName);
                    continue;
                }

                if (announce(context, announcer)) announced = true;
            }

            if (!announced) {
                context.getLogger().info(RB.$("announcers.not.triggered"));
            }
            context.getLogger().decreaseIndent();
            context.getLogger().restorePrefix();
            return;
        }

        boolean announced = false;
        for (Map.Entry<String, Announcer<?>> entry : announcers.entrySet()) {
            Announcer<?> announcer = entry.getValue();

            if (context.getExcludedAnnouncers().contains(announcer.getName())) {
                context.getLogger().info(RB.$("announcers.announcer.excluded"), announcer.getName());
                continue;
            }

            if (announce(context, announcer)) announced = true;
        }

        if (!announced) {
            context.getLogger().info(RB.$("announcers.not.triggered"));
        }
        context.getLogger().decreaseIndent();
        context.getLogger().restorePrefix();
    }

    private static boolean announce(JReleaserContext context, Announcer<?> announcer) {
        try {
            context.getLogger().setPrefix(announcer.getName());

            if (announcer.isEnabled()) {
                fireAnnounceEvent(ExecutionEvent.before(JReleaserCommand.ANNOUNCE.toStep()), context, announcer);

                try {
                    announcer.announce();
                    fireAnnounceEvent(ExecutionEvent.success(JReleaserCommand.ANNOUNCE.toStep()), context, announcer);
                    return true;
                } catch (AnnounceException e) {
                    fireAnnounceEvent(ExecutionEvent.failure(JReleaserCommand.ANNOUNCE.toStep(), e), context, announcer);
                    context.getLogger().warn(e.getMessage().trim());
                    return true;
                }
            } else {
                context.getLogger().debug(RB.$("announcers.announcer.disabled"));
            }
        } finally {
            context.getLogger().restorePrefix();
        }

        return false;
    }

    private static void fireAnnounceEvent(ExecutionEvent event, JReleaserContext context, Announcer<?> announcer) {
        try {
            context.fireAnnounceStepEvent(event, announcer.getAnnouncer());
        } catch (WorkflowListenerException e) {
            context.getLogger().error(RB.$("listener.failure", e.getListener().getClass().getName()));
            context.getLogger().trace(e);
        }
    }

    private static Map<String, Announcer<?>> findAnnouncers(JReleaserContext context) {
        JReleaserModel model = context.getModel();

        Map<String, AnnouncerBuilder<?>> builders = StreamSupport.stream(ServiceLoader.load(AnnouncerBuilderFactory.class,
                Announcers.class.getClassLoader()).spliterator(), false)
            .collect(Collectors.toMap(AnnouncerBuilderFactory::getName, AnnouncerBuilderFactory::getBuilder));

        Map<String, Announcer<?>> announcers = new TreeMap<>();
        builders.forEach((name, builder) -> {
            if (null != model.getAnnounce().findAnnouncer(name) &&
                !context.getExcludedAnnouncers().contains(name)) {
                announcers.put(name, builder.configureWith(context).build());
            }
        });

        return announcers;
    }
}

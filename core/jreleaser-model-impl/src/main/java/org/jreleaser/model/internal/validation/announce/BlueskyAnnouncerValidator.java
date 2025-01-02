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
package org.jreleaser.model.internal.validation.announce;

import org.jreleaser.bundle.RB;
import org.jreleaser.model.internal.JReleaserContext;
import org.jreleaser.model.internal.announce.BlueskyAnnouncer;
import org.jreleaser.util.Errors;

import java.nio.file.Files;

import static org.jreleaser.model.api.announce.BlueskyAnnouncer.BLUESKY_PASSWORD;
import static org.jreleaser.model.internal.validation.common.Validator.checkProperty;
import static org.jreleaser.model.internal.validation.common.Validator.resolveActivatable;
import static org.jreleaser.model.internal.validation.common.Validator.validateTimeout;
import static org.jreleaser.util.CollectionUtils.listOf;
import static org.jreleaser.util.StringUtils.isBlank;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Simon Verhoeven
 * @author Tom Cools
 * @since 1.7.0
 */
public class BlueskyAnnouncerValidator {
    private BlueskyAnnouncerValidator() {
        // noop
    }

    public static void validateBluesky(JReleaserContext context, BlueskyAnnouncer announcer, Errors errors) {
        context.getLogger().debug("announce.bluesky");

        resolveActivatable(context, announcer, "announce.bluesky", "NEVER");
        if (!announcer.resolveEnabledWithSnapshot(context.getModel().getProject())) {
            context.getLogger().debug(RB.$("validation.disabled"));
            return;
        }

        announcer.setHost(
            checkProperty(context,
                listOf(
                    "announce.bluesky.host",
                    "bluesky.host"),
                "announce.bluesky.host",
                announcer.getHost(),
                errors));

        announcer.setHandle(
            checkProperty(context,
                listOf(
                    "announce.bluesky.handle",
                    "bluesky.handle"),
                "announce.bluesky.handle",
                announcer.getHandle(),
                errors));

        announcer.setPassword(
            checkProperty(context,
                listOf(
                    "announce.bluesky.password",
                    BLUESKY_PASSWORD),
                "announce.bluesky.password",
                announcer.getPassword(),
                errors,
                context.isDryrun()));

        if (isNotBlank(announcer.getStatusTemplate()) &&
            !Files.exists(context.getBasedir().resolve(announcer.getStatusTemplate().trim()))) {
            errors.configuration(RB.$("validation_directory_not_exist", "bluesky.statusTemplate", announcer.getStatusTemplate()));
        }

        if (isBlank(announcer.getStatus()) && isBlank(announcer.getStatusTemplate()) && announcer.getStatuses().isEmpty()) {
            announcer.setStatus(RB.$("default.release.message"));
        }

        validateTimeout(announcer);
    }
}

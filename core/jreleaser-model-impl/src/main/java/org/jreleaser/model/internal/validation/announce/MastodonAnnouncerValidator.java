/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020-2023 The JReleaser authors.
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
import org.jreleaser.model.internal.announce.MastodonAnnouncer;
import org.jreleaser.util.Errors;

import java.nio.file.Files;

import static org.jreleaser.model.api.announce.MastodonAnnouncer.MASTODON_ACCESS_TOKEN;
import static org.jreleaser.model.internal.validation.common.Validator.checkProperty;
import static org.jreleaser.model.internal.validation.common.Validator.resolveActivatable;
import static org.jreleaser.model.internal.validation.common.Validator.validateTimeout;
import static org.jreleaser.util.CollectionUtils.listOf;
import static org.jreleaser.util.StringUtils.isBlank;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.4.0
 */
public final class MastodonAnnouncerValidator {
    private MastodonAnnouncerValidator() {
        // noop
    }

    public static void validateMastodon(JReleaserContext context, MastodonAnnouncer mastodon, Errors errors) {
        context.getLogger().debug("announce.mastodon");
        resolveActivatable(context, mastodon, "announce.mastodon", "NEVER");
        if (!mastodon.resolveEnabledWithSnapshot(context.getModel().getProject())) {
            context.getLogger().debug(RB.$("validation.disabled"));
            return;
        }

        mastodon.setHost(
            checkProperty(context,
                listOf(
                    "announce.mastodon.host",
                    "mastodon.host"),
                "announce.mastodon.host",
                mastodon.getHost(),
                errors));

        mastodon.setAccessToken(
            checkProperty(context,
                listOf(
                    "announce.mastodon.access.token",
                    MASTODON_ACCESS_TOKEN),
                "announce.mastodon.accessToken",
                mastodon.getAccessToken(),
                errors,
                context.isDryrun()));

        if (isNotBlank(mastodon.getStatusTemplate()) &&
            !Files.exists(context.getBasedir().resolve(mastodon.getStatusTemplate().trim()))) {
            errors.configuration(RB.$("validation_directory_not_exist", "mastodon.statusTemplate", mastodon.getStatusTemplate()));
        }

        if (isBlank(mastodon.getStatus()) && isBlank(mastodon.getStatusTemplate()) && mastodon.getStatuses().isEmpty()) {
            mastodon.setStatus(RB.$("default.release.message"));
        }

        validateTimeout(mastodon);
    }
}
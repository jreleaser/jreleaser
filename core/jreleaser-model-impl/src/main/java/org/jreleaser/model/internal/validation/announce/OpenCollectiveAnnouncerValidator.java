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
import org.jreleaser.model.internal.announce.OpenCollectiveAnnouncer;
import org.jreleaser.util.Errors;

import java.nio.file.Files;

import static org.jreleaser.model.api.announce.OpenCollectiveAnnouncer.OPENCOLLECTIVE_SLUG;
import static org.jreleaser.model.api.announce.OpenCollectiveAnnouncer.OPENCOLLECTIVE_TOKEN;
import static org.jreleaser.model.internal.validation.common.Validator.checkProperty;
import static org.jreleaser.model.internal.validation.common.Validator.resolveActivatable;
import static org.jreleaser.model.internal.validation.common.Validator.validateTimeout;
import static org.jreleaser.util.CollectionUtils.listOf;
import static org.jreleaser.util.StringUtils.isBlank;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 1.6.0
 */
public final class OpenCollectiveAnnouncerValidator {
    private static final String DEFAULT_OPENCOLLECTIVE_TPL = "src/jreleaser/templates/opencollective.tpl";

    private OpenCollectiveAnnouncerValidator() {
        // noop
    }

    public static void validateOpenCollective(JReleaserContext context, OpenCollectiveAnnouncer announcer, Errors errors) {
        context.getLogger().debug("announce.openCollective");
        resolveActivatable(context, announcer, "announce.openCollective", "NEVER");
        if (!announcer.resolveEnabledWithSnapshot(context.getModel().getProject())) {
            context.getLogger().debug(RB.$("validation.disabled"));
            return;
        }

        if (isBlank(announcer.getHost())) {
            announcer.setHost("https://api.opencollective.com/graphql/v2");
        }

        announcer.setToken(
            checkProperty(context,
                listOf(
                    "announce.openCollective.token",
                    OPENCOLLECTIVE_TOKEN),
                "announce.openCollective.token",
                announcer.getToken(),
                errors,
                context.isDryrun()));

        announcer.setSlug(
            checkProperty(context,
                listOf(
                    "announce.openCollective.slug",
                    OPENCOLLECTIVE_SLUG),
                "announce.openCollective.slug",
                announcer.getSlug(),
                context.getModel().getProject().getResolvedName()));

        if (isBlank(announcer.getTitle())) {
            announcer.setTitle(RB.$("default.discussion.title"));
        }

        if (isBlank(announcer.getMessage()) && isBlank(announcer.getMessageTemplate())) {
            if (Files.exists(context.getBasedir().resolve(DEFAULT_OPENCOLLECTIVE_TPL))) {
                announcer.setMessageTemplate(DEFAULT_OPENCOLLECTIVE_TPL);
            } else {
                announcer.setMessage(RB.$("default.release.message.html"));
            }
        }

        if (isNotBlank(announcer.getMessageTemplate()) &&
            !Files.exists(context.getBasedir().resolve(announcer.getMessageTemplate().trim()))) {
            errors.configuration(RB.$("validation_directory_not_exist", "openCollective.messageTemplate", announcer.getMessageTemplate()));
        }

        validateTimeout(announcer);
    }
}
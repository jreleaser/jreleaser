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
import org.jreleaser.model.internal.announce.DiscussionsAnnouncer;
import org.jreleaser.util.Errors;

import java.nio.file.Files;

import static org.jreleaser.model.internal.validation.common.Validator.resolveActivatable;
import static org.jreleaser.model.internal.validation.common.Validator.validateTimeout;
import static org.jreleaser.util.StringUtils.isBlank;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 0.1.0
 */
public final class DiscussionsAnnouncerValidator {
    private static final String DEFAULT_DISCUSSIONS_TPL = "src/jreleaser/templates/discussions.tpl";

    private DiscussionsAnnouncerValidator() {
        // noop
    }

    public static void validateDiscussions(JReleaserContext context, DiscussionsAnnouncer announcer, Errors errors) {
        context.getLogger().debug("announce.discussions");
        resolveActivatable(context, announcer, "announce.discussions", "NEVER");
        if (!announcer.resolveEnabledWithSnapshot(context.getModel().getProject())) {
            context.getLogger().debug(RB.$("validation.disabled"));
            return;
        }

        if (!org.jreleaser.model.api.release.GithubReleaser.TYPE.equals(context.getModel().getRelease().getReleaser().getServiceName())) {
            errors.configuration(RB.$("validation_discussions_enabled"));
            context.getLogger().debug(RB.$("validation.disabled"));
            announcer.disable();
            return;
        }

        if (isBlank(announcer.getOrganization())) {
            errors.configuration(RB.$("validation_must_not_be_blank", "discussions.organization"));
        }

        if (isBlank(announcer.getTeam())) {
            errors.configuration(RB.$("validation_must_not_be_blank", "discussions.team"));
        }

        if (isBlank(announcer.getTitle())) {
            announcer.setTitle(RB.$("default.discussion.title"));
        }

        if (isNotBlank(announcer.getMessageTemplate()) &&
            !Files.exists(context.getBasedir().resolve(announcer.getMessageTemplate().trim()))) {
            errors.configuration(RB.$("validation_directory_not_exist", "discussions.messageTemplate", announcer.getMessageTemplate()));
        }

        if (isBlank(announcer.getMessage()) && isBlank(announcer.getMessageTemplate())) {
            if (Files.exists(context.getBasedir().resolve(DEFAULT_DISCUSSIONS_TPL))) {
                announcer.setMessageTemplate(DEFAULT_DISCUSSIONS_TPL);
            } else {
                announcer.setMessage(RB.$("default.release.message"));
            }
        }

        validateTimeout(announcer);
    }
}
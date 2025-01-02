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
import org.jreleaser.model.internal.announce.LinkedinAnnouncer;
import org.jreleaser.util.Errors;

import java.nio.file.Files;

import static org.jreleaser.model.api.announce.LinkedinAnnouncer.LINKEDIN_ACCESS_TOKEN;
import static org.jreleaser.model.api.announce.LinkedinAnnouncer.LINKEDIN_OWNER;
import static org.jreleaser.model.internal.validation.common.Validator.checkProperty;
import static org.jreleaser.model.internal.validation.common.Validator.resolveActivatable;
import static org.jreleaser.model.internal.validation.common.Validator.validateTimeout;
import static org.jreleaser.util.CollectionUtils.listOf;
import static org.jreleaser.util.StringUtils.isBlank;
import static org.jreleaser.util.StringUtils.isNotBlank;

/**
 * @author Andres Almiray
 * @since 1.5.0
 */
public final class LinkedinAnnouncerValidator {
    private static final String DEFAULT_LINKEDIN_TPL = "src/jreleaser/templates/linkedin.tpl";

    private LinkedinAnnouncerValidator() {
        // noop
    }

    public static void validateLinkedin(JReleaserContext context, LinkedinAnnouncer announcer, Errors errors) {
        context.getLogger().debug("announce.linkedin");
        resolveActivatable(context, announcer, "announce.linkedin", "NEVER");
        if (!announcer.resolveEnabledWithSnapshot(context.getModel().getProject())) {
            context.getLogger().debug(RB.$("validation.disabled"));
            return;
        }

        announcer.setOwner(
            checkProperty(context,
                listOf(
                    "announce.linkedin.owner",
                    LINKEDIN_OWNER),
                "announce.linkedin.owner",
                announcer.getOwner(),
                errors,
                true)); // optional

        announcer.setAccessToken(
            checkProperty(context,
                listOf(
                    "announce.linkedin.access.token",
                    LINKEDIN_ACCESS_TOKEN),
                "announce.linkedin.accessToken",
                announcer.getAccessToken(),
                errors,
                context.isDryrun()));

        if (isBlank(announcer.getSubject())) {
            announcer.setSubject("{{projectNameCapitalized}} {{projectVersion}} released");
        }

        if (isBlank(announcer.getMessage()) && isBlank(announcer.getMessageTemplate())) {
            if (Files.exists(context.getBasedir().resolve(DEFAULT_LINKEDIN_TPL))) {
                announcer.setMessageTemplate(DEFAULT_LINKEDIN_TPL);
            } else {
                announcer.setMessage(RB.$("default.release.message"));
            }
        }

        if (isNotBlank(announcer.getMessageTemplate()) &&
            !Files.exists(context.getBasedir().resolve(announcer.getMessageTemplate().trim()))) {
            errors.configuration(RB.$("validation_directory_not_exist", "linkedin.messageTemplate", announcer.getMessageTemplate()));
        }

        validateTimeout(announcer);
    }
}